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
public class YahooFilter extends Base {

	/* (non-Javadoc)
	 * @see test.code.jit.asm.simple.Base#invokeExact(java.lang.String)
	 */
	@Override
	public String invokeExact(String a) {
		return Functions.addYahoo(a);
	}

	public Object test(){
		
		return null;
	}
	
	private boolean test(byte t){
		
		Float a = new Float(4.0);
		
		return true;
	}
}
