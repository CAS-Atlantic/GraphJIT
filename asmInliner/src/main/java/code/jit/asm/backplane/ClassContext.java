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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import code.jit.asm.common.IGraphNode;
import code.jit.asm.common.utils.ASMUtil;
import code.jit.asm.common.utils.ReflectionUtil;
import code.jit.asm.common.utils.Utils;
import code.jit.asm.services.ACLoader;
import code.jit.asm.services.ConfigurationService;

/**
 * @author shijiex
 *
 */
public class ClassContext{

	private IGraphNode _owner;
	
	private ClassNode _originClassNode;               //The ClassNode generated from Template of owner;
	
	//_transformedTarget is the target that is converted from MH.
	//private Object _transformedTarget;    //Still use _owner for _transformedTarget as 1) _transformedTarget is from _owner. 
	//                                Make sure the field name is not changed in the BytecodeAnn. Otherwise would be NPE failure. 
	private Map<String, MethodNode> _methods;
	private ClassNode _transformedClassNode;
	
	String _desc;
	String _methodName;
	
	/**
	 *   The reason for keeping both _bytes and _newCls is that I am no longer able to get InputStream from the 
	 *   Class (Anonymous Class) that is loaded by ACLoader. The InputStream to the new bytecode buffer is still
	 *   necessary because the generated Class is in the Cache and used in the Second Generation Pass. 
	 *   
	 *   @TODO: Still I think it would be better if Using ClassLoader per Generation Class. 
	 *   
	 *   Background on anonymous classes()openjdk.5641.n7.nabble.com/Lambda-class-names-no-longer-reported-when-listening-for-JVMTI-EVENT-CLASS-FILE-LOAD-events-td174840.html:

They are a private feature of the HotSpot JVM implementation.  Naturally, compilers and other tightly-coupled tools have to know about them.

They don't have names, at least in the sense of something you could use to look up via a class loader.  What names they display are (as you see) derived from their bytecodes, but they do not function as regular class names.

Specifically, if someone tries to use the supposed name of an anonymous class with Class.forName or ClassLoader.loadClass, the result will be ClassNotFoundException.

The mangled suffix /XXXXX on Class.getName string provides an extra hint as to what is wrong.  (The XXXXX value happens to be the class's hash code, which makes it easier to distinguish classes with the same supposed name.)  Since slash '/' is an illegal element of class names, there's no ambiguity about how the name got that way. 
	 *   
	 */
	Class<?> _newCls;
	
	boolean _isCaller;
	
	
	/* 
	 * 
	 * This is only used for caching field & methods of the target class which has already in the BytecodeCache or an optimized..
	 * The format is: 
	 * 
	 *              FieldName -==>   FieldContext
	 *              ----------------------
	 *  
	 *  All methodNodes and FieldNode here have been visited in the inliner Class or the inlinee class.  
	*/
	Map<String, IFieldContext>  _cachedFields = new ConcurrentHashMap<String, IFieldContext>();
	
	private List<FieldNode> _fieldNodes; //This is class fieldNode list.
	
	//Map field names in the original class to the new FieldNames. This only applys to inlineable fields. 
	private Map<String, Collection<String>> _fieldNames = new LinkedHashMap<String, Collection<String>>();
	
	
	
	public static ClassContext newClassContext(){
		return new ClassContext();
	}
	
	public void setCaller() {
		_isCaller = true;
	}
	
	public boolean isCaller(){
		return _isCaller;
	}

	
	private ClassContext(){
		_fieldNames = new LinkedHashMap<String, Collection<String>>();
		_methods = new LinkedHashMap<String, MethodNode>();
	}

//	public Class getOriginClass(){
//		return _originalCls;
//	}
//	
//	public void setOriginClass(Class cls){
//		_originalCls = cls;  //maybe not correct
//	}
	
