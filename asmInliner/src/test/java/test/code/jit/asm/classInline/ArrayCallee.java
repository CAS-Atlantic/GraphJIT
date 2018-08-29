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
public class ArrayCallee {

	int[] _iData;
	String[] _sData;
	
	public ArrayCallee(int len, String s){
		if(len ==0 ) len =1;
		_iData = new int[len];
		_sData = new String[len];
		
		for(int i=0; i<len; i++){
			_iData[i] = i;
			_sData[i] =s+i*len;
		}
	}
	
	public int sumInt(){
		
		int t = 0;
		System.out.println("begin..");
		System.out.println(_iData.length);
		for(int i=0; i<_iData.length; i++){
			t+=_iData[i];
		}
		System.out.println("End..");
		System.out.println("[ArrayCallee] "+_sData[_iData.length-1]);
		return t;
	}
}
