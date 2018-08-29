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
public class ArrayCaller {

	ArrayCallee _a;
	ArrayCallee _b;
	
	public ArrayCaller(int i, int j){
		_a = new  ArrayCallee(i, "shi_"+i);
		_b = new  ArrayCallee(j, "jie_"+j);
		
		
	}
	
	public int sum(String a){
		System.out.println("[ArrayCaller] "+a);
		return _a.sumInt()+_b.sumInt();
	}
	
}
