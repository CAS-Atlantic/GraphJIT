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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import code.jit.asm.backplane.IVisitorID;
import code.jit.asm.backplane.InlineCode;
import code.jit.asm.backplane.MethodContext;
import code.jit.asm.backplane.ReceiverContext;
import code.jit.asm.common.utils.ReflectionUtil;
import code.jit.asm.services.ConfigurationService;
import code.jit.asm.services.InlineFilterService;

/**
 * @author shijiex
 *
 */
public class TypeInferencer extends MethodVisitor implements IVisitorID{

	MethodNode _node;
	String _className;
 
	Frame[] _frames;
	MethodContext _context;
	List<AbstractInsnNode> _invocations = new ArrayList<AbstractInsnNode>();
	
	int _pc =-1;
	private  Map<Class, IInference> _inferences = new LinkedHashMap<Class, IInference>();
		
	/**
	 * @param api
	 * @param mv
	 */
	public TypeInferencer(int api, MethodVisitor mv, MethodNode node, MethodContext context) {
		super(api, mv);
		_className = context.getOwnerName();
		_node = node;
		_context = context;
		Analyzer _analyzer = new Analyzer(new SourceInterpreter());
		try {
			_frames = _analyzer.analyze(_className, node);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		// Index all invokevirtual
		for(AbstractInsnNode instr : node.instructions.toArray()){
			switch(instr.getOpcode()){
				case Opcodes.INVOKEVIRTUAL:
					_invocations.add(instr);
					break;
				case Opcodes.GETFIELD:
					if(ConfigurationService.get().INLINE_CODE == InlineCode.CLASS_INLINE){
						_invocations.add(instr);
					}
					break;
				default:
					
			}
		}
		
		

		_inferences.put(IntInsnNode.class,  new TypeInferencer.IntInsnInference());
		_inferences.put(InsnNode.class,  new TypeInferencer.InsnInference());
		_inferences.put(FieldInsnNode.class,  new TypeInferencer.FieldInference());
		//_inferences.put(FieldNode.class,  new TypeInferencer.FieldInference());
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		super.visitMethodInsn(opcode, owner, name, desc, itf);
		
		if(opcode==Opcodes.INVOKEVIRTUAL){
			_pc++;
			int index = _node.instructions.indexOf(_invocations.get(_pc));
			//_frame［index］ == null ==> inaccessible instruction. 
			if(index==-1 || _frames[index] == null ||
					!InlineFilterService.get().isInvocationInlineableCallSite(owner, name, desc)) {
				_context.setInvocationReceiver(null);
				return;	
			} 
			
			Type descType = Type.getType(desc);
			int receiverIndex = _frames[index].getStackSize() - descType.getArgumentTypes().length;
			SourceValue value = (SourceValue)_frames[index].getStack(receiverIndex-1);
			inference(value);
		}
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc){
		super.visitFieldInsn(opcode, owner, name, desc);
		
		if((opcode == Opcodes.GETFIELD || Opcodes.PUTFIELD == opcode) && 
				ConfigurationService.get().INLINE_CODE == InlineCode.CLASS_INLINE
				&& owner.equals(_context.getOwnerName())){
			_pc++;
			int index = _node.instructions.indexOf(_invocations.get(_pc));
			AbstractInsnNode node = _node.instructions.get(index);
			SourceValue value = (SourceValue) _frames[index].getStack(_frames[index].getStackSize()-1);
			String mappedField = null;
			if(node instanceof FieldInsnNode && value.size == 1 ){
				FieldInsnNode fnode = (FieldInsnNode) node;
				//The previous instruction should be aload 0. 
				for( Object dependence: value.insns){
					if(dependence instanceof VarInsnNode){
						VarInsnNode varNode = (VarInsnNode)dependence;
						if(varNode.var == 0 ){ //this
							_context.setFieldNameRewrittenAble(true);
							return;
						}
					}
				} 
				
			}
			
		}
		_context.setFieldNameRewrittenAble(false);
	}
	
	
	private void inference(SourceValue value){
		if(value.size != 1){
			System.err.println("[TypeInferencer] sees some unexpected value "+ value.toString());
		}
		
		Iterator<AbstractInsnNode> iter = value.insns.iterator();
		while(iter.hasNext()){
			AbstractInsnNode node = iter.next();
			ReceiverContext receiver = null;
			if(_inferences.containsKey(node.getClass())){
				receiver = _inferences.get(node.getClass()).handle(node);
			}
			_context.setInvocationReceiver(receiver);
		}
		
	}

	interface IInference{
		public ReceiverContext handle(AbstractInsnNode node);
	}


	private class IntInsnInference implements IInference{

		/* (non-Javadoc)
		 * @see code.jit.asm.core.TypeInferencer.IInference#handle(org.objectweb.asm.tree.AbstractInsnNode)
		 */
		@Override
		public ReceiverContext handle(AbstractInsnNode node) {
			if (node instanceof IntInsnNode) {
				IntInsnNode intNode = (IntInsnNode) node;
				switch (intNode.getOpcode()) {
				case Opcodes.BIPUSH:
				case Opcodes.SIPUSH:
					return new ReceiverContext(intNode.operand, false);
				default:
					System.err.println("You should not seen this opcode"
							+ node.getOpcode() + "in the IntInsnInference");
				}
			}
			return null;
		}
		
	}
	private class FieldInference implements IInference{

		/* (non-Javadoc)
		 * @see code.jit.asm.core.TypeInferencer.IInference#handle()
		 */
		@Override
		public ReceiverContext handle(AbstractInsnNode aNode) {
			FieldInsnNode node = (FieldInsnNode) aNode;
			
			if(node.getOpcode() == Opcodes.GETFIELD && node.owner.equals(_className))
//					&& _context.getClassContext().isFieldInlineable(node.owner, node.name, node.desc))
			{
				/**
				 *  A more safe way is to look the operand on the stack to determine which receiver of getField is on. 
				 *  e.g.,  aload 0;  getField 
				 *         aload 1;  getField .. 
				 *  Here I only simply assume that the getField only applys to 0 (this). 
				 */
				Object fieldObject = ReflectionUtil.getFieldObject(_context.getClassContext().getOwner(), node.name);
				_context.setReceiverFieldName(node.name);
				ReceiverContext context = new ReceiverContext(fieldObject, false); 
				context.setFieldType(node.desc);
				return context;
			}
			return null;
//			int index = _node.instructions.indexOf(node);
//			if(index!=-1){
//				Frame frame = _frames[index];
//				if(frame!=null){
//					
//				}
//			}
			
			
		}
	}
	
//	private class VarInsnInference implements IInference{
//
//		/* (non-Javadoc)
//		 * @see code.jit.asm.core.TypeInferencer.IInference#handle(org.objectweb.asm.tree.AbstractInsnNode)
//		 */
//		@Override
//		public Object handle(AbstractInsnNode node) {
//			return null;
//			
//		}
//		
//	}
//	
//	private class MethodInsnInference implements IInference{
//
//		/* (non-Javadoc)
//		 * @see code.jit.asm.core.TypeInferencer.IInference#handle(org.objectweb.asm.tree.AbstractInsnNode)
//		 */
//		@Override
//		public Object handle(AbstractInsnNode node) {
//			return null;
//			
//		}
//	}
	
	private class InsnInference implements IInference{

		/* (non-Javadoc)
		 * @see code.jit.asm.core.TypeInferencer.IInference#handle(org.objectweb.asm.tree.AbstractInsnNode)
		 */
		@Override
		public ReceiverContext handle(AbstractInsnNode anode) {
			InsnNode node = (InsnNode)anode;
			switch(node.getOpcode()){
			case Opcodes.AALOAD:
				Frame frame = _frames[_node.instructions.indexOf(node)];
				ArrayTuple tuple = new ArrayTuple();
				for(int i=2; i>0;i--){
					SourceValue top = (SourceValue) frame.getStack(frame.getStackSize()-i);
					Iterator<AbstractInsnNode> iter = top.insns.iterator();
					while(iter.hasNext()){
						AbstractInsnNode inst = iter.next();
						ReceiverContext myObject = _inferences.get(inst.getClass()).handle(inst); 
						
						if(myObject.getReceiver() instanceof Integer){
							tuple.setIndex((Integer)myObject.getReceiver());
						}else{
							tuple.setReceiver(myObject.getReceiver());
							tuple.setFieldType(myObject.getFieldTypeInternalName());
						}
					}
				}
				return tuple.eval();
				
			case Opcodes.ICONST_0:
				return adaptToReceiverContext(0);
			case Opcodes.ICONST_1:
				return adaptToReceiverContext(1);
			case Opcodes.ICONST_2:
				return adaptToReceiverContext(2);
			case Opcodes.ICONST_3:
				return adaptToReceiverContext(3);
			case Opcodes.ICONST_4:
				return adaptToReceiverContext(4);
			case Opcodes.ICONST_5:
				return adaptToReceiverContext(5);
			default: 
					System.err.print("You should not see this one in the InsnInference..");
			}
			return null;
		}
		
		private ReceiverContext adaptToReceiverContext(int operand){
			return new ReceiverContext(operand, false);
		}
		
	}

	@Override
	public String getVisitorName() {
		return TypeInferencer.class.getName();
	}
	

}


class ArrayTuple {
	private int _index;
	private Object[] _obj;
	
	private String _receiverFieldType;
	
	public ArrayTuple(){
	}
	
	public void setFieldType(String fieldTypeInternalName) {
		_receiverFieldType = fieldTypeInternalName;
	}

	public void setIndex(int index){
		_index = index;
	}
	
	public void setReceiver(Object obj){
		if(obj instanceof Object[]){
			_obj = (Object[])obj;	
		}else{
			System.err.println("Set non-array object to the Object[]");
		}
	}
	
	public ReceiverContext eval(){
		return new ReceiverContext(_obj[_index], true, _receiverFieldType);
	}
	
	
	
}


