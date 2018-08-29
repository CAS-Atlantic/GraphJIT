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

package code.jit.asm.common.concurrency;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

public class AtomicMethodNode extends MethodNode {
	public AtomicMethodNode(final int asm5, final int accPublic, final String invokeExact, 
			final String methodDescriptorString, final String signature,String[] strings) {
		super(asm5, accPublic, invokeExact, methodDescriptorString, signature, strings);
	}

	@Override
	public void accept(final MethodVisitor mv) {
		synchronized (this) {
			// Thread.sleep(3s)
			super.accept(mv);
		}
	}
}
