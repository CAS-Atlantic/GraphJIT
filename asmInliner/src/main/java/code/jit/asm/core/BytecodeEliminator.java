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

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import code.jit.asm.backplane.ClassContext;
import code.jit.asm.backplane.InlineCode;
import code.jit.asm.services.ConfigurationService;
import code.jit.asm.services.InlineFilterService;

public class BytecodeEliminator extends MethodVisitor {

	private boolean _erasing;
	private ClassContext _context;
	
	public BytecodeEliminator(MethodVisitor mv, ClassContext context) {
		super(Opcodes.ASM5, mv);
		_erasing = false;
		_context = context;
	}

	public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			Object[] stack) {
		if(_erasing){
			return;
		}
		super.visitFrame(type, nLocal, local, nStack, stack);	
	}

	public void visitInsn(int opcode) {
		
		if(ConfigurationService.get().INLINE_CODE == InlineCode.CLASS_INLINE && _erasing && opcode == Opcodes.AALOAD){
			_erasing = false;
			return;
		}else if(_erasing &&  opcode != Opcodes.AALOAD){
			return;
		}else{
			super.visitInsn(opcode);	
		}
		
	}

	public void visitIntInsn(int opcode, int operand) {
		if(_erasing){
			return;
		}

		super.visitIntInsn(opcode, operand);
	}

	public void visitVarInsn(int opcode, int var) {
		if(_erasing){
			return;
		}

		super.visitVarInsn(opcode, var);
	}

	public void visitTypeInsn(int opcode, String type) {
		if(_erasing){
			return;
		}

		super.visitTypeInsn(opcode, type);
	}

	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		
		Type fieldType = Type.getType(desc);
		if(ConfigurationService.get().INLINE_CODE != InlineCode.CLASS_INLINE ||
				InlineFilterService.get().isPrimiteType(fieldType)){
			super.visitFieldInsn(opcode, owner, name, desc);
			return ;
		}
		
		if(_context.isInlinableFieldArray(name)){
			_erasing = true;
			return ;
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		if(_erasing){
			return;
		}

		super.visitMethodInsn(opcode, owner, name, desc, itf);

	}

	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
			Object... bsmArgs) {
		if(_erasing){
			return;
		}

		super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);

	}

	public void visitJumpInsn(int opcode, Label label) {
		if(_erasing){
			return;
		}

		super.visitJumpInsn(opcode, label);
	}

	public void visitLabel(Label label) {
		if(_erasing){
			return;
		}

		super.visitLabel(label);

	}

	public void visitLdcInsn(Object cst) {
		if(_erasing){
			return;
		}

		super.visitLdcInsn(cst);
	}

	public void visitIincInsn(int var, int increment) {
		if(_erasing){
			return;
		}

		super.visitIincInsn(var, increment);
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label... labels) {
		if(_erasing){
			return;
		}

		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		if(_erasing){
			return;
		}

		super.visitLookupSwitchInsn(dflt, keys, labels);
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		if(_erasing){
			return;
		}

		super.visitMultiANewArrayInsn(desc, dims);
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {
		if(_erasing){
			return;
		}

		super.visitTryCatchBlock(start, end, handler, type);
	}

	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		if(_erasing){
			return;
		}

		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

}
