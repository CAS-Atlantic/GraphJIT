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

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.slf4j.Logger;

import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.logging.GraphLogger;


/**
 *  This class is used to visit callees's method and inline variable 
 *  load/store instructions.
 *  
 *  It includes two tasks: 
 *    a) Popup stacked callee's arguments and store them to new indexes. 
 *    b) Renumber all variable indexes. 
 *    
 *  !!!Notice this class does not exert any Type Remapping. Therefore, inline the
 *  whole Callee's class is not supported yet. !!!!!  (RemappingMethodVisitor)
 *  
 *  Also, this class must be used with MethodCallInliner adapter
 *  
 * 
 * 
 */

/**
 * @author shijiex
 *
 */
public class InliningAdapter extends BaseInlineeMethodAdapter {

	private final LocalVariablesSorter lvs;
	private final Label end;

	private MethodContext _context;

	/**
	 * _callerstack: a stack that holds all existing arguments (method invocation arguments are excluded). 
	 * It is depressed. 
	 */
	private List<StackEle> __callerstacks = null; //new LinkedList<StackEle>();

	public InliningAdapter(LocalVariablesSorter lvsMV, int acc, String desc,
			Remapper remapper, Label end, MethodContext context) {

		super(acc, desc, lvsMV, remapper);
		lvs = lvsMV;
		mv = context.getRawMV();
		this.end = end;

		_logger.debug("Start inlining context:{} and MN_desc {}", context, desc);
		_context = context;
		_className = context.getOwnerName();
		//List<Type> types = _context.getOperandStack();
		int offset = ((acc & Opcodes.ACC_STATIC) != 0 ? 0 : 1);
		Type[] args = Type.getArgumentTypes(desc);
		for (int i = args.length - 1; i >= 0; i--) {
			super.visitVarInsn(args[i].getOpcode(Opcodes.ISTORE), i + offset);
		}

		int poped = args.length;
		if (offset > 0) {
			poped++;
			super.visitVarInsn(Opcodes.ASTORE, 0);
		}

//		int left = arhgs.size() - poped - 1;
//		while (left > 0) {
//			// NON-parameters in the stack => pop up too and restore back after
//			// complete.
//			int variable = newLocal(types.get(left));
//			int opcode = types.get(left).getOpcode(Opcodes.ISTORE);
//			__callerstacks.add(0, new StackEle(types.get(left), variable)); // |-->TOP
//			mv.visitVarInsn(opcode, variable);
//			left--;
//
//		}

	}

	@Override
    public void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc, final boolean itf) {
		//System.out.println("[Callee: ] invokeVirtual "+ owner +"  "+name+"  "+ desc);
		super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
	
	@Override
	public void visitInsn(int opcode) {
		if (Opcodes.RETURN >= opcode && opcode >= Opcodes.IRETURN || false /* athrow */) {
			// @FIXME pls also handle athrow exception
			_logger.debug("Inlining ends because of seeing {}, start restore caller stack now. {} ", opcode, _context.getOwnerName());
			restoreCallerStack(opcode);
			super.visitJumpInsn(Opcodes.GOTO, end);

		} else {
			super.visitInsn(opcode);
		}
	}

	@Override
	protected int newLocalMapping(Type type) {
		return lvs.newLocal(type);
	}

	@Override
	public void visitMaxs(int stack, int locals) {
	}

	private void restoreCallerStack(int opcode) {
		if(__callerstacks==null) return;  //Current below code is not executed..
//		if (this.__callerstacks.size() == 0)
//			return;

		int returnVariable = Opcodes.ISTORE;
		int loadOpcode = 0;
		switch (opcode) {
		case Opcodes.RETURN:
			break;
		case Opcodes.IRETURN:
		case Opcodes.FRETURN:
			loadOpcode = Opcodes.ISTORE;
			returnVariable = lvs.newLocal(Type.getType(int.class));
			mv.visitVarInsn(loadOpcode, returnVariable);
			loadOpcode = Opcodes.ILOAD;
			break;
		case Opcodes.ARETURN:
			loadOpcode = Opcodes.ASTORE;
			returnVariable = lvs.newLocal(Type.getType(Object.class));
			mv.visitVarInsn(Opcodes.ASTORE, returnVariable);
			loadOpcode = Opcodes.ALOAD;
			break;
		case Opcodes.DRETURN:
			loadOpcode = Opcodes.DSTORE;
			returnVariable = lvs.newLocal(Type.getType(double.class));
			mv.visitVarInsn(Opcodes.DSTORE, returnVariable);
			loadOpcode = Opcodes.DLOAD;
			break;
		case Opcodes.LRETURN:
			loadOpcode = Opcodes.LSTORE;
			returnVariable = lvs.newLocal(Type.getType(long.class));
			mv.visitVarInsn(Opcodes.LSTORE, returnVariable);
			loadOpcode = Opcodes.LLOAD;
			break;
		default:
			System.err.println("Some wrong Error Return instruction. !");
			System.exit(-1);
		}
		{
			// @FIXME The risk here is that the JVM stack might be not empty,
			// and this risk will results in corrupted stack and jitted code
			// Pls clean them before pushing cached stack elements here.
		}

//		for (int i = 0; i < __callerstacks.size(); i++) {
//			mv.visitVarInsn(
//					__callerstacks.get(i).getType().getOpcode(Opcodes.ILOAD),
//					__callerstacks.get(i).getLocalVariable());
//		}

		mv.visitVarInsn(loadOpcode, returnVariable);

	}

}

class StackEle {
	Type _type;
	int _variable;

	public StackEle(Type type, int variable) {
		_type = type;
		_variable = variable;
	}

	public Type getType() {
		return _type;
	}

	public int getLocalVariable() {
		return _variable;
	}
}

class BaseInlineeMethodAdapter extends RemappingMethodAdapter {

	protected static Logger _logger = GraphLogger.get(InliningAdapter.class);
	protected String _className;
	/**
	 * @param access
	 * @param desc
	 * @param mv
	 * @param remapper
	 */
	public BaseInlineeMethodAdapter(int access, String desc, MethodVisitor mv,
			Remapper remapper) {
		super(access, desc, mv, remapper);
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		super.visitVarInsn(opcode, var + firstLocal);
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		super.visitIincInsn(var + firstLocal, increment);
	}

	  @Override
      public void visitFrame(
          int type,
          int nLocal,
          Object[] local,
          int nStack,
          Object[] stack){
		 _logger.debug("inling adapt, type={}, nLocal={}", type, nLocal);
		 super.visitFrame(type, nLocal, local, nStack, stack);
	  }
	  
	@Override
	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		//This method will not be called with SKIP_DEBUG option.
		super.visitLocalVariable(name, desc, signature, start, end, index
				+ firstLocal);
	}
	
	@Override
    public void visitFieldInsn(int opcode, String owner, String name,
            String desc) {
		String newOwner = remapper.mapType(owner);
		String newName = remapper.mapFieldName(owner, name, desc);
        super.visitFieldInsn(opcode, owner , name, desc );
                
    }
	
    @Override
    public void visitEnd() {
        //Yes, it is also empty implementation.
    }
    
    @Override
    public void visitJumpInsn(int opcode, Label label) {
    	super.visitJumpInsn(opcode, label);
    }
    
    @Override
    public void visitLabel(Label label){
    	super.visitLabel(label);
    }
}
