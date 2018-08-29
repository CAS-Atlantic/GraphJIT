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
package code.jit.asm.rules;

import code.jit.asm.common.utils.Constants;
import code.jit.asm.common.utils.ReflectionUtil;

/**
 * @author shijiex
 *
 */
public class MethodHandleRule extends BaseRule {
	
	public MethodHandleRule(RuleKind kind){
		super(RuleKind.METHODHANDLE);
	}

	/* (non-Javadoc)
	 * @see code.jit.asm.rules.IRule#filterCallee(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean filterCallee(String className, String methodName, String desc) {
		if(!methodName.equals(Constants.INVOKE_EXACT)) return false;
		if(!ReflectionUtil.isMethodHandleChild(className)) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see code.jit.asm.rules.IRule#isCallerMethodAllowedInlining(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isCallerMethodAllowedInlining(String className,
			String methodName) {
		if(className.startsWith(Constants.JAVA)) return false;
		  
		return super.isCallerMethodAllowedInlining(className, methodName);
	}

	/* (non-Javadoc)
	 * @see code.jit.asm.rules.IRule#isTypeInlineable(java.lang.String)
	 */
	@Override
	public boolean isTypeInlineable(String className) {
//		if(!ReflectionUtil.isMethodHandleChild(className)){
//			return super.isTypeInlineable(className);
//		}
		if(className.contains(Constants.METHODHANDLE)) return true;
		return super.isTypeInlineable(className);
	}

}
