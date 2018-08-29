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

package code.jit.asm.backplane;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;

import code.jit.asm.common.Blob;
import code.jit.asm.common.utils.Configs;
import code.jit.asm.services.ConfigurationService;
import code.jit.asm.services.InlineFilterService;

public class MethodContext {

	//private List<Type> _operandStacks = new LinkedList<Type>();
	
	private boolean _inlining = false;

	private MethodVisitor _mv;
	
	private String _receiverFieldName;
	
	private boolean _visitConstructor = false;
	
	private ClassContext _classContext;
	
	public MethodContext(ClassContext context, String methodName, String desc){
		_classContext = context;
	}
	
//	public void stackSnapshot(List<Object> stack){
//		_operandStacks.clear();
//		for(int i=stack.size() -1; i>=0; i--){
//			Object obj = stack.get(i);
//			if(obj instanceof Integer){
//				int type = (Integer) obj;
//				switch(type){
//				case 1:
//					_operandStacks.add(0, Type.INT_TYPE); //1 integer
//					break;
//				case 2:
//					_operandStacks.add(0, Type.FLOAT_TYPE); //2 float
//					break;
//				case 0: 
//					if(i-1>=0){
//						i--;
//						type = (Integer)stack.get(i);  //MUST be..
//						if(type==3){
//							_operandStacks.add(0, Type.DOUBLE_TYPE);
//						}else if(type==4){
//							_operandStacks.add(0, Type.LONG_TYPE);
//						}else{
//							System.err.println(" JVM Stack Wrong data..!");
//						}
//					}
//					break;
//				case 3:
//				case 4:
//				case 5:
//				case 6:
//					System.err.println(" Compiler Stack does not support NULL or UNINITIALIZED_THIS currently");
//				default:
//						throw new IllegalArgumentException();		
//				}
//				//System.out.println(i+" Data type in the stack "+type);
//			}else if(obj instanceof String){
//				//@FIXME It is Array or other type.. 
//				_operandStacks.add(0, Type.getType(obj.toString()));
//			}else{
//				 throw new IllegalArgumentException();
//			}
//		}
//	}
	
	public void beginInline(){
		_inlining = true;
		_visitConstructor = false;
	}
	
	public void endInline(){
		_callee = null;
		_inlining = false;
		//_operandStacks.clear();
		_visitConstructor = false;
	}
	
	public boolean inlining(){
		return _inlining;
	}
	
//	public int getStackSize(){
//		return _operandStacks.size();
//	}

	
	
	/**
	 *  In oder to handle the Array Object, we track the past instrs during execution. 
	 *  However, this is RISKY for holding this status here. 
	 *  
	 *           RISKLY..
	 *           
	 *   We will replace it with SourceInerpreter in another branch. 
	 * 
	 *  ASsume this is in order: 
	 *     ALOAD 
	 *     getField [] 
	 *     BIPUSH x
	 *     AALOAD 
	 */
	             
//	private boolean _seenLastFieldMember;
//	private boolean _startTrack = false;
//	private int _arrayIndex = -1;
//	private String _lastArrayFieldMember;
//	public void executeInst(int opcode, int value, String owner, String name, String desc){
//		IPlugin plugin = NameMappingService.get().getPlugin();
//		switch(opcode){
//		case Opcodes.AALOAD:
//			if(_startTrack){
//				ClassContext clsContext = getClassContext();
//				Object obj = clsContext.getReceiver();
//				Object arrField = ReflectionUtil.getFieldObject(obj, getLastFieldName());
//				if(arrField instanceof Object[]){
//					Object targetField = ((Object[])arrField)[_arrayIndex];
//					//Reset the last type to the real Receiver. 
////					int len = stack.size();
////					
////					//Push type stack.
////					_operandStacks.set(len-1, Type.getType(targetField.getClass()));
//				}
//				
//			}
//			
//			_seenLastFieldMember = false;
//			_startTrack = false;
//			
//			break;
//		case Opcodes.BIPUSH:
//			if(_startTrack){
//				_arrayIndex = value;
//			}
//			break;
//		case Opcodes.GETFIELD:
//			if(Type.getType(desc).getSort() == Type.ARRAY && plugin.track(desc)){
//				// Track the array object..
//				_startTrack = true;
//				_lastArrayFieldMember = name;
//			}else{
//				_seenLastFieldMember = true;
//				_startTrack =false;	
//			}
//			
//			break;
//			default:
//				_startTrack = false;
//		}
//		return ;
//	}
	