	public ClassNode getOriginalClassNode(){
		return _originClassNode;
	}
	
//	public InputStream getTransformResult(){
//		return new ByteArrayInputStream(_bytes);
//	}

	
	public Class<?> getTransformClass(){
		return _newCls;
	}
	
//	public byte[] getTransformBuffer(){
//		return _bytes;
//	}
//	
	public String getClassName(){
		return _originClassNode.name;
	}
	
	public String getDesc(){
		return _desc;
	}

	public String getMethodName() {
		// TODO Auto-generated method stub
		return _methodName;
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer("");
		buffer.append(_owner.getClass().getName()).append(",").append(_methodName).append(".").append(_desc).append(",isCaller:").append(_isCaller);
		return buffer.toString();
	}

	public void cacheArrayField(String fieldName, Type fieldType, Object[] transformedObjs, ClassContext context){
//		String fieldOwner = Type.getInternalName(_originalCls);
//		ArrayFieldContext fieldContext = new ArrayFieldContext(fieldOwner, fieldName, fieldType, transformedObjs, context);
//		_cachedFields.put(fieldName, fieldContext);
	}
	
	public boolean isFieldErased(String fieldName) {
		if(!_cachedFields.containsKey(fieldName) || !(_cachedFields.get(fieldName) instanceof ArrayFieldContext)) return false;
		
		ArrayFieldContext fieldContext = (ArrayFieldContext)_cachedFields.get(fieldName);
		return fieldContext.isFieldErased();
	}


	/**
	 * 
	 * @param fieldName
	 * @param resource
	 * @param fieldObject  => Should be IGraphNode 
	 */
	public void cacheFields(String fieldName, BytecodeResource resource, Object fieldObject){
			FieldContext fieldContext = new FieldContext(fieldName, resource, Type.getType(_owner.getClass()), fieldObject, this);
			_cachedFields.put(fieldName, fieldContext);	
	}
	
	public void cacheCallerFieldNode(FieldNode fv){
		if(getFieldNodes() == null ) _fieldNodes = new LinkedList<FieldNode>();
		getFieldNodes().add(fv);
	}
	
	/**
	 * I do not want to add CW here so the cachedFields is returned back and the caller is in the charge of 
	 * visiting the hitted fields and constructors. 
	 * @return
	 */
	public void accept(ClassNode classNode){
		if(_fieldNodes == null ) return ;
		for(FieldNode node : _fieldNodes){
			if(_cachedFields.containsKey(node.name) && _cachedFields.get(node.name).isFieldErased()){
				_cachedFields.get(node.name).accept(classNode);
				continue;
			}
			classNode.fields.add(node);
			
		}
	}
	
	
	/**
	 * @param visitMethod
	 */
	private void acceptConstructor(MethodVisitor mv, Type methodType) {
//		mv.visitCode();
//		Label l0 = new Label();
//		mv.visitLabel(l0);
//		mv.visitVarInsn(Opcodes.ALOAD, 0);
//		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
//		
//		Type[] args = methodType.getArgumentTypes();
//		
//		if(_cachedFields!=null){
//			int i=0;   //args should match  _cachedFields' size (the number of NULL field receiver)
//			for(Map.Entry<String, FieldContext> entry : _cachedFields.entrySet()){
//				FieldContext fieldContext = entry.getValue();
//				if(fieldContext.getReceiver() == null ){
//					//Constructor initializes values.
//					if(i >=args.length ){
//						System.err.println("{Error here}: Construlctor generation failure with message: args size:"+ args.length + " iteration: "+i);
//					}
//					fieldContext.putFields(mv, args[i], i);
//					i++;
//					
//				}else{
//					//The fields will be initialized when FieldNode set up. 
//					
//				}
//				
//			}	
//		}
//		
//		
//		mv.visitInsn(Opcodes.RETURN);
//		mv.visitMaxs(1, 1);
//		mv.visitEnd();
		
	}

	/**
	 *  There is a case that _cachedFields is NULL because all fields are pritimite and the _cachedFields is not built. 
	 *  In this case, 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return
	 */
	public boolean skipVisitFieldInsn(String owner, String name, String desc){
		if(ConfigurationService.get().INLINE_CODE != InlineCode.CLASS_INLINE){
			return false;
		}
		return _cachedFields.containsKey(name) && ASMUtil.getClassName(owner).equals(_originClassNode.name) && _cachedFields.get(name).isFieldErased();
	}

