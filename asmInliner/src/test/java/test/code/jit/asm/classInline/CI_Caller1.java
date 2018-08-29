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
public class CI_Caller1 {
	 int _data;
	 CI_Callee_2 _callee;
	
	public CI_Caller1(int data, CI_Callee_2 callee){
		_data = data;
		_callee = callee;
	}
	
	public int test_two_fields_callee(){
		System.out.println("[CI_Caller1]Here is in the CI_Caller_case1:test_two_fields_callee: " + _data);
		_callee.sayHello("xyushijie ");
		System.out.println("[CI_Caller1]{PASS}");
		return _data;
	}
	

}
