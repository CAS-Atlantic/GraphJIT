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
package code.jit.asm.plugins;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.Blob;

/**
 * @author shijiex
 *
 */
public interface IPlugin {
	public MethodNode map(String className, String methodName, String desc, MethodContext context, MethodVisitor mv);
	
	public boolean postProcess(MethodNode node, MethodVisitor mv);

	/**
	 *   It gets the blob (ClassNode, MethodNode, and FieldNodes) of the target receievr class.
	 *   
	 *  It should also be called when the MH.invocationCount> thresh and it is non-root MH.
	 *   
	 * @param obj
	 * @return
	 */
	public Blob transform(Object receiver, boolean force);

	public boolean track(String desc);
}