	/**
	 *  Maps the field name in the Callee to the newFieldName in the Caller. 
	 * 
	 * @param owner
	 * @param name
	 * @param lastGetFieldName
	 * @return
	 */
	public String mapInlineeFieldName(String owner, String name,
			String lastFieldNameInliner) {
		
		if(lastFieldNameInliner == null || !_cachedFields.containsKey(lastFieldNameInliner)){
			System.err.println("Remap getField name in the Callee sees NULL or not Inlinabvle.., pls check OOPS: "+ owner+"  "+name+"  "+lastFieldNameInliner );
			return name;
		}
		
		String newName = _cachedFields.get(lastFieldNameInliner).mapFieldName(owner, name);
		return newName;
	}

	/**
	 * @param classNode
	 */
	public void setOriginalClassNode(ClassNode classNode) {
		_originClassNode = classNode;
		for(Object ele: _originClassNode.methods.toArray()){
			MethodNode node = (MethodNode) ele;
			_methods.put(Utils.generateMethodNodeKey(node.name, node.desc), node);
		}
	}
	
	public MethodNode getMethodNode(String methodName, String desc){
		return _methods.get(Utils.generateMethodNodeKey(methodName, desc));
	}

	/**
	 * @param mv   This the ConstructorMerge visitor..
	 * @param top
	 * @param variable/
	 */
	public void putFields(MethodVisitor mv, int variable, Type top, String fieldName) {
		
		if(!_cachedFields.containsKey(fieldName)){
			System.err.println("PutField error here.. Should not see this one.. ");
			return ;
		}
		
		/**
		 *  The operand stack is like: 
		 *          aload 0 
		 *          aload variable 
		 *           
		 */
		
		_cachedFields.get(fieldName).putFields(mv, top, variable);
	}
	
	public boolean willFieldSplit(String fieldName){
		return _cachedFields.containsKey(fieldName);
	}
	
	public boolean isInlinableFieldArray(String fieldName){
		return _cachedFields!=null && willFieldSplit(fieldName) && (_cachedFields.get(fieldName) instanceof ArrayFieldContext); 
	}

	/**
	 * @return
	 */
	public Map<String, String> createMapper(String fieldHoster) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		if(_cachedFields.containsKey(fieldHoster)){
			_cachedFields.get(fieldHoster).populateMap(map);
		}
		return map;
	}

	/**
	 * @param fieldName
	 * @param fieldReceiver
	 */
