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
package code.jit.asm.core;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import code.jit.asm.backplane.ClassContext;
import code.jit.asm.common.concurrency.AtomicClassNode;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.services.ConfigurationService;
import code.jit.asm.services.InlineFilterService;

/**
 * @author shijiex
 *
 */
public class MethodInlineVisitor extends AtomicClassNode {

	ConstructorMethod  _constructor;
	MethodNode   _defaultConstr;
	protected boolean emptyConstFlag = false;
	
	ClassContext _context;
	
	protected String _superName;
	
	/**
	 * @param api
	 * @param cv
	 */
	public MethodInlineVisitor(int api, ClassContext context) {
		super(api);
		_context = context;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
	            String superName, String[] interfaces) {
		_superName = superName;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		if(ConfigurationService.get().SKIP_STATICMETHOD && 
				(access == (Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC) ||
				 access == (Opcodes.ACC_STATIC|Opcodes.ACC_PRIVATE))){
			return null;
		}
		
		if (name.equals(Constants.CONSTRUCTOR) && desc.equals(Constants.EMPTY_DESC)) {
			emptyConstFlag = true;
		}

		if(!InlineFilterService.get().isMethod4InlineVisitCaller(_context.getOriginalClassName(), name)){
			
			if((access==Opcodes.ACC_PRIVATE||access == 0 ) && desc.equals(Constants.EMPTY_DESC)) access =Opcodes.ACC_PUBLIC; // access=0 result to newInstance failure.
			
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
		
		MethodNode mv = (MethodNode) super.visitMethod(access, name, desc, signature, exceptions);
	    return new TransformationChain(Opcodes.ASM5, access, name, desc, signature, mv, _context); 
	}
	
	@Override
	public void visitEnd(){
		postClassGeneration();
		super.visitEnd();
	}
	
	protected void postClassGeneration(){
		if(emptyConstFlag == false){
			addDefaultConstructor();
		}
	}
	
	private void addDefaultConstructor() {
		MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, Constants.CONSTRUCTOR, Constants.EMPTY_DESC, null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, _superName, "<init>", "()V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	
	
}
