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
import org.objectweb.asm.Type;

import code.jit.asm.backplane.MethodContext;
import code.jit.asm.services.InlineFilterService;

/**
 * @author shijiex
 *
 */
public class ConstructorMerge extends MethodCallInliner {

	
	/**
	 * @param api
	 * @param context
	 */
	public ConstructorMerge(int access, String desc, MethodContext context) {
		super(access, desc, context);
	}

	
	/**
	 *  invokeSpecial/interface in the constructor, and I do not want to do anything in the Caller. 
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		_context.getRawMV().visitMethodInsn(opcode, owner, name, desc, itf);
		
		//I do not want to inline any method in the Constructor.. Uncomment below and modify it 
		// if you want suppport inlining in the Constrcutor 
		
//		if(!_context.inlining()){
//			if(opcode == Opcodes.INVOKESPECIAL){
//				_context.getRawMV().visitMethodInsn(opcode, owner, name, desc, itf);  //visit it..
//			}
//			return;
//		}else{
//			
//			if(opcode==Opcodes.INVOKESPECIAL && owner.equals(_context.getCallee()) && name.equals(Constants.EMPTY_DESC)){
//				_context.visitCalleeSuperInstVisited();   //set the boolean flag here.
//				return ;
//			}
//		}
//		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}
	
//	@Override
//	public void visitVarInsn(final int opcode, final int var) {
//		if(_context.inlining() && !_context.isCalleeSuperInstVisited()){
//			return ;
//		}
//		super.visitVarInsn(opcode, var);
//	}
	

	/**
	 *  @ASSUMPTION: 1, Receivers of getField in the Constructor is only applied to the constructor parameters.
	 *                  We never do any changes on the getField in Constructor. 
	 *               2, In contrast to the GetField, target of putField is only applied to the field members of current class. 
	 *                  In other means, inline callee's constructor for each putField instruction here. 
	 *               3, None of changes on the getStatic/putStatic. 
	 *               
	 *  @TODO  It is a correct way to do it by lexical analysis, and this is left for further task.
	 *  
	 *  
	 *  
	 *  Case 1: 
	 *       aload 0
	 *       aload X
	 *       putField this a  MyClass 
	 *  
	 *  =>   astore Y
	 *       {
	 *           aload Y 
	 *           getField MyClass field1 
	 *           aload Y 
	 *           getField MyClass field2
	 *           ...  
	 *       }
	 *       
	 *       //below are MyClass Constructor
	 *       {
	 *           //popup parameters
	 *           aStore 
	 *           aStore ..
	 *           
	 *           bytecode sequence. 
	 *           
	 *       }
	 *        
	 *  
	 *  happy for it! just inline. 
	 *  
	 *  Case 2: 
	 *       aload 0
	 *       getField this a MyClass
	 *       aload X
	 *       putField MyClass a_a YourClass
	 *   This is TODO
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		
		Type top = Type.getType(desc);
		
		if(!InlineFilterService.get().isTypeInlineable(top) ){
			_context.getRawMV().visitFieldInsn(opcode, owner, name, desc);
			return ;
		}
		
		int sort = top.getSort();
		if(_context.inlining() == false && opcode == Opcodes.PUTFIELD &&
				(sort == Type.OBJECT || sort == Type.ARRAY) 
				){

		   switch(sort){
		   	case Type.OBJECT:
		   		objectPutFieldHandle(owner, top, name);
			   break;
		   case Type.ARRAY:
			   //@TODO
			   System.err.println("Sorry, I have not complete this routine.");
			   break;
			default: 
				   System.err.println("I have not done this rotine for putField instruction!");
		   }
			
			return ;
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		_context.getRawMV().visitMaxs(-1, -1);
	}
	
	
	private void objectPutFieldHandle(String owner, Type top, String currentFieldName){
		int variable = super.newLocal(top);
   		
   		/**
   		 *  @ASSUMPTION: the resource must be in the Cache..Handle if it is purged or other unknown reason.
   		 *               1, The order of the fields in class definition is the same as desc in the constructor. 
   		 *               
   		 *  @NOTICE: 
   		 *          I am using the originalClassNode (un-optimized Class )  here to simulate the invocation of the Callee's constructor.
   		 *          
   		 */
   		
   		MethodVisitor fieldMemberVisitor = _context.getRawMV();
   		fieldMemberVisitor.visitVarInsn(top.getOpcode(Opcodes.ISTORE), variable);
   		fieldMemberVisitor.visitInsn(Opcodes.POP);   //Is pop2 possible?  It is 0
   		_context.getClassContext().putFields(fieldMemberVisitor, variable, top, currentFieldName);
   		
   		/**
   		 *  One thing we need keep mind is that resource.getFields matches the _cachedField() ’top.getClassName()‘ in the Context here.
   		 *  
   		 *  That is: resource.getFields() = _context._cacheFields[name].__inlineeFields
   		 *  
   		 */
	}
	
}
