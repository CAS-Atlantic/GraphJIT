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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import code.jit.asm.common.utils.Constants;

/**
 * @author shijiex
 *
 */

public class AnalyzerTransformer extends ClassNode implements Opcodes {
	private String className;

	public AnalyzerTransformer() {
		super(ASM5);
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		className = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		if (mv == null
				|| (access & (Opcodes.ACC_NATIVE & Opcodes.ACC_ABSTRACT)) > 0
				|| name.equals(Constants.CONSTRUCTOR)) {
			return mv;
		}
		return new InvokeMethodVisitor(className, access, name, desc,
				signature, exceptions, mv);
	}
}

class InvokeMethodVisitor extends MethodNode {
	private final String owner;
	private final MethodVisitor mv;
	private final ArrayList<MethodInsnNode> calls = new ArrayList<MethodInsnNode>();

	/**
	 * @param api
	 * @param mv
	 */
	public InvokeMethodVisitor(String owner, int access, String name,
			String desc, String signature, String[] exceptions, MethodVisitor mv) {
		super(Opcodes.ASM5, access, name, desc, signature, exceptions);
		this.owner = owner;
		this.mv = mv;

	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		super.visitMethodInsn(opcode, owner, name, desc, itf);
		if ((opcode == Opcodes.INVOKEVIRTUAL
				|| opcode == Opcodes.INVOKEINTERFACE) 
				&& name.equals("sayHello")) {
			calls.add((MethodInsnNode) instructions.getLast());
		}
	}

	public void visitEnd() {
		if (!calls.isEmpty()) {
			try {
				HashMap nodes = new LinkedHashMap();

				Analyzer a = new Analyzer(new SourceInterpreter());
				Frame[] frames = a.analyze(owner, this);
				for (MethodInsnNode methodInsnNode : calls) {
					Frame methodFrame = frames[instructions
							.indexOf(methodInsnNode)];
					int stackSlot = methodFrame.getStackSize();
					for (Type type : Type.getArgumentTypes(methodInsnNode.desc)) {
						stackSlot -= type.getSize();
					}
					SourceValue stackValue = (SourceValue) methodFrame
							.getStack(stackSlot -1 );
					Set<AbstractInsnNode> insns = stackValue.insns;
					for (AbstractInsnNode node : insns) {
						nodes.put(node, methodInsnNode.owner);
					}
				}

				// for (Map.Entry e : nodes.entrySet()) {
				// instructions.insert(e.getKey(), new
				// TypeInsnNode(Opcodes.CHECKCAST, e.getValue()));
				// }
			} catch (AnalyzerException ex) {
			}
		}
		accept(mv);
	}

}

// @SuppressWarnings("unused")
// private static void printSourcesAnalyzerResult(String name, MethodNode
// method,
// Analyzer a, final PrintWriter pw) {
// Frame[] frames = a.getFrames();
// Textifier t = new Textifier();
// TraceMethodVisitor mv = new TraceMethodVisitor(t);
//
// pw.println(name + method.desc);
// for (int j = 0; j < method.instructions.size(); ++j) {
// method.instructions.get(j).accept(mv);
//
// StringBuffer s = new StringBuffer();
//
// Frame f = frames[j];
// if (f == null) {
// s.append('?');
// } else {
// for (int k = 0; k < f.getStackSize(); ++k) {
// s.append("[ ");
// // for (AbstractInsnNode insn : f.getStack(k).)
// // s.append(method.instructions.indexOf(insn)).append(' ');
// s.append("] ");
// }
// }
// while (s.length() < method.maxStack + method.maxLocals + 1) {
// s.append(' ');
// }
// pw.print(Integer.toString(j + 100000).substring(1));
// pw.print(" " + s + " : " + t.text.get(t.text.size() - 1));
// }
// for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
// //method.tryCatchBlocks.get(j).accept(mv);
// //pw.print(" " + t.text.get(t.text.size() - 1));
// }
// pw.println();
// pw.flush();
// }
//
// }