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

package code.jit.asm.core;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;

import code.jit.asm.backplane.IVisitorID;
import code.jit.asm.common.logging.GraphLogger;

/**
 * @author shijiex
 *
 */
abstract class BaseMethodTransform extends MethodVisitor {

	protected final List<MethodVisitor> _visitors = new LinkedList<MethodVisitor>();
	private Logger _logger = GraphLogger.get(BaseMethodTransform.class);
	private String _methodName;

	public BaseMethodTransform(int api, MethodVisitor mv, String className,
			String methodName) {
		super(api, mv);
		_methodName = methodName;
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		for (MethodVisitor mv : _visitors) {
			long start = System.nanoTime();
			mv.visitMethodInsn(opcode, owner, name, desc, itf);
			long elapsed = System.nanoTime() - start;
			IVisitorID mvVisitor = (IVisitorID) mv;
			_logger.info("Steps {} to parse {} elapsed {}",
					mvVisitor.getVisitorName(), _methodName, elapsed);

		}
	}

	@Override
	public void visitInsn(int opcode) {
		for (MethodVisitor mv : _visitors) {
			mv.visitInsn(opcode);
		}
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		for (MethodVisitor mv : _visitors) {
			mv.visitIntInsn(opcode, operand);
		}
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		for (MethodVisitor mv : _visitors) {
			mv.visitVarInsn(opcode, var);
		}
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		for (MethodVisitor mv : _visitors) {
			mv.visitTypeInsn(opcode, type);
		}
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		for (MethodVisitor mv : _visitors) {
			mv.visitFieldInsn(opcode, owner, name, desc);
		}
	}

	@Override
	public void visitLdcInsn(Object cst) {
		for (MethodVisitor mv : _visitors) {
			mv.visitLdcInsn(cst);
		}
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		for (MethodVisitor mv : _visitors) {
			mv.visitIincInsn(var, increment);
		}
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			Object[] stack) {
		for (MethodVisitor mv : _visitors) {
			if (_visitors.get(_visitors.size() - 1) != mv) {
				mv.visitFrame(type, nLocal, local, nStack, stack);
			}

		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		for (MethodVisitor mv : _visitors) {
			if (mv != _visitors.get(_visitors.size() - 1)) {
				continue;
			}

			mv.visitMaxs(maxStack, maxLocals);
		}
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
			Object... bsmArgs) {
		for (MethodVisitor mv : _visitors) {
			mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		for (MethodVisitor mv : _visitors) {
			mv.visitJumpInsn(opcode, label);
		}
	}
	
	@Override
	public void visitLabel(final Label label){
		for(MethodVisitor mv: _visitors){
			mv.visitLabel(label);
		}
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label... labels) {
		for (MethodVisitor mv : _visitors) {
			mv.visitTableSwitchInsn(min, max, dflt, labels);
		}
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		for (MethodVisitor mv : _visitors) {
			mv.visitLookupSwitchInsn(dflt, keys, labels);
		}
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		for (MethodVisitor mv : _visitors) {
			mv.visitMultiANewArrayInsn(desc, dims);
		}
	}
}
