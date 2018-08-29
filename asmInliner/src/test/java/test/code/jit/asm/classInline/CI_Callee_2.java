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
public class CI_Callee_2 {
	
	int _a1 =2 ;
	String _t = "mytest here";
	
	public CI_Callee_2(Integer a1, String t){
		_a1 = a1;
		_t = t;
	}
	
	public int sayHello(String message){
		System.out.println("[CI_Callee_2]: " +message + " a="+_a1+"  _t="+_t);
		//System.out.println(_a1);
		return _a1+_t.length() + message.length();
	}

}
