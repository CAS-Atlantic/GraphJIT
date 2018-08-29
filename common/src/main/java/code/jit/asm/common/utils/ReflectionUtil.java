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

package code.jit.asm.common.utils;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.objectweb.asm.Type;


/**
 * @author shijiex
 *
 */
public class ReflectionUtil {

	public static void adapt2GeneratedObject(Object original, Object newObj)
			 {

		for(Field field: newObj.getClass().getDeclaredFields()){
			try {
				if(java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
				String fieldName = field.getName();
				Object value = getFieldObject(original, fieldName);
				setFieldObject(newObj, fieldName, value);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	/**
	 * 
	 * @param original
	 * @param newObj
	 * @param map The fieldName mapping from fields in the original Class to that of newClass. 
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	public static boolean adapt2GeneratedObject(Object original, Object newObj, Map<String, Collection<String>> map) {
		
		for (Entry<String, Collection<String>> entry : map.entrySet()) {
			String name = entry.getKey();
			Object originalValue = getFieldObject(original, name);
			try {
				setFieldObject(newObj, name, originalValue);
			}catch(NoSuchFieldException e0){
				Collection<String> mappedNames = entry.getValue();

				for (String mappedName : mappedNames) {
					String nameOfChildField = restoreChildFieldName(name,
							mappedName);
					if (null == nameOfChildField) {
						System.err
								.println("Can not to decode the mapped field name "
										+ name + " " + mappedName);
					}
					Object originChildFieldValue = getFieldObject(originalValue, nameOfChildField);
					try {
						setFieldObject(newObj, mappedName, originChildFieldValue);
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
				}
			}catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}
		
		for(Field field : original.getClass().getDeclaredFields()){
			String name = field.getName();
			if(map.keySet().contains(name)) continue;
			
			Object newValue = getFieldObject(original, name);
			try {
				setFieldObject(newObj, name, newValue);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * @param name: The field name in the original class 
	 * @param mappedName: The one of field names in the newClass that mapped from field 'name'. 
	 * @return
	 */
	private static String restoreChildFieldName(String name, String mappedName) {
		if(mappedName.startsWith(name) == false){
			return null;
		}else{
			return mappedName.replace(name+Constants.SEPARATOR, "");
		}
	}



	public static boolean setupObject(Object originalReceiver, Object newReceiver, String fieldName, String mappedFieldName){
		
		try {
			
			Object value = getFieldObject(originalReceiver, fieldName);
			setFieldObject(newReceiver, mappedFieldName, value);
			
//			Field newField = newReceiver.getClass().getField(mappedFieldName);	
//			newField.set(newReceiver, value);
			
		}
		catch (NoSuchFieldException| SecurityException | IllegalArgumentException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	
	
	public static Object getFieldObject(Object receiver, String fieldName) {
		try {
			Field field = getClsField(receiver.getClass(), fieldName);
			if(field == null){
				// in case the fieldName == trueTarget_next_falseTarget
				Object obj = receiver ;
				StringTokenizer tokens = new StringTokenizer(fieldName, Constants.SEPARATOR);
				while(tokens.hasMoreTokens()){
					String token = tokens.nextToken();
					obj = getFieldObject(obj, token);
				}
				return obj;
				
				
			}else{
				Object fieldObject = field.get(receiver);
				return fieldObject;
				
			}
			

		} catch (IllegalArgumentException | IllegalAccessException
				|  SecurityException e) {
			// Yes. I want print it here..
			e.printStackTrace();
		}
		return null;
	}
	
	public static void setFieldObject(Object receiver, String fieldName, Object fieldValue) throws NoSuchFieldException{
		try {
			Field field= getClsField(receiver.getClass(), fieldName);
			field.set(receiver, fieldValue);
			
		} catch (IllegalArgumentException | IllegalAccessException
				| SecurityException e) {
			// Yes. I want print it here..
			e.printStackTrace();
		}
	}
	
	private static Field getClsField(Class  cls, String fieldName){
		Field field = null;
		while(cls!=null){
			try{
				field = cls.getDeclaredField(fieldName);	
				if(field!=null) break;
			}catch(NoSuchFieldException e){
				cls = cls.getSuperclass();
			}
		}
		if(field!=null){
			field.setAccessible(true);  //Overcome the private field.
			return field;	
		}
		return null;
	}
	/**
	 *  Check targetObject is one kind of internalName type. 
	 *  
	 * @param targetObject
	 * @param internalName
	 * @return
	 */
	public static boolean checkTypeInhereentance(Object targetObject, String internalName){
		String className = Type.getObjectType(internalName).getClassName();
		Class cls = null;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
			
		}
		return cls.isAssignableFrom(targetObject.getClass());
		
	}
	
	public static boolean checkTypeInhereentance(String superClass, String childClass){
		Class superCls = getClassFromName(superClass);
		Class childCls = getClassFromName(childClass);
		if(superCls!=null && childCls!=null){
			return superCls.isAssignableFrom(childCls);	
		}
		return false;
		
	}
	
	public static boolean checkTypeInhereentance(Class superCls, String childClass){
		Class childCls = getClassFromName(childClass);
		if(childCls!=null){
			return superCls.isAssignableFrom(childCls);	
		}
		return false;
		
	}
	
	private static Class getClassFromName(String className){
		Class cls = null;
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean isMethodHandleChild(String className){
		//It is quite strange here because className = null while uternalName = TGWD
		try {
			if(className != null){
				// ????? className is anonymous ? 
				Class cls = Class.forName(className);
				return MethodHandle.class.isAssignableFrom(cls);
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
		
	}

	
	public static boolean setupNewObject(Object old, Object newObj){
		
		return true;
		
	}
}
