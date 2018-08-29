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

import java.util.Iterator;
import java.util.Map;


/**
 * 
 * The abstract Graph key identifies sub graph that have been bytecode-JITted.
 *  
 * @author Shijie Xu
 *
 */
public class AbsGraphKey {
	private Class _nodeClass;
	private INodeKey _nodeKey;
	private Map<String, AbsGraphKey> _childs; 
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//NO Recursively.
		result = prime * result + ((_childs == null) ? 0 : _childs.hashCode());
		result = prime * result
				+ ((_nodeClass == null) ? 0 : _nodeClass.hashCode());
		result = prime * result
				+ ((_nodeKey == null) ? 0 : _nodeKey.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbsGraphKey))
			return false;
		AbsGraphKey other = (AbsGraphKey) obj;
		if (_childs == null) {
			if (other._childs != null)
				return false;
		} else{
			if(_childs.size() != other._childs.size()) return false;
			Iterator<String> iter = _childs.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				if(!other._childs.containsKey(key)) return false;
				if(!_childs.get(key).equals(other._childs.get(key)))  return false;
			}
		} 
		
		if (_nodeClass == null) {
			if (other._nodeClass != null)
				return false;
		} else if (!_nodeClass.equals(other._nodeClass))
			return false;
		if (_nodeKey == null) {
			if (other._nodeKey != null)
				return false;
		} else if (!_nodeKey.equals(other._nodeKey))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbsGraphKey [Nodeclass="+_nodeClass+", nodeKey=" + _nodeKey + ", childs=" + _childs
				+ "]";
	}
}
