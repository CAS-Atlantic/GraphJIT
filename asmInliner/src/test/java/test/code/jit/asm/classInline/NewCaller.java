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

/**
 * @author shijiex
 *
 */
public class NewCaller {

	CI_Caller1 _caller1;
	
	CI_Caller1 _caller2;
	
	public NewCaller(CI_Caller1 a, CI_Caller1 b){
		_caller1 = a;
		_caller2 = b;
	}
	
	public int test(String m, String q){
		int t = _caller1.test_two_fields_callee();
		
		int b = _caller2.test_two_fields_callee();
		System.out.println("[New Caller]: "+t+"  " +b+"   "+ m.length() + q.length());
		return t+b;
	}
}
