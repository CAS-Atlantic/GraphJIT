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

public class Caller {

	public final Callee _callee ;
	
	public Caller(Callee callee){
		_callee = callee;
	}

	public void test(int a, int b){
		long r = (long)a- _callee.calculate(a, b, "xyu", 1000l);
		
		long c = a +_callee.calculate(b, 0, "shiji", 45l);
		System.out.println(r);
	}
		
	
	public static void main(String[] args) {
		
		new Caller(new Callee(new Callee2())).test(1, 2);
	}
}