//	public void cacheFieldReceiver(String fieldName, Object fieldReceiver) {
//		createIfAbsent();
//		
//		if(_cachedFields.containsKey(fieldName)){
//			FieldContext fieldContext = _cachedFields.get(fieldName);
//			fieldContext.setReceiver(fieldReceiver);
//		}
//		
//	}


	
	private List<Object> _args4Const ;
	
	public Type generateConstructDesc() throws UnImplementedException{

		List<Type> types = new LinkedList<Type>();
		_args4Const = new LinkedList<Object>();
		if(_fieldNodes !=null){
			for(FieldNode node: getFieldNodes()){
				if(_cachedFields!=null &&_cachedFields.containsKey(node.name)){
					IFieldContext context = _cachedFields.get(node.name);
					if(context.getReceiver()!=null){
						types.add(Type.getType(context.getReceiver().getClass()));
						_args4Const.add(_cachedFields.get(node.name).getReceiver());
					}else{
						_args4Const.add(null);
						throw new UnImplementedException("It is un-implemenedted. You should not see this exception!");
						//types.add(Type.getType(context._class));
					}
				}else{
					
					types.add(Type.getType(node.desc));
					_args4Const.add(ReflectionUtil.getFieldObject(_owner, node.name));
				}
			}	
		}else{
			return Type.getMethodType(Type.getType(void.class));
		}
		
		
		Type[] args = new Type[types.size()];
		types.toArray(args);
		return Type.getMethodType(Type.getType(void.class), args);
	}
	
	public List<Object> getConstArgs(){
//		if(_args4Const == null ){
//			generateConstructDesc();
//		}
		//Un-implemeneted..
		return null;
	}
	 

	/**
	 * @return the _fieldNodes
	 */
	public List<FieldNode> getFieldNodes() {
		return _fieldNodes;
	}

	
	
	private Object _newObj;
	
	public Object getGenerateObj(){
		return _newObj;
	}
	
	
	/**
	 * @param newObj
	 */
	public void createTransformedObject() {
		
		// The generation fails or the instance has already been there.
		if(_transformedClassNode == null || _newObj!=null) return;   
		
		if(_newCls ==null){
			_newCls = ACLoader.getACLoader().loadClass(_transformedClassNode);
		}
		
		try {
			_newObj = _newCls.newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(ConfigurationService.get().INLINE_CODE != InlineCode.METHOD_INLINE){
			if(_fieldNodes!= null){
				for(FieldNode fieldNode: _fieldNodes){
					String fieldName = fieldNode.name;
					if(_cachedFields!=null && _cachedFields.containsKey(fieldName)){
//						;
//						Object oldFieldObj = ReflectionUtil.getFieldObject(_receiver, fieldNode.name);
//						Object generatedObject = ObjectCache.getInstance().get(oldFieldObj); 
//						
						IFieldContext context = _cachedFields.get(fieldName);
						context.setupNewReceiver(_newObj);
						
						_fieldNames.put(fieldNode.name, context.getMappedNames());
						
					}else{
						
						//@SHIJIE: Comment out first to pass compilation.
						ReflectionUtil.setupObject(_owner, _newObj, fieldName, fieldName);
					}
				}
				//@TODO: setFieldInitValue is to keep initial value of a field value here.. THIS REQUIRE MORE CHECKING because it seems there is CONFLICT.
				//           >____________<~~~!!
				
				this.setFieldInitValue(_newObj);
			}	
		}else{
			// The generation maybe invoked by (className, methodName, desc). Here it does not support inherient
			//@SHIJIE Comment out to pass compilation.
//			if(_transformedTarget!=null){
//				ReflectionUtil.adapt2GeneratedObject(_transformedTarget, _newObj);	
//			}
			
		}
	}
	
	
	public Map<String, Collection<String>> getFieldMappers(){
		return _fieldNames;
	}

		
	public void setOwner(IGraphNode obj) {
		_owner = obj;
	}
	
	public IGraphNode getOwner(){
		return _owner;
	}

	
	private Map<String, Object> _fieldInitValue = new LinkedHashMap<String, Object>(); 
	public void recordInitValue(String name, Object value){
		_fieldInitValue.put(name, value);
	}
	
	public void setFieldInitValue(Object obj){
		Iterator<String> iter = _fieldInitValue.keySet().iterator();
		while(iter.hasNext()){
			String name = iter.next();
			Object value = _fieldInitValue.get(name);
			try {
				if(value!=null && value != ReflectionUtil.getFieldObject(obj, name)){
					ReflectionUtil.setFieldObject(obj, name, value);	
				}
				
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setGeneratedClassNode(ClassNode node){
		this._transformedClassNode = node;
	}
	
	public ClassNode getGeneratedClassNode(){
		return this._transformedClassNode;
	}

	private boolean _bSetupObject =false;
	public void isObjectSetup(boolean setupObject) {
		_bSetupObject = setupObject;
	}
	public boolean getBObjectSetup(){
		return _bSetupObject;
	}

	public String getOriginalClassName() {
		return this._originClassNode.name;
	}

	public void setDesc(String desc) {
		_desc = desc;
		
	}

	public void fromCache(BytecodeResource bc) {
		setOriginalClassNode(bc.getOriginalClassNode());
		setGeneratedClassNode(bc.getOriginalClassNode());
		_newObj = bc.getTransformedObject();
		
	}

}
