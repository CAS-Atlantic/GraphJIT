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

package test.code.jit.asm.simple;

public class Callee {
	
	Callee2 _call2;
	public Callee(Callee2 callee2){
		_call2 = callee2;
	}
	
	public long calculate(int t, int p, String a, long x){
		
		int tmp=_call2.sayHello("xushijie");
//		try{
//			//MethodHandles.publicLookup().findVirtual(String.class, "test", MethodType.methodType(int.class));
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		
		int m = tmp+p;
		System.out.println(tmp+"  "+t+"  "+p);
		//System.out.println(x+a.length());
		return tmp;
	}

	public static void main(String[] args) {
		new Callee(new Callee2()).calculate(0, 1, "sad", 1l);
	}
}
