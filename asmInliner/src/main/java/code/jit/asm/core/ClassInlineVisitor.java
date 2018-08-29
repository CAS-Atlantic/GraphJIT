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
package code.jit.asm.core;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import code.jit.asm.backplane.BytecodeResource;
import code.jit.asm.backplane.ClassContext;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.common.utils.ReflectionUtil;
import code.jit.asm.services.BytecodeCacheService;
import code.jit.asm.services.BytecodeGenerator;
import code.jit.asm.services.InlineFilterService;

/**
 * @author shijiex
 *
 */
public class ClassInlineVisitor extends MethodInlineVisitor {
	

	public ClassInlineVisitor(int api, ClassContext context) {
		super(api, context);
		//_context = context;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		Type type = Type.getType(desc);
		FieldNode fieldNode = new FieldNode(Opcodes.ASM5, access, name, desc, signature, value);
		_context.cacheCallerFieldNode(fieldNode);
		if(InlineFilterService.get().isPrimiteType(type) || !InlineFilterService.get().isClassFieldInlineable(_context.getOriginalClassName(),
						         type.getClassName(), name)){
			return null;
		}
		
		Object fieldReceiver = ReflectionUtil.getFieldObject(_context.getOwner(), name);

		if(fieldReceiver instanceof Object[]){
			//Skip array field members
			return null; //false;
		}else{
			BytecodeResource resource = BytecodeCacheService.get().getBcClass(fieldReceiver);
			if(resource ==null){
				BytecodeGenerator.get().generate(fieldReceiver, true, false);
				resource = BytecodeCacheService.get().getBcClass(fieldReceiver);	
			}
			//The resource may still be NULL.
			if(resource!=null){
				_context.cacheFields(name, resource, fieldReceiver);	
			}
			
			return null;
		}
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		
		if (name.equals(Constants.CONSTRUCTOR)) {
			return null;  //Remove existing constructors 
		}
		return super.visitMethod(access, name, desc, signature, exceptions); 
	}
	
	@Override
	public void visitEnd() {
		_context.accept(this);
		super.visitEnd();
	}




	@Override
	public String toString(){
		return _context.toString();
	}
}

class ConstructorMethod{
	public  int _access;
	public  String _name;   //"init"
	public  String _desc;
	public  String _signature;
	public  String[] _exceptions;
	
	public ConstructorMethod(int access, String name, String desc, String signature, String[] exceptions){
		_access = access;
		_name = name;
		_desc = desc;
		_signature = signature;
		_exceptions = exceptions;
	}
	
}
