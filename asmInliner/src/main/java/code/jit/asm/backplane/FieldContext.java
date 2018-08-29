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
package code.jit.asm.backplane;


import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import code.jit.asm.common.utils.Constants;
import code.jit.asm.common.utils.ReflectionUtil;
import code.jit.asm.services.BytecodeCacheService;


/**
 * 
 *  
 *  This class maintains the an Inlinee's fields. 
 *  It is indexed by the hosted field name
 * 
 * @author shijiex
 *
 */

public class FieldContext implements IFieldContext{

	
	//Inlinee's field nodes
	Map<String, InlineeField> _inlineeFields = new LinkedHashMap<String, InlineeField>();
	
	//private String _internalName;  //After transformation.
	String _fieldHoster;   //The fieldName in the hoster class.
	
	Set<MethodNode> _methods = new LinkedHashSet<MethodNode>();
	
	private String _owner;
	
	private Object _fieldObject;
	

	public FieldContext(String fieldName, BytecodeResource resource, Type newOwner, Object fieldObject, ClassContext context){

		//_internalName = resource.getOriginalClassNode().name;
		_fieldHoster = fieldName;
		_fieldObject = fieldObject;
		
		if(resource!=null){
			for(FieldNode field: resource.getTargetFields()){
				_inlineeFields.put(field.name, new InlineeField(field, _fieldHoster, context));
			}	
		}
		
		_owner = context.getClassName();
	}
	
	public FieldContext(Class<? extends Object> class1, String string,
			List<FieldNode> targetFields, Object object, Object obj,
			ClassContext context) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString(){
		BytecodeResource resource = BytecodeCacheService.get().getBcClass(_fieldObject);
		String internalName = "";
		if(resource!=null){
			internalName = resource.getOriginalClassNode().name; 
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("name:").append(internalName).append(",");
//		for(Set<ClassElement> node: _fields.values()){
//			buffer.append(node.toString()).append(",");
//		}
//		buffer.append("]");
		return buffer.toString();
	}
	
	@Override
	public void accept(ClassNode classNode){
		for(Map.Entry<String, InlineeField> entry: _inlineeFields.entrySet()){
			String key = entry.getKey();
			InlineeField inlineeEle = entry.getValue();
			Object receiver = ReflectionUtil.getFieldObject(_fieldObject, key);
			inlineeEle.accept(classNode, _fieldHoster, receiver);
		}
		
	}

	/**
	 * @param owner: Onwer of the Callee.
	 * @param name: field name in the Callee
	 * @return
	 */
	@Override
	public String mapFieldName(String owner, String name) {
		return _inlineeFields.get(name).calculateName();
	}

	/**
	 * @param mv
	 * @param variable
	 * @param fieldType: MyMH in the sample code.
	 */
	@Override
	public void putFields(MethodVisitor mv, Type getFieldOwner, int variable) {
		/**
		 *  The main concern here is for ClassField 
		 *     Class X {
		 *          MH  o;
		 *          
		 *          X(MYMH b){
		 *              o=b;
		 *          }
		 *     }
		 *  
		 */

		for(Entry<String, InlineeField> entry: _inlineeFields.entrySet()){
			String fieldName = entry.getKey();
			InlineeField element = entry.getValue();
			
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(getFieldOwner.getOpcode(Opcodes.ILOAD), variable);
			mv.visitFieldInsn(Opcodes.GETFIELD, getFieldOwner.getInternalName(), fieldName, element.desc);
			mv.visitFieldInsn(Opcodes.PUTFIELD, _owner, element.calculateName(), element.desc);
		} 
	}

	/**
	 * @param map
	 */
	@Override
	public void populateMap(Map<String, String> map) {
		BytecodeResource resource = BytecodeCacheService.get().getBcClass(_fieldObject);
		String internalName = "";
		if(resource!=null){
			internalName = resource.getOriginalClassNode().name; 
		}
		
		String inlineeOnwer;
		for(Entry<String, InlineeField> entry: _inlineeFields.entrySet()){
			String fieldName = entry.getKey();
			InlineeField element = entry.getValue();
			map.put(internalName+"."+element.name, element.calculateName());
			
		} 

		map.put(internalName, _owner);
		
	}

	/**
	 * Assign obj[fieldName] = new generated obj.  If 
	 *       obj[fieldName] is not transformed, use existing one. 
	 *        
	 * 
	 * 
	 * @param obj This is new created obj whose fieldName member waiting to be initialized.  
	 */
	public boolean assignObject(Object obj){
		if(_fieldObject == null ){
			return false;
		}else{
			
			try {
				
				for(Field field : _fieldObject.getClass().getFields()){
					String fieldName = field.getName();
					Object fieldObj = field.get(_fieldObject);
					
					InlineeField inlinee = _inlineeFields.get(fieldName);
					String newHosterName = inlinee.calculateName();
				
					Field hostField = obj.getClass().getField(newHosterName);
					hostField.set(obj, fieldObj);
				}
				
				return true;
				
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
		}
		
	}

	@Override
	public Object getReceiver() {
		return _fieldObject;
	}

	/**
	 * 
	 */
	@Override
	public void setupNewReceiver(Object newObj) {
		
		if(_inlineeFields.size() == 0 ){
			return ;
		}
		
		for(Entry<String, InlineeField> entry : _inlineeFields.entrySet()){
			String key = entry.getKey();
			InlineeField ele =  entry.getValue();
			
			String mappedName = ele.calculateName();
			ReflectionUtil.setupObject(_fieldObject, newObj, key, mappedName);
		}
	}

	/**
	 * @return
	 */
	@Override
	public Collection getMappedNames() {
		Set<String> mappedNames = new LinkedHashSet<String>();
		for(InlineeField field: _inlineeFields.values()){
			mappedNames.add(field.getMappedName());
		}
		
		return mappedNames;
	}

	@Override
	public boolean isFieldErased() {
		// TODO Auto-generated method stub
		return true;
	}
}




class InlineeField extends FieldNode{
	
	private String _mappedName;
	private String _fieldHost;
	
	private ClassContext _context;

	
	public InlineeField(FieldNode node, String fieldHost, ClassContext context){
		super(Opcodes.ASM5, node.access, node.name, node.desc, node.signature, node.value);
		_fieldHost = fieldHost;
		_context = context;
	}
	
	/**
	 * @return
	 */
	public String getMappedName() {
		return _mappedName;
	}

	public String getName(){
		return name;
	}
	
	public void accept(ClassNode classNode, String fieldHoster,  Object receiver){
		if(receiver!=null && !receiver.getClass().isArray()){
			value = receiver;
		}
		String name = calculateName();
		if(value instanceof Integer || 
				value instanceof Long || 
				value instanceof String ||
				value instanceof Double ||
				value instanceof Float ){
			classNode.fields.add(new FieldNode(Opcodes.ASM5, access, name, desc, signature, value));
		}else{
			classNode.fields.add(new FieldNode(Opcodes.ASM5, access, name, desc, signature, null));
			_context.recordInitValue(name, value);
		}
		
	}
	
	public String calculateName() {
		if(_mappedName!=null){
			return _mappedName;
		}
		
		StringBuffer buffer = new StringBuffer();
		_mappedName = buffer.append(_fieldHost).append(Constants.SEPARATOR).append(name).toString();
		return _mappedName;
		
	}
}
