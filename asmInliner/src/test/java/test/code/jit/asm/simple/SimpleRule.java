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

import code.jit.asm.rules.BaseRule;
import code.jit.asm.rules.RuleKind;

/**
 * @author shijiex
 *
 */
public class SimpleRule extends BaseRule {

	/**
	 * @param cls
	 */
	public SimpleRule() {
		super(RuleKind.SIMPLE);
		register(Caller.class, "test");
		register(Callee.class, "calculate");
		register(Callee2.class, "sayHello");
		register(staticCaller.class, "test");
		register(staticCallee.class, "invoke");
		register(GWTSample.class, "invokeExact");
		register(MHFalseCallee.class, "invokeExact");
		register(MHTrueCallee.class, "invokeExact");
		register(MHGuard.class, "invokeExact");
		register(FilterArgument.class, "invokeExact");
		register(Base.class, "invokeExact");
		register(TryHandle.class, "invokeExact");
		register(CatchHandle.class, "invokeExact");
		register(TryCatchSample.class, "invokeExact");
	}


}
