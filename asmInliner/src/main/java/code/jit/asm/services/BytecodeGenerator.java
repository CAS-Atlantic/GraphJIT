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

package code.jit.asm.services;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;

import code.jit.asm.backplane.BytecodeResource;
import code.jit.asm.backplane.ClassContext;
import code.jit.asm.backplane.InlineCode;
import code.jit.asm.common.Blob;
import code.jit.asm.common.IGenerator;
import code.jit.asm.common.IGraphNode;
import code.jit.asm.common.PluginType;
import code.jit.asm.common.logging.GraphLogger;
import code.jit.asm.common.utils.Configs;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.common.utils.Utils;
import code.jit.asm.core.ClassInlineVisitor;
import code.jit.asm.core.MethodInlineVisitor;
import code.jit.asm.plugins.IPlugin;
import code.jit.asm.plugins.MethodHandlePlugin;
import code.jit.asm.rules.IRule;
import code.jit.asm.rules.MethodHandleRule;
import code.jit.asm.rules.RuleKind;

/**
 * @author shijiex
 *
 */
public class BytecodeGenerator implements IGenerator{

	private static List<IAction> _actions = new LinkedList<IAction>();
	
	static {
		_actions.add(new IAction(){

			@Override
			public String getActionName() {
				return Constants.CHECK_CACHE_ACTION;
			}

			
			/**
			 *  return true:  Cache-hit but not optimized, or Generate template .
			 *         false: Cache-hit and fully optimized, or No template. 
			 */ 
			@Override
			public boolean action(ClassContext context) {
				
				BytecodeResource bc = BytecodeCacheService.get().getBcClass(context.getOwner());
				if(bc!=null){
					//No need to do if it is optimized, otherwise continue optimization.
					//context.setOriginClass(bc.getOriginalClass());
					context.fromCache(bc);
					
					
					//return !bc.isOptimized();
					return false;
				}
				
				Blob blob = NameMappingService.get().getPlugin().transform(context.getOwner(), true);
				if(blob!=null && blob.getClassNode()!=null){
					//context.setOriginClass(blob.getCompiledClass());
					context.setOriginalClassNode(blob.getClassNode());
					return true;
				}
				return false;
			}
			
		});
		
		_actions.add(new IAction(){

			@Override
			public String getActionName() {
				return Constants.TIMER_ACTION;
			}

			@Override
			public boolean action(ClassContext context) {
				return true;
			}
		});
		
		
		_actions.add(new IAction(){

			@Override
			public String getActionName() {
				// necessary filter. 
				return Constants.FILTER_ACTION;
			}

			/**
			 *  Return true: Inline YES  
			 *         false: Inline NO
			 */       
			@Override
			public boolean action(ClassContext context) {
				if(context.isCaller()) return true;  //Force to visit bytecode body.
				return InlineFilterService.get().isCalleeConfiguredInlinable(context);
			}
		});
		
		_actions.add(new IAction() {

			@Override
			public String getActionName() {
				return Constants.TRANSFORMER_ACTION;
			}

			@Override
			public boolean action(ClassContext context) {
				
				ClassNode visitor = (ConfigurationService.get().INLINE_CODE == InlineCode.CLASS_INLINE)
						? new ClassInlineVisitor(Opcodes.ASM5, context)
						: new MethodInlineVisitor(Opcodes.ASM5, context);
				
					context.getOriginalClassNode().accept(visitor);
				
				context.setGeneratedClassNode(visitor);
				return dump(visitor, context);
			}

			private boolean dump(ClassNode visitor, ClassContext context) {
				if (!Configs._enableDump)
					return true;
				FileOutputStream fos = null;
				try {
					byte[] buffer;
					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES); // Is
																												// cw
					// necessary??
					visitor.accept(cw);
					buffer = cw.toByteArray();

					CheckClassAdapter.verify(new ClassReader(buffer), false, new PrintWriter(System.err));

					fos = new FileOutputStream(Utils.getTemplDir() + context.getClassName() + ".class");
					fos.write(buffer);
				} catch (IOException e) {
					_logger.debug(e.getMessage());
				} catch (Throwable e) {
					//e.printStackTrace();
					_logger.error("exception {}, {}, {}", e, context.getOriginalClassNode(), context.getOwner());
					_logger.error("The original Bytecode Sequences are: ");
					context.getOriginalClassNode().accept(new TraceClassVisitor(new PrintWriter(System.err)));
					_logger.error("=>>>The final Bytecode Sequences are: ");
					visitor.accept(new TraceClassVisitor(new PrintWriter(System.err)));
					return false;
				} finally {
					if (fos != null)
						try {
							fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}

				return true;
			}

		}
		);

		_actions.add(new IAction(){

			@Override
			public String getActionName() {
				return Constants.UPDATECACHE_ACTION;
			}

			@Override
			public boolean action(ClassContext context) {
				if(context.getGeneratedClassNode()!=null){
					return BytecodeCacheService.get().put("", context);
				}
				return false;
			}
			
		});
		
		_actions.add(new IAction(){

			@Override
			public String getActionName() {
				return Constants.OBJECTSETTER_ACTION;
			}

			@Override
			public boolean action(ClassContext context) {
				if(context.getBObjectSetup()== false ) return true;
				context.createTransformedObject();;
				BytecodeResource resource=BytecodeCacheService.get().getBcClass(context.getOwner());
				if(resource == null) return true;
				resource.setTransformedObject(context.getGenerateObj());
				
				return true;
			}
			
		});
		
		BytecodeCacheService.get().start();
	}
	
