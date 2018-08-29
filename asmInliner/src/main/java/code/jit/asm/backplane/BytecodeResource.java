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
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import code.jit.asm.common.IGraphNode;

public class BytecodeResource {

	private IGraphNode _key;
	
//	private Class _orginalCls;
	private ClassNode _originalClassNode;
	
	
	//Cache parsed result for performance consideration.
	private ClassNode _optimizedClassNode;

	private Map<String, Collection<String>> _fieldNameMaps;
	private boolean _optimized;
	
	private Object  _newObject;
	
	private long _lastVisited;
	public BytecodeResource(ClassContext context){
		setOriginalClassNode(context.getOriginalClassNode());
		_optimizedClassNode =  context.getGeneratedClassNode();

		_fieldNameMaps = context.getFieldMappers();
		_optimized = true;
		_lastVisited = System.currentTimeMillis();
	}

	public IGraphNode getKey() {
		return _key;
	}

	public void setOptimized(){
		_optimized = true;
	}
	
	public boolean isOptimized(){
		return _optimized;
	}
	
	
	public List<FieldNode> getTargetFields(){
//		if(_optimizedClassNode == null ){
//			_optimizedClassNode = new ClassNode();
//			if(!ASMUtil.parseClassNode(new ByteArrayInputStream(_bytes), _optimizedClassNode)){
//				return null;
//			}
//		}
		return _optimizedClassNode.fields;

	}
	
	public List<MethodNode> getTargetMethods(){
//		if(_optimizedClassNode == null ){
//			_optimizedClassNode = new ClassNode();
//			if(!ASMUtil.parseClassNode(new ByteArrayInputStream(_bytes), _optimizedClassNode)){
//				return null;
//			}
//		}
		return _optimizedClassNode.methods;

	}

	public MethodNode getTargetMethodNode(String methodName, String desc){
		for(Object oMethod : _optimizedClassNode.methods){
			MethodNode method = (MethodNode)oMethod;
			if(method.name.equals(methodName) && method.desc.equals(desc)){
				return method;
			}
		}
		return null;
	}

	/**
	 * @return the _originalClassNode
	 */
	public ClassNode getOriginalClassNode() {
		return _originalClassNode;
	}


	/**
	 * @param _originalClassNode the _originalClassNode to set
	 */
	public void setOriginalClassNode(ClassNode originalClassNode) {
		this._originalClassNode = originalClassNode;
	}
	
	public Map<String, Collection<String>> getFieldMaps(){
		return _fieldNameMaps;
	}
	
	public void setTransformedObject(Object newObj){
		_newObject = newObj;
	}
	public Object getTransformedObject(){
		return _newObject;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(_originalClassNode.name).append(" -> ").append(_optimizedClassNode.name);
		return buffer.toString();
	}
	
	public void hit(){
		_lastVisited = System.currentTimeMillis();
	}
	
	public long getLife(){
		return System.currentTimeMillis()-_lastVisited;
	}
	
}
