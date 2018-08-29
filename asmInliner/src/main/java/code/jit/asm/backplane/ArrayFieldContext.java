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

import java.util.Collection;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import code.jit.asm.common.utils.ReflectionUtil;
import code.jit.asm.services.BytecodeCacheService;

public class ArrayFieldContext implements IFieldContext{

	private FieldContext[] _fieldContexts;
	private String _fieldOwner;
	private Type _fieldType;
	private String _fieldName;
	private ClassContext _classContext;
	
	private Object[] _transformedObjs;
	
	private boolean _eraseField = true;
	public ArrayFieldContext(String fieldOwner,  String fieldName, Type fieldType, Object[] transformedObjs, ClassContext context){
		
		_fieldContexts = new FieldContext[transformedObjs.length];
		_fieldOwner = fieldOwner;
		_fieldType = fieldType;
		_fieldName = fieldName;
		_classContext = context;
		_transformedObjs = transformedObjs;
		
		for(int i=0; i< transformedObjs.length; i++){
			Object obj = transformedObjs[i];
			if(obj == null) {
				_eraseField = false;
				continue;
			}
			String typeName = obj.getClass().getName();
			BytecodeResource resource = BytecodeCacheService.get().getBcClass(typeName);
			if(resource==null) {
				System.err.println("Something error might happen.. The "+typeName+" should be cache-hit in most times (Purging??)");
				_fieldContexts[i] = null;
			 }else{
				 _fieldContexts[i] = new FieldContext(obj.getClass(),_fieldName+i, resource.getTargetFields(), null, obj, context);
			 }
				
		}
			
	}

	@Override
	public void setupNewReceiver(Object newObj) {
		if(!_eraseField){
			try {
				ReflectionUtil.setFieldObject(newObj, _fieldName, _transformedObjs);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			
		}
	}

	@Override
	public Object getReceiver() {
		return null;
	}

	@Override
	public void accept(ClassNode cw) {
		for(FieldContext context: _fieldContexts){
			if(context!=null){
				context.accept(cw);	
			}
		}
	}

	@Override
	public String mapFieldName(String owner, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putFields(MethodVisitor mv, Type getFieldOwner, int variable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void populateMap(Map<String, String> map) {
		for(FieldContext context : _fieldContexts){
			if(context!=null){
				context.populateMap(map);
			}
		}
	}

	@Override
	public Collection getMappedNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFieldErased(){
		return _eraseField;
	}

}