	final static Logger _logger = GraphLogger.get(BytecodeGenerator.class);
	
	private static class GenerateorHolder{
		private static final BytecodeGenerator _instance = new BytecodeGenerator();
	}
	
	public static BytecodeGenerator get(){
		return GenerateorHolder._instance;
	} 
	
	public BytecodeGenerator(){
		_logger.info("Create generator {} ", this);
	}
	

	
	/**
	 * 
	 * @param obj  TGWD, TInsert, TFilterReturn.
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@Override
	public Object generate(Object obj)  throws InstantiationException, IllegalAccessException{
		long start = System.currentTimeMillis();
		if(generate(obj, true, true)){
			//retrieve the ClassNode and FieldNodes from Cache and then make initialization.
			BytecodeResource resource = BytecodeCacheService.get().getBcClass(obj);
			if(resource!=null &&resource.getTransformedObject()!=null){
				long elapsed = System.currentTimeMillis() - start;
				_logger.info("Generation for {} takes {} ms", obj, elapsed);
				return resource.getTransformedObject();
			}
				
		}
		return obj;
	}
	
	
	/**
	 * 
	 * The reason of adding force here is that only the first MH for the MH is increased while the left are 0 (--++) 
	 * 
	 * @param obj
	 * @param skipSelection Indicate whether to skip IMethodHandle's invocationCount and code size. If true, then 
	 *           the obj will be selected for generation unless generation fails.  
	 * @return
	 */
	public boolean generate(Object inputObj, boolean skipSelection, boolean setupObject){
		_logger.info("Generate {}:{} skip selection {}", inputObj, inputObj.getClass().getName(), skipSelection);
		
		try{
			if(!inputObj.getClass().isArray()){
				if(inputObj instanceof IGraphNode){
					IGraphNode obj = (IGraphNode) inputObj;
					ClassContext context = ClassContext.newClassContext();
					context.setOwner(obj);
					context.isObjectSetup(setupObject);
					context.setCaller();
					run(context);
					
					if(setupObject){
						context.createTransformedObject();
					}
					return true;
				}
				
				return false;

			}else{
				IGraphNode[] objs =(IGraphNode[]) inputObj;
				Object[] newObjs = new Object[objs.length];
				for(int i=0; i< newObjs.length; i++){
					if(objs[i]!=null){
						Object reObj = generate(objs[i], skipSelection, false);
						newObjs[i] = reObj;
					}
					
				}
				return true;
			}
		}catch(Throwable e){
			_logger.error(": <  Notice unexcepted error here: {} ", e);
			_logger.info("Generator cancel current generation and returns the original one {}", inputObj);
		}
		return false;
	}

	private void run(ClassContext context){
		for (IAction action : _actions) {
			if (!action.action(context)) {
//				if(action.getActionName().equals(Constants.CHECK_CACHE_ACTION)){
//					//1) Fully optimized and cache hit, or NO template.  
//					return true;
//				}else{
//					// NO INLINE.
//					System.out.println(action.getActionName()+" disable the generation for this class.");
//					return false;
//				}
				return;
			}
		}
        //return true;
	}
	
//	/**
//	 *  Try several way to find the Class Defination for given full className
//	 * @param className: Sample "java.lang.Thread"
//	 * @return
//	 */
//	private static Class retrieveClassFromClassLoader(String className){
//		try {
//			// I still need a classLoader parameters.. 
//			String name = className.replace('/', '.');
//			if(name.endsWith(".class"))  name = name.substring(0, name.lastIndexOf('.'));
//			return retrieveClassFromClassLoader(name, BytecodeGenerator.class.getClassLoader());
//		} catch (ClassNotFoundException e) {
//			//@TODO: Try other ways to load the className here.
//			System.err.println("The current Classloader does not see "+className+" before!");
//		}
//		return null;
//	}
//	
//	private static Class retrieveClassFromClassLoader(String className, ClassLoader loader) throws ClassNotFoundException{
//		return Class.forName(className, true, loader);
//	}
//	
//	public static InputStream fromClass2InputStream(Class cls){
//		String classAsPath = cls.getName().replace('.', '/') + ".class";
//		classAsPath="/"+classAsPath;
//		return cls.getResourceAsStream(classAsPath);
//		//return cls.getResourceAsStream(classAsPath);
//	}
	
	private interface IAction{
		public String getActionName();
		
		/**
		 * 
		 * @param context
		 * @return  continue next it true
		 *          Otherwise stops and then check the data in the context
		 */
		public boolean action(ClassContext context);
	}

	@Override
	public void initPlugin(PluginType pluginType) {
		if(pluginType == PluginType.METHODHANDLE){
			IPlugin plugin = new MethodHandlePlugin(PluginType.METHODHANDLE);
			IRule rule = new MethodHandleRule(RuleKind.METHODHANDLE);
			ConfigurationService.get().INLINE_CODE = InlineCode.CLASS_INLINE;	
		}else if(pluginType == PluginType.SIMPLE){
			//@TODO
		}
	}
}

