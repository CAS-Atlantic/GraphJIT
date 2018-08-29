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

package code.jit.asm.plugins;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;

import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.AnnoBytecode;
import code.jit.asm.common.Blob;
import code.jit.asm.common.IGraphNode;
import code.jit.asm.common.PluginType;
import code.jit.asm.common.logging.GraphLogger;
import code.jit.asm.common.utils.ASMUtil;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.common.utils.ReflectionUtil;
import code.jit.asm.common.utils.Utils;
import code.jit.asm.services.BytecodeCacheService;
import code.jit.asm.services.BytecodeGenerator;
import code.jit.asm.services.InlineFilterService;


/**
 * @author shijiex
 *
 */
public class MethodHandlePlugin extends AbstractPlugin {

	private Logger _logger = GraphLogger.get(MethodHandlePlugin.class);
	
	/**
	 * @param type
	 */
	public MethodHandlePlugin(PluginType type) {
		super(PluginType.METHODHANDLE);
	}

	/* (non-Javadoc)
	 * @see code.jit.asm.plugins.AbstractPlugin#isInvokeInlinable(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isInvokeInlinable(String owner, String name, String desc) {
		if(name.equals(Constants.INVOKE_EXACT) && ReflectionUtil.isMethodHandleChild(ASMUtil.getClassName(owner))) return true;
		return false;
	}


	/**
	 *  Find the correct MethodHandle type and push new ClassContext to the BytecodeCacheService. 
	 *  
	 *  className: concrete MethodHandle class name 
	 *  methodName: Must be 'invokeexact'
	 *  desc: 
	 */
	@Override
	protected Blob generate(String owner, String methodName,
			String desc, MethodContext context) {
		String className = ASMUtil.getClassName(owner);
		if(!methodName.equals(Constants.INVOKE_EXACT) || !ReflectionUtil.isMethodHandleChild(className)) return null;
		
		Object inliningTarget = context.getInvocationReceiver().getReceiver(); 
		if(inliningTarget instanceof IGraphNode){
			IGraphNode target = (IGraphNode) inliningTarget;
			
			String generateClassName = Utils.generateMHClassName(target.getClass().getName());
			long start = System.nanoTime();
			Blob blob = target.get(generateClassName, methodName, desc, false);
			long elapsed = System.nanoTime() - start;
			{
				//if newObject instantof DirectHandle => generateClassName!= newObject.className
				//   which points to the referenced classname 
			}
			if(blob!=null){
				_logger.info("Elapsed time for template {}:{} takes {}", generateClassName, methodName, elapsed);
				if(!BytecodeCacheService.get().put("", context.getClassContext())){
					_logger.error("Fail to put content to the Cache {} ", context.getClassContext());
				}
				InlineFilterService.get().registerGeneratedClass(blob.getClassNode().name, methodName);
			}
			
			return blob;
		}
		return null;
	}
	
	@Override
	public boolean postProcess(MethodNode node, MethodVisitor mv){
		Type returnType = Type.getReturnType(node.desc); //(Ljava/util/List;)V   (List)void
		if( returnType.getSort() == Type.VOID/*returnType.equals(Type.VOID_TYPE) */){
			mv.visitInsn(Opcodes.POP);
			return true;
		}
		
		mv.visitInsn(Opcodes.SWAP);
		mv.visitInsn(Opcodes.POP);
		return true;
	}
	
	@Override
	public Blob compile(IGraphNode object){
		_logger.info(">> GraphNode node:{} ", object.getClass().getName());
		//if(SelectionService.getSelectionService().select(object)){
			String generateClassName = Utils.generateMHClassName(object.getClass().getName());
			
			long start = System.nanoTime();
			Blob blob = object.get(generateClassName, Constants.INVOKE_EXACT, "", false);
			long elapsed = System.nanoTime() - start;
			if(blob ==null){
				_logger.debug("try to compile {}, generated template is null", object);
				return null;
			}
			
			InlineFilterService.get().registerGeneratedClass(blob.getClassNode().name, Constants.INVOKE_EXACT);	
			return blob;
//			Object blobObj = blob.getTemplateObject();
//			if(blobObj!=null && Utils.isLegalGenerated(blobObj.getClass().getName())){
//				
//				_logger.info("Elapsed time for template {}:{} takes {}", generateClassName, Constants.INVOKE_EXACT, elapsed);
//				InlineFilterService.get().getCurrentRule().register(blobObj.getClass(), Constants.INVOKEEXACT);	
//			}
//				
//			_logger.debug("Compile {} succeeds and generated template obj is {} ", object, blob.getTemplateObject());
//			return  blob.getTemplateObject();	
//		}
//			
//		return object;
	}
	@Override
	public boolean track(String desc){
		return desc.equals(Constants.METHODHANDLE_ARRAY); 
	}

	@Override
	protected void traverseTarget(IGraphNode object) {
		Class cls = object.getClass();
		 while (cls != null && cls!= MethodHandle.class) {
		     for (Field field : object.getClass().getDeclaredFields()) {
		    	 if(!field.isAnnotationPresent(AnnoBytecode.class)) continue;
		    	 for(Annotation anno: field.getAnnotations()){
		    		 if(anno instanceof AnnoBytecode){
		    			 AnnoBytecode annoBytecode = (AnnoBytecode) anno;
		    			 if(!annoBytecode.excluded()){
		    				 Object obj = ReflectionUtil.getFieldObject(object, field.getName());
		    				 Object compiledChildGraph = generateChildObject(obj);
		    				 if(compiledChildGraph!=null){
		    					 try {
									ReflectionUtil.setFieldObject(object, field.getName(), compiledChildGraph);
								} catch (NoSuchFieldException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		    				 }
		    			 }
		    		 }
		    	 }
		     }
		     cls = cls.getSuperclass();
		 }
   	
	}
	
	protected Object generateChildObject(Object object){
		//Casting to MethodHandle forcely is not a good option here..
		if(!(object instanceof MethodHandle)){
			return object;
		}
		//Cast object to MethodHandle manually. 
		MethodHandle handle = (MethodHandle) object;
		try {
			Object newObj = BytecodeGenerator.get().generate(handle, true, false);
			MethodHandle generatedHandle;
			generatedHandle = MethodHandles.publicLookup().findVirtual(newObj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			return generatedHandle;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

}

