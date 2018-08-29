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

import test.code.jit.asm.methodhandle.Functions;

/**
 * @author shijiex
 *
 */
public class MHGuard {

	public boolean invokeExact(String a){
		
		return Functions.isFooString(a);
	}
	
	public void test(int a, int b){
		int c = a+b;
		c=c*2;
		if(c>10){
			System.out.println(c);
		}else{
			int t = c;
			c=t--;
		}
		
		int k = c;
		
	}
}
