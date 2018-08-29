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


import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import code.jit.asm.backplane.ClassContext;
import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.plugins.IPlugin;
import code.jit.asm.services.NameMappingService;
/**
 * 
 * @author shijie xu
 *
 */
public class TransformationChain extends BaseMethodTransform {
	static final IPlugin _plugin = NameMappingService.get().getPlugin();
	
	
	public TransformationChain(int api, int access, String name, String desc,  String signature, MethodVisitor mv, ClassContext classContext) {
		super(api, mv, classContext.getClassName(), name);
		
		final MethodContext context = new MethodContext(classContext, name, desc);
		
//		Printer printer = new Textifier();
//		TraceMethodVisitor  methodPrinter= new TraceMethodVisitor(mv, printer);
		
		context.setRawMV(mv);
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS); 
		context.getClassContext().setDesc(desc);
		MethodNode node = context.getClassContext().getMethodNode(name, desc);
		final MethodVisitor inferener = new TypeInferencer(Opcodes.ASM5, cw.visitMethod(access, name, desc, null, null), node, context);
		_visitors.add(inferener);
			
		MethodVisitor inliner = new MethodCallInliner(access, desc, context);
		_visitors.add(name.equals(Constants.CONSTRUCTOR)?new ConstructorMerge(access, desc, context):inliner);
	}

}
