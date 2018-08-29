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


/**
 * 
 *  Hi. Please go to simple this interface.. It is more than needed...
 *  
 * @author Shijiex
 *
 */
public interface IFieldContext {

	//Set newObj's field members' value according to the value in the _receiver
	public void setupNewReceiver(Object newObj);
	
	public Object getReceiver();
	
	//Visit this fieldContext by classWriter
	public String mapFieldName(String owner, String name);
	
	public void putFields(MethodVisitor mv, Type getFieldOwner, int variable);
	
	public void populateMap(Map<String, String> map);
	
	public Collection getMappedNames();
	
	public boolean isFieldErased();

	void accept(ClassNode classNode);
}
