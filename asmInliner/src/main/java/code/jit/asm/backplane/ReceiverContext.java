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

import org.objectweb.asm.Type;

public class ReceiverContext {

	private Object _receiver;
	private boolean _isArrayElement;
	
	//Normal this should be the same as that of _receiver. The exception is for the array field Desc.
	private String _fieldType;
	
	public ReceiverContext(Object receiver, boolean isArrayEle){
		_receiver = receiver ;
		_isArrayElement = isArrayEle;
		_fieldType = Type.getInternalName(receiver.getClass());
	}
	
	/**
	 * 
	 * @param receiver
	 * @param isArrayEle
	 * @param receiverFieldType  might be equal to receiver's class, or not. For example 
	 * 
	 *                     MethodHAndle[]  a = {GWT, Filter}     //receiverFieldType = MethodHAndle[]
	 */
	public ReceiverContext(Object receiver, boolean isArrayEle, String receiverFieldType){
		_receiver = receiver ;
		_isArrayElement = isArrayEle;
		_fieldType = receiverFieldType;
	}
	
	public Object getReceiver(){
		return _receiver;
	}
	
	public boolean isArrayElement(){
		return _isArrayElement;
	}
	
	public void setFieldType(String desc){
		_fieldType = Type.getType(desc).getInternalName();
	}
	
	public String getFieldTypeInternalName(){
		return _fieldType;
	}
}
