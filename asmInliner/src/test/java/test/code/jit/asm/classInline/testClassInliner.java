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
package test.code.jit.asm.classInline;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.Before;
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
 * @author shijiex
 *
 */
public class testClassInliner extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		IPlugin plugin = new SimplePlugin(PluginType.SIMPLE);
		IRule rule = new ClassInlineRule();
		ConfigurationService.get().INLINE_CODE = InlineCode.CLASS_INLINE;
	}

	@Test
	public void testCalleeFieldMember(){

		BytecodeCacheService.get().reset();
		CI_Callee_2 obj = new CI_Callee_2(2, "xu");
		
		try {
			Object mappd = BytecodeGenerator.get().generate(obj);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"sayHello",
					MethodType.methodType(int.class, String.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			String message = "Xu Shijie Is here..";
			int t = (int)handle.invokeExact(mappd, message);
			assertEquals(obj.sayHello(message), t);
			System.out.println(mappd.getClass().getName()+" return value is : "+ t);

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
	public void testCICaller_two(){
		
		CI_Caller1 obj = new CI_Caller1(10, new CI_Callee_2(2, "xu"));
		try {
			Object mappd = BytecodeGenerator.get().generate(obj);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"test_two_fields_callee",
					MethodType.methodType(int.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			int q = (int)handle.invokeExact(mappd);
			assertEquals(q, obj.test_two_fields_callee());
			System.out.println(mappd.getClass().getName());

		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
			fail("fale...");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail("fale...");
		} catch (Throwable e) {
			e.printStackTrace();
			fail("fale...");
		}

	}
	
	
	@Test
	public void testCICaller_three(){
		
	
		CI_Caller1 obj1 = new CI_Caller1(4, new CI_Callee_2(5, "xu"));
		CI_Caller1 obj2 = new CI_Caller1(10, new CI_Callee_2(11, "shijie"));
		
		NewCaller caller = new NewCaller(obj1, obj2);
		try {
			Object mappd = BytecodeGenerator.get().generate(caller);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"test",
					MethodType.methodType(int.class, String.class, String.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			int expectedRes = caller.test("xu", "shijie");
			
			System.out.println("===================");
			int res = (int)handle.invokeExact(mappd, "xu", "shijie");

			System.out.println(mappd.getClass().getName());
			
			assertEquals(res, expectedRes);

		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
			fail("fale...");
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		}

	}
	
	@Test
	public void testArrayCallee(){
		
		ArrayCallee caller = new ArrayCallee(4, "shijie");
		try {
			Object mappd = BytecodeGenerator.get().generate(caller);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"sumInt",
					MethodType.methodType(int.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			int res = (int)handle.invokeExact(mappd);

			System.out.println(mappd.getClass().getName());
			
			assertEquals(res, 6);

		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
			fail("fale...");
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		}

	}

	
	
	@Test
	public void testArrayCaller(){
		
		ArrayCaller caller = new ArrayCaller(3,4);
		try {
			Object mappd = BytecodeGenerator.get().generate(caller);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"sum",
					MethodType.methodType(int.class, String.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			int res = (int)handle.invokeExact(mappd, "xu");

			System.out.println(mappd.getClass().getName());
			
			assertEquals(res, 9);

		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
			fail("fale...");
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		}

	}
	
	@Test
	public void testArrayCallers(){
		
		ArrayCallers caller = new ArrayCallers("Shijie", 3);
		String res = caller.testArray();
		try {
			Object mappd = BytecodeGenerator.get().generate(caller);

			MethodHandle handle = MethodHandles.publicLookup().findVirtual(
					mappd.getClass(),
					"testArray",
					MethodType.methodType(String.class));
			handle = handle.asType(handle.type().changeParameterType(0,
					Object.class));
			;

			System.out.println(mappd.getClass().getName());
			
			assertEquals(res, (String)handle.invokeExact(mappd));

		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch blockha
			e1.printStackTrace();
			fail("fale...");
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("fale...");
		}

	}

	
}
