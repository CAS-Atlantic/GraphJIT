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
package test.code.jit.asm.simple;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import code.jit.asm.backplane.InlineCode;
import code.jit.asm.common.PluginType;
import code.jit.asm.plugins.IPlugin;
import code.jit.asm.plugins.SimplePlugin;
import code.jit.asm.rules.IRule;
import code.jit.asm.services.BytecodeCacheService;
import code.jit.asm.services.BytecodeGenerator;
import code.jit.asm.services.ConfigurationService;
import junit.framework.TestCase;

/**
 * @author shijie xu
 *
 */
public class testMethodInliner extends TestCase {

	protected void setUp() throws Exception {
		IPlugin plugin = new SimplePlugin(PluginType.SIMPLE);
		IRule rule = new SimpleRule();
	}
	
	@Test
	public void testMICallee2(){
		IRule rule = new SimpleRule();
		String callee = "test.code.jit.asm.simple.Callee";
		    
		Callee2 obj = new Callee2();
		
		 try {
				Object mappd = BytecodeGenerator.get().generate(obj);
				
				MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "sayHello", MethodType.methodType(int.class, String.class));
				handle = handle.asType(handle.type().changeParameterType(0, Object.class));
				int t = (int)handle.invokeExact(mappd, "xushijie");
				
				System.out.println(mappd.getClass().getName());
				
			} catch (InstantiationException | IllegalAccessException e1) {
				// TODO Auto-generated catch blockha
				e1.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	@Test
	public void testMIFun(){
    	ConfigurationService.get().INLINE_CODE = InlineCode.METHOD_INLINE;
	   	
	 	String caller = "test.code.jit.asm.simple.Caller";
	    String callee = "test.code.jit.asm.simple.Callee";
	    
	    Caller obj = new Caller(new Callee(new Callee2()));
	    
	    try {
			Object mappd = BytecodeGenerator.get().generate(obj);
			
			MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "test", MethodType.methodType(void.class, int.class, int.class));
			handle = handle.asType(handle.type().changeParameterType(0, Object.class));
			handle.invokeExact(mappd, 2,3);
			
			System.out.println(mappd.getClass().getName());
			
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void testMICallee(){
	   	IRule rule = new SimpleRule();
	   	ConfigurationService.get().INLINE_CODE = InlineCode.METHOD_INLINE;
	   	BytecodeCacheService.get().reset();
	    Callee obj = new Callee(new Callee2());
	    Caller mycaller = new Caller(obj); 
	    try {
			Object mappd = BytecodeGenerator.get().generate(obj);
			
			MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "calculate", MethodType.methodType(long.class, int.class, int.class, String.class, long.class));
			handle = handle.asType(handle.type().changeParameterType(0, Object.class));
			long t = (long)handle.invokeExact(mappd, 2,3, "xushijie", (long)1.0);
			
			System.out.println(mappd.getClass().getName());
			
			mappd = BytecodeGenerator.get().generate(mycaller);
			handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "test", MethodType.methodType(void.class, int.class, int.class));
			handle = handle.asType(handle.type().changeParameterType(0, Object.class));
			handle.invokeExact(mappd, 2,3);
			
			
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public void testMICalleeChild(){
		ConfigurationService.get().INLINE_CODE = InlineCode.METHOD_INLINE;
		BytecodeCacheService.get().reset();

		Callee obj = new Callee(new Callee2Child());
		try {
			Object mappd = BytecodeGenerator.get().generate(obj);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"calculate",
					MethodType.methodType(long.class, int.class, int.class,
							String.class, long.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			long t = (long) handle.invokeExact(mappd, 2, 3, "xushijie",
					(long) 1.0);

			System.out.println(mappd.getClass().getName());
			assertEquals(t, obj.calculate(2, 3, "xushijie", (long)1.0));

		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void testCICalleeChild(){
		IRule rule = new SimpleRule();
		ConfigurationService.get().INLINE_CODE = InlineCode.CLASS_INLINE;
		BytecodeCacheService.get().reset();

		Callee obj = new Callee(new Callee2Child());
		try {
			Object mappd = BytecodeGenerator.get().generate(obj);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"calculate",
					MethodType.methodType(long.class, int.class, int.class,
							String.class, long.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			long t = (long) handle.invokeExact(mappd, 2, 3, "xushijie",
					(long) 1.0);

			assertEquals(t, obj.calculate(2, 3, "xushijie", (long)1.0));

		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testCICallee2(){
		ConfigurationService.get().INLINE_CODE = InlineCode.CLASS_INLINE;
		
		Callee2 obj = new Callee2();
		
		 try {
				Object mappd = BytecodeGenerator.get().generate(obj);
				
				MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "sayHello", MethodType.methodType(int.class, String.class));
				handle = handle.asType(handle.type().changeParameterType(0, Object.class));
				int t = (int)handle.invokeExact(mappd, "xushijie");
				
				System.out.println(mappd.getClass().getName());
				
			} catch (InstantiationException | IllegalAccessException e1) {
				// TODO Auto-generated catch blockha
				e1.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	@Test
	public void testCICallee() {
		ConfigurationService.get().INLINE_CODE = InlineCode.CLASS_INLINE;
		BytecodeCacheService.get().reset();
		Callee obj = new Callee(new Callee2());
		try {
			Object mappd = BytecodeGenerator.get().generate(obj);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"calculate",
					MethodType.methodType(long.class, int.class, int.class,
							String.class, long.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			long t = (long) handle.invokeExact(mappd, 2, 3, "xushijie",
					(long) 1.0);

			System.out.println(mappd.getClass().getName());

		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
	
	@Test
	public void testStaticCallee(){
	   	IRule rule = new SimpleRule();
	   	ConfigurationService.get().INLINE_CODE = InlineCode.METHOD_INLINE;

	   staticCaller caller = new staticCaller();
	    try {
			Object mappd = BytecodeGenerator.get().generate(caller);
			
			MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "test", MethodType.methodType(int.class));
			handle = handle.asType(handle.type().changeParameterType(0, Object.class));
			int t = (int)handle.invokeExact(mappd);
			
			System.out.println(mappd.getClass().getName());
			
			assertEquals(caller.test(), t);
			
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

   @Test
   public void test_gwt(){
	   	IRule rule = new SimpleRule();
	   	ConfigurationService.get().INLINE_CODE = InlineCode.METHOD_INLINE;
	   	BytecodeCacheService.get().reset();
	   	
	   	GWTSample obj = new GWTSample(new MHGuard(), new MHTrueCallee(), new MHFalseCallee());
	   	
	    try {
			Object mappd = BytecodeGenerator.get().generate(obj);
			
			MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "invokeExact", MethodType.methodType(String.class, String.class, String.class));
			handle = handle.asType(handle.type().changeParameterType(0, Object.class));
			String trueRes = (String)handle.invokeExact(mappd, "foo", "zhe");
			assertEquals(obj.invokeExact("foo", "zhe"), trueRes);
			//System.out.println(mappd.getClass().getName());
			
			String falseRes = (String)handle.invokeExact(mappd, "fool", "zhe");
			assertEquals(obj.invokeExact("fool", "zhe"), falseRes);
			
			
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
   
   @Test
   public void testFilterArgument(){
	   	IRule rule = new SimpleRule();
	   	ConfigurationService.get().INLINE_CODE = InlineCode.METHOD_INLINE;
	   	BytecodeCacheService.get().reset();
	   	
	   	FilterArgument obj = new FilterArgument();
	   	
	    try {
			Object mappd = BytecodeGenerator.get().generate(obj);
			
			MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "invokeExact", MethodType.methodType(String.class, String.class, String.class));
			handle = handle.asType(handle.type().changeParameterType(0, Object.class));
			String res = (String)handle.invokeExact(mappd, "foo", "zhe");
			assertEquals(obj.invokeExact("foo", "zhe"), res);
			System.out.println(res);
			
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
   
   @Test
   public void testExceptionHandle(){
	   	IRule rule = new SimpleRule();
	   	ConfigurationService.get().INLINE_CODE = InlineCode.METHOD_INLINE;
	   	BytecodeCacheService.get().reset();
	   	
	   	TryCatchSample obj = new TryCatchSample();
	   	List<String> arg1 = new LinkedList<String>(); arg1.add("ttt");
	   	List<String> arg2 = new LinkedList<String>(); arg2.add("ttt");
	    try {
			Object mappd = BytecodeGenerator.get().generate(obj);
			
			MethodHandle handle = MethodHandles.publicLookup().findVirtual(mappd.getClass(), "invokeExact", MethodType.methodType(int.class, List.class));
			handle = handle.asType(handle.type().changeParameterType(0, Object.class));
			int res = (int)handle.invokeExact(mappd, arg2);
			assertEquals(obj.invokeExact(arg1), res);
			System.out.println(res);
			
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
   }
}



