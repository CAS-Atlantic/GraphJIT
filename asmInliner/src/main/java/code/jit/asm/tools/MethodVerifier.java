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

package code.jit.asm.tools;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;

public final class MethodVerifier extends MethodNode {

	private String _className;
	
    public MethodVerifier(MethodVisitor mv, String className) {
        super(Opcodes.ASM5);
        _className = className;
    }

    @Override
    public void visitEnd(){
    	 Analyzer a = new Analyzer(new BasicInterpreter());
         try {
             a.analyze(_className, this);
         } catch (AnalyzerException ex) {
             ex.printStackTrace();
         }
         accept(mv);
    }

}