	/**
	 *  
	 * @return opcodes list
	 */
//	public List<Type> getOperandStack(){
//		return _operandStacks;
//	}
//	
	
	public MethodVisitor getRawMV(){
		return _mv;
	}
	
	public void setRawMV(MethodVisitor mv){
		_mv = mv;
	}
	
	
	public String getOwnerName(){
		return _classContext.getOriginalClassNode().name;
	}

	private String _callee;

	/**
	 * @param owner
	 */
	public void setCallee(String owner) {
		_callee = owner;
	}
	
	public String getCallee(){
		return _callee;
	}


	/**
	 * 
	 * Map a XXXfield in the Callee (owner) to a new Name in the Caller.
	 * 
	 * @param owner: The owner when 
	 * @param name
	 * @param desc
	 * @return
	 */
	public String map2NewName(String owner, String name, String desc) {
		// TODO Auto-generated method stub
		return name;
	}

	/**
	 * @return
	 */
	public ClassContext getClassContext() {
		return _classContext;
	}
	
	
	public void visitCalleeSuperInstVisited(){
		_visitConstructor = true;
	}
	
	public boolean isCalleeSuperInstVisited(){
		return _visitConstructor;
	}

	/**
	 * @return
	 */
	public int getInstIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	private ReceiverContext _invocationReceiver;
	/**
	 * @param object
	 */
	public void setInvocationReceiver(ReceiverContext object) {
		_invocationReceiver = object;
		
	}
	
	public ReceiverContext getInvocationReceiver(){
		return _invocationReceiver;
	}
	
	public void setReceiverFieldName(String fieldName){
		_receiverFieldName = fieldName;
	}
	
	public String getReceiverFieldName(){
		return _receiverFieldName;
	}

	
	private boolean _enableFieldRename = false;
	/**
	 * 
	 */
	public void setFieldNameRewrittenAble(boolean flag) {
		_enableFieldRename = flag;
	}

	/**
	 * 
	 */
	public boolean isFieldNameRenameEnabled() {
		return _enableFieldRename;
		
	}
	
	private Blob _inliningBlob;
	public void setBlob(Blob mapped) {
		_inliningBlob = mapped;
	}
	
	public Blob getInliningBlob(){
		return _inliningBlob;
	}

	private Printer _printer;
	public void setPrint(Printer printer) {
		_printer = printer;
	}
	
	public void dumpTrace(){
		if(Configs._enableDump){
			StringWriter sw = new StringWriter();
			_printer.print(new PrintWriter(sw));
			_printer.getText().clear();
			System.err.println(sw.toString());
		}
	}

	/**
	 *  Check whether the invocationReceiver is inlinable. 
	 *  Return true if it is Class_Inline mode and invocationReceiver is    
	 *   
	 * @return
	 */
	public boolean isInvocationReceiverFilted() {
		if(_invocationReceiver == null || _invocationReceiver.getReceiver() == null) return true;
		if(ConfigurationService.get().INLINE_CODE == InlineCode.CLASS_INLINE){
			if(_invocationReceiver.isArrayElement() && 
					InlineFilterService.get().isFieldTypeConfiguredSkipInlining(
							Type.getType(_invocationReceiver.getFieldTypeInternalName()).getClassName()))  return true;   
		}
		
		return false;
	}
	

}
