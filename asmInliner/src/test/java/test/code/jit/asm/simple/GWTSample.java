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

import code.jit.asm.common.Blob;
import code.jit.asm.common.IGraphNode;
import test.code.jit.asm.methodhandle.Functions;

/**
 * @author shijiex
 *
 */
public class GWTSample implements IGraphNode{

	MHGuard _guard;
	MHTrueCallee _true;
	MHFalseCallee _false;
	
	public GWTSample(MHGuard guard, MHTrueCallee trueCallee, MHFalseCallee falseCallee){
		_guard = guard;
		_true = trueCallee;
		_false = falseCallee;
	}
	
	public String invokeExact(String a, String b){
		
		 boolean flag = _guard.invokeExact(a);
		 
		 if(flag)
		 {
			return _true.invokeExact(a, b);
			
		 }
		return _false.invokeExact(a, b);
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInvocationCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean cacheData(Object object) {
		return true;
		
	}

	@Override
	public Object getCacheData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Blob get(String clsName, String mhName, String desc,
			boolean initInstance) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	public void test(String a, String b){
//		int c =10;
//		c =3;
//		{
//			int k = 8;
//			k++;
//			String t="shijie";
//		}
//		
//		if(c>0){
//			System.out.println(".,,,,");
//		}else{
//			c++;
//			System.out.print("asdasda");
//		}
//	}
	
//	public static void main(String[] args){
//		new GWTSample().invokeExact(args[0], args[1]);
//	}
//	
}
