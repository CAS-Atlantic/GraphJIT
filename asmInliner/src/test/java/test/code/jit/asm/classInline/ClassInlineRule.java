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

import code.jit.asm.backplane.InlineCode;
import code.jit.asm.rules.BaseRule;
import code.jit.asm.rules.RuleKind;
import code.jit.asm.services.ConfigurationService;

/**
 * @author shijiex
 *
 */
public class ClassInlineRule extends BaseRule{

	/**
	 * @param cls
	 */
	public ClassInlineRule() {
		super(RuleKind.CLASSINLINE);
		ConfigurationService.get().INLINE_CODE = InlineCode.CLASS_INLINE;
		
		register(CI_Caller1.class, "test_two_fields_callee");
		
		register(CI_Callee_2.class, "sayHello");
		register(NewCaller.class, "test");
		
		//below for the Array test
		register(ArrayCallee.class, "sumInt");
		register(ArrayCaller.class, "sum");
		
	}

}
