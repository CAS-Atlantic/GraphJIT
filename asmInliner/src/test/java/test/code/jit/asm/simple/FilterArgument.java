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


/**
 * @author shijiex
 *
 */
public class FilterArgument {
	
	public Base[] _filters;
	public Target _target;
	public FilterArgument(){
		_target = new Target();
		_filters=new Base[]{
				new BarFilter(),
				new YahooFilter()
		};
	}
	
	public String invokeExact(String a, String b){
		
		return _target.invokeExact(_filters[0].invokeExact(a), _filters[1].invokeExact(b));
	}
	
}

