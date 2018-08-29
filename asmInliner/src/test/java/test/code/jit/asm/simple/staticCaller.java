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

public class staticCaller {

	public int test(){
		String str ="xushijie";
		return staticCallee.invoke(str);
	} 
	
}

class staticCallee{
	public static int invoke(String str){
		System.out.println("[staticCallee] "+ str);
		return str.length();
	} 
}
