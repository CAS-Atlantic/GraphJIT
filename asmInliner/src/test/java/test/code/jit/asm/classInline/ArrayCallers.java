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
public class ArrayCallers {
	
	String _name;
	
	long _value;
	
	ArrayCaller[] _callers;
	
	public ArrayCallers(String name, long value){
		
		_name = name;
		_value = value;
		_callers = new ArrayCaller[3];
		for(int i=0; i<3; i++){
			_callers[i] = new ArrayCaller(i, i*3);
		}
	}
	
	
	public String testArray(){
		System.out.println("[ArrayCallers]: "+_name+" value: "+_value);
		int t =0;
		for(int i=0; i<3; i++){
			t+= _callers[i].sum(_name);
		}
		
		return _name + t;
	}
}
