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

package test.code.jit.asm.simple.standalone;

import static org.junit.Assert.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

import org.junit.Before;
import org.junit.Test;

public class MHPoolTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		//fail("Not yet implemented");
	}

	@Test
	public void testPool(){
//		try {
//			MethodHandle handle = MethodHandles.publicLookup().findVirtual(String.class, "hashCode", MethodType.methodType(int.class));
//			MethodHandle h = MethodHandle.getUnique(handle);
//			int res = (int)h.invokeExact("Shijie");
//			System.out.println(res);
//		} catch (IllegalAccessException | NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (WrongMethodTypeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	@Test
	public void test1() throws WrongMethodTypeException, Throwable{
		MethodHandle handle = MethodHandles.publicLookup().findVirtual(String.class, "length", MethodType.methodType(int.class));
		System.out.println((int)handle.invokeExact("str"));
	}
}
