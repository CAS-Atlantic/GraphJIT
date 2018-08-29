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

package code.jit.asm.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class Blob {

	private Object _generatedObject;  //This one should be null
	private MethodNode _methodNode = null;
	private Class _cls = null;        //May be not null
	
	private ClassNode _classNode;
//	private Map<String, FieldNode> _fields = null;
	
	private boolean _visited;
	
	public Blob(Object object, MethodNode node, boolean visited){
		_generatedObject = object;
		_methodNode = node;
		_visited = visited;
	}
	
	
	public Blob(){
	}

	
//	public void addFieldNode(String name, FieldNode fieldNode){
//		if(_fields == null){
//			_fields = new LinkedHashMap<String, FieldNode>();
//		}
//		_fields.putIfAbsent(name, fieldNode);
//	}
//	
//	public Map<String, FieldNode> getFieldNodes(){
//		return _fields;
//	}
	/**
	 * @return the _classNode
	 */
	public ClassNode getClassNode() {
		return _classNode;
	}


	/**
	 * @param _classNode the _classNode to set
	 */
	public void setClassNode(ClassNode _classNode) {
		this._classNode = _classNode;
	}


	public Object getTemplateObject(){
		return _generatedObject;
	}
	
	public void setTemplateObject(Object obj){
		_generatedObject = obj;
	}
	
	public MethodNode getMethodNode(){
		return _methodNode;
	}
	
	public void setMethodNode(MethodNode node){
		_methodNode = node;
	}
	
	public void setClass(Class cls){
		_cls = cls;
	}
	
	public Class getCompiledClass(){
		return _cls;
	}
	
	public boolean isVisited(){
		return _visited;
	}
}
