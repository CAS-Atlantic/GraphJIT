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

import java.util.List;

import test.code.jit.asm.methodhandle.Functions.BlahException;

public class TryCatchSample {

	TryHandle tryHandle = new TryHandle();
	CatchHandle catchHandle = new CatchHandle();
	
	public int invokeExact(List<String> arg) {
		try {
			tryHandle.invokeExact(arg);
		} catch (BlahException e) {
			catchHandle.invokeExact(e, arg);
		}
		return arg.size();
	}
}
