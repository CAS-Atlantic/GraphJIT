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

package code.jit.asm.rules;

public interface IRule {

	/**
	 * 
	 * Check whether a given class is inlinable according to the Configuration.
	 *    
	 * @param className
	 * @param methodName
	 * @param desc
	 * @return determining whether to visit a class (Cache)or just simply using classload result. 
	 *  
	 */
	public boolean filterCallee(String className, String methodName, String desc);

	/**
	 * 
	 * Decide whether to visit a className:methodName to see inline method invocation.
	 * Return true if: 
	 *          className is registered, but no method is register. (All mehtods in the class will be visited (Constructor is exception))
	 *        className is registered, and registered method name is not empty and it contains the method name
	 * @param methodName
	 * @return
	 */
	public boolean isCallerMethodAllowedInlining(String className, String methodName);

	/**
	 * @param internalName
	 * @return
	 */
	public boolean isTypeInlineable(String className);
	
	public void register(Class cls, String... methods);
	public void register(String className, String method);
	
}
