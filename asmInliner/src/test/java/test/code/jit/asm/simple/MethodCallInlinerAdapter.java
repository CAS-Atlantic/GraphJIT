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

/**
 * @author shijiex
 *
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.objectweb.asm.commons.TryCatchBlockSorter;
import org.objectweb.asm.tree.MethodNode;

public abstract class MethodCallInlinerAdapter extends LocalVariablesSorter {

    /*
     * Only used if the inlineFrames option is true. Allows the computation of
     * the stack map frames at any instruction of the caller method, and in
     * particular at the call sites to methods to be inlined (needed to merge
     * this caller stack maps with the callee ones).
     */
    private AnalyzerAdapter analyzerAdapter;

    /*
     * If a method to be inlined is called several times from the same caller
     * method, we don't want to allocate new local variables for each
     * invocation. Instead, we want to share the local slots between all call
     * sites. To do this we need to reuse the InliningAdapter instance, which
     * maintains (as a LocalVariablesSorter subclass) the mapping from the
     * original locals in the method to be inlined to the locals in the caller
     * method. This is the goal of this map.
     */
    private Map<String, InlinedMethod> inliners;

    public static class InlinedMethod {

        // The code that must be inlined.
        public final MethodNode method;

        // How the code must be remapped to be inlined.
        public final Remapper remapper;

        InliningAdapter inliner;

        public InlinedMethod(final MethodNode method, final Remapper remapper) {
            this.method = method;
            this.remapper = remapper;
        }
    }

    static InlinedMethod DO_NOT_INLINE = new InlinedMethod(null, null);

    // If inlineFrame is true, frames from the calling method and from
    // the inlined methods are merged during inlining, thus avoiding the
    // need to compute all frames from scratch. But this requires both
    // methods to already have computed frames.
    // If inlineFrame is false the frames are not changed at all, thus
    // they must be computed from scratch in the ClassWriter.
    public MethodCallInlinerAdapter(
        String owner,
        int access,
        String name,
        String desc,
        MethodVisitor next,
        boolean inlineFrames)
    {
        this(Opcodes.ASM5, owner, access, name, desc, next, inlineFrames);
    }

    protected MethodCallInlinerAdapter(
        int api,
        String owner,
        int access,
        String name,
        String desc,
        MethodVisitor next,
        boolean inlineFrames)
    {
        super(api, access, desc, getNext(owner,
                access,
                name,
                desc,
                next,
                inlineFrames));
        this.analyzerAdapter = inlineFrames ? (AnalyzerAdapter) mv : null;
    }

    private static MethodVisitor getNext(
        String owner,
        int access,
        String name,
        String desc,
        MethodVisitor next,
        boolean inlineFrames)
    {
        MethodVisitor mv = new TryCatchBlockSorter(next,
                access,
                name,
                desc,
                null,
                null);
        if (inlineFrames) {
            mv = new AnalyzerAdapter(owner, access, name, desc, mv);
        }
        return mv;
    }

    @Override
    public void visitMethodInsn(
        int opcode,
        String owner,
        String name,
        String desc, boolean itf)
    {
    	System.out.println("Calling the method invocation instruction "+ owner+"  "+name);
        InlinedMethod inliner = getInliner(owner, name, desc);
        if (inliner == DO_NOT_INLINE) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        if (inliner.inliner == null) {
            MethodVisitor mv = this.mv;
            if (analyzerAdapter != null) {
                mv = new MergeFrameAdapter(api, analyzerAdapter, mv);
            }
            int access = opcode == Opcodes.INVOKESTATIC
                    ? Opcodes.ACC_STATIC
                    : 0;
            inliner.inliner = new InliningAdapter(api,
                    access,
                    desc,
                    new SubLocalVariableSorter(Opcodes.ASM5, access, desc,this),
                     mv,
                    inliner.remapper);

        }
        inliner.method.accept(inliner.inliner);
    }
    
    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack,
            Object[] stack){
    	System.out.println("Caller's visitFrame ");
    	super.visitFrame(type, nLocal, local, nStack, stack);
    }

    protected abstract InlinedMethod mustInline(
        String owner,
        String name,
        String desc);

    private InlinedMethod getInliner(String owner, String name, String desc) {
        if (inliners == null) {
            inliners = new HashMap<String, InlinedMethod>();
        }
        String key = owner + "." + name + desc;
        InlinedMethod method = inliners.get(key);
        if (method == null) {
            method = mustInline(owner, name, desc);
            if (method == null) {
                method = DO_NOT_INLINE;
            } else {
                method.method.instructions.resetLabels();
            }
            inliners.put(key, method);
        }
        return method;
    }

    static class InliningAdapter extends RemappingMethodAdapter {

        private int access;

        private String desc;

        private final SubLocalVariableSorter caller;

        private Label end;

        public InliningAdapter(
            int api,
            int access,
            String desc,
            SubLocalVariableSorter caller,
            MethodVisitor next,
            Remapper remapper)
        {
            super(api, access, desc, next, remapper);
            this.access = access;
            this.desc = desc;
            this.caller = caller;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            int off = (access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
            Type[] args = Type.getArgumentTypes(desc);
            for (int i = args.length - 1; i >= 0; i--) {
                visitVarInsn(args[i].getOpcode(Opcodes.ISTORE), i + off);
            }
            if (off > 0) {
                visitVarInsn(Opcodes.ASTORE, 0);
            }
            /*
             * TODO: if the method to be inlined contains try catch blocks, we
             * need to save the full stack, and not only the method call
             * arguments (and we need to restore the full stack in visitMaxs).
             * Indeed if an exception is thrown and catched in the inlined
             * method, this will clear not only the inlined method stack, but
             * also the caller method's stack (while it is not the case in the
             * original, non-inlined code).
             *
             * Problem: to save the full stack, we need to know its size and the
             * types it contains, which means that we need already computed
             * frames for the caller method. In other words the
             * inlineFrames=false mode is not usable when inlined methods can
             * contain try catch blocks, unless they are never called inside
             * expressions (worse, this condition cannot even be checked during
             * inlining if we don't have the caller frames, which could have been
             * useful to keep the original code unchanged in this case).
             */
            end = new Label();
            System.out.println("++"+end.toString());
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                super.visitJumpInsn(Opcodes.GOTO, end);
            } else {
                super.visitInsn(opcode);
            }
        }

        @Override
        public void visitVarInsn(final int opcode, final int var) {
            // Offset all locals by firstLocal to also remap the method
            // arguments
            super.visitVarInsn(opcode, var + firstLocal);
        }

        @Override
        public void visitIincInsn(final int var, final int increment) {
            // Offset all locals by firstLocal to also remap the method
            // arguments
            super.visitIincInsn(var + firstLocal, increment);
        }
        
        @Override
        public void visitMethodInsn(final int opcode, final String owner,
                final String name, final String desc, final boolean itf){
        	super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitLocalVariable(
            final String name,
            final String desc,
            final String signature,
            final Label start,
            final Label end,
            final int index)
        {
            // Offset all locals by firstLocal to also remap the method
            // arguments
            super.visitLocalVariable(name, desc, signature, start, end, index
                    + firstLocal);
        }

        @Override
        public void visitMaxs(int stack, int locals) {
            super.visitLabel(end);
            System.out.println("--"+end.toString());
        }

        @Override
        public void visitEnd() {
            // Do nothing
        }

        @Override
        protected int newLocalMapping(final Type type) {
            return caller.newLocalMapping(type);
        }
    }

    static class MergeFrameAdapter extends MethodVisitor {

        private AnalyzerAdapter analyzerAdapter;

        public MergeFrameAdapter(
            int api,
            AnalyzerAdapter analyzerAdapter,
            MethodVisitor next)
        {
            super(api, next);
            this.analyzerAdapter = analyzerAdapter;
        }

        @Override
        public void visitFrame(
            int type,
            int nLocal,
            Object[] local,
            int nStack,
            Object[] stack)
        {
            List<Object> callerLocal = analyzerAdapter.locals;
            int nCallerLocal = callerLocal == null ? 0 : callerLocal.size();
            int nMergedLocal = Math.max(nCallerLocal, nLocal);
            Object[] mergedLocal = new Object[nMergedLocal];
            for (int i = 0; i < nCallerLocal; ++i) {
                if (callerLocal.get(i) != Opcodes.TOP) {
                    mergedLocal[i] = callerLocal.get(i);
                }
            }
            for (int i = 0; i < nLocal; ++i) {
                if (local[i] != Opcodes.TOP) {
                    mergedLocal[i] = local[i];
                }
            }
            List<Object> callerStack = analyzerAdapter.stack;
            int nCallerStack = callerStack == null ? 0 : callerStack.size();
            int nMergedStack = nCallerStack + nStack;
            Object[] mergedStack = new Object[nMergedStack];
            for (int i = 0; i < nCallerStack; ++i) {
                mergedStack[i] = callerStack.get(i);
            }
            if (nStack > 0) {
                System.arraycopy(stack, 0, mergedStack, nCallerStack, nStack);
            }
            super.visitFrame(type,
                    nMergedLocal,
                    mergedLocal,
                    nMergedStack,
                    mergedStack);
        }
    }
}

class SubLocalVariableSorter extends LocalVariablesSorter{

	/**
	 * @param api
	 * @param access
	 * @param desc
	 * @param mv
	 */
	protected SubLocalVariableSorter(int api, int access, String desc,
			MethodVisitor mv) {
		super(api, access, desc, mv);
	}
	
	
	public int newLocalMapping(final Type type){
		return super.newLocalMapping(type);
	}
	
}