/*******************************************************************************
 * Copyright (c) 2018 IBM Corp. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

/**
 * 
 */
package code.jit.asm.plugins;



import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;

import code.jit.asm.backplane.BytecodeResource;
import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.Blob;
import code.jit.asm.common.IGraphNode;
import code.jit.asm.common.PluginType;
import code.jit.asm.common.logging.GraphLogger;
import code.jit.asm.common.utils.ASMUtil;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.services.BytecodeCacheService;
import code.jit.asm.services.NameMappingService;
import code.jit.asm.services.SelectionService;


/**
 * @author shijiex
 *
 */


public abstract class AbstractPlugin implements IPlugin{
	
	protected PluginType _type ;
	
	private Logger _logger = GraphLogger.get(AbstractPlugin.class);
	
	public AbstractPlugin(PluginType type){
		_type = type;
		NameMappingService.get().register(type, this);
	}

	/* (non-Javadoc)
	 * @see code.jit.asm.plugins.IPlugin#map(java.lang.String, java.lang.String, java.lang.String, code.jit.asm.backplane.MethodContext)
	 */
	@Override
	public MethodNode map(String owner, String methodName, String desc,
			code.jit.asm.backplane.MethodContext context, MethodVisitor mv) {
		
		if(isInvokeInlinable(owner, methodName, desc) == false
				|| context.isInvocationReceiverFilted()) return null;
		
		if(context.getInvocationReceiver()!=null){
			IGraphNode key = (IGraphNode) context.getInvocationReceiver().getReceiver();
			BytecodeResource resource =  BytecodeCacheService.get().getBcClass(key);
			if(resource!=null){
				MethodNode mn = resource.getTargetMethodNode(methodName, desc);
				if(mn!=null) return mn;
			}
		}
		
		//Normally, you should not reach this point. 
		Blob mapped = generate(owner, methodName, desc, context);
		
		if(mapped !=null) {
			if(mapped.isVisited()){
				ClassNode classNode = mapped.getClassNode();
				return ASMUtil.getMethodNode(classNode, methodName, desc);
			}else if(mapped.getTemplateObject()!=null){
				//The MethodNode requires optimization. 
				return defaultGenerate(mapped.getTemplateObject(), methodName, desc);
			}else if(mapped.getMethodNode()!=null){
				return mapped.getMethodNode();
			}
		}
		return null;
	}
	
	protected abstract Blob generate(String className, String methodName, String desc, MethodContext context);
	protected boolean preprocess(MethodVisitor mv, MethodNode node){
		return true;
	}	
	
	protected MethodNode defaultGenerate(Object obj, String methodName, String desc){
//		try {
//			if(BytecodeGenerator.get().generate(obj, true)){
//				BytecodeClass resource = BytecodeCacheService.get().getBcClass(target.getClass().getName());
//				if(target!=null){
//					return ASMUtil.getMethodNode(resource, methodName, desc);
//				}
//			}
//			
//			
//		} catch (InstantiationException | IllegalAccessException e) {
//			e.printStackTrace();
//		}	
		return null;
	}
		
	public boolean postProcess(MethodNode node, MethodVisitor mv){
		//MethodVisitor adapter = context.getFrameTracker();
		
		//mv.visitFrame(Frame, nLocal, local, nStack, stack);
		return true;
	}
	
	@Override
	public Blob transform(Object obj, boolean skipSelection){
		
		if(obj instanceof IGraphNode){
			IGraphNode object = (IGraphNode)obj;
			_logger.debug("IGraphNode node:{}: ", object.getClass().getName());
			if(skipSelection || SelectionService.getSelectionService().select(object)){
				return compile(object);
			}else{
				traverseTarget(object);		
			}
		}
		return null;
	}
	
	
	/**
	 *  Traverse object graph and generate a child objects if it can be selected, and then wrapped the genereated as 
	 *  a new IMethodHandle. 
	 *  
	 *  For example: 
	 *  
	 *   MH1 ->MH2 -> MH3 -> MH5 ..
	 *  
	 *   if MH2 is selected, then the graph after MH2 are generated and wrapped as AsTypeHandle and then attached
	 *    to MH1's child.  
	 *   
	 *   The implementation will be based on Reflection. 
	 *   
	 * @param object
	 */
	protected void traverseTarget(IGraphNode object) {
	}

	public Blob compile(IGraphNode receiver){
		return null;
	}
	public abstract boolean isInvokeInlinable(String owner, String name, String desc);
	
	public PluginType getType(){
		return _type;
	}
	
	public boolean track(String desc){
		return desc.startsWith(Constants.LEFT_BRACKER);
	}
	
	
}
