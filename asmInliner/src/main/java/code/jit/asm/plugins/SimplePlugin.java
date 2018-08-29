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
package code.jit.asm.plugins;

import code.jit.asm.backplane.BytecodeResource;
import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.Blob;
import code.jit.asm.common.IGraphNode;
import code.jit.asm.common.PluginType;
import code.jit.asm.common.utils.ReflectionUtil;
import code.jit.asm.services.BytecodeCacheService;
import code.jit.asm.services.BytecodeGenerator;
import code.jit.asm.services.InlineFilterService;

/**
 *  This is similiar to the MethodHandlePlugin, where the final inlinee does not have any field members. 
 */

/**
 * @author shijiex
 *
 */
public class SimplePlugin extends AbstractPlugin {

	public SimplePlugin(PluginType type){
		super(PluginType.SIMPLE);
	}
	

	/* (non-Javadoc)
	 * @see code.jit.asm.plugins.AbstractPlugin#isOwnerInlinable(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isInvokeInlinable(String owner, String name, String desc) {
		if(owner.startsWith("java")) return false;
		return InlineFilterService.get().isInvocationInlineableCallSite(owner, name, desc);
	}

	/* (non-Javadoc)
	 * @see code.jit.asm.plugins.AbstractPlugin#generate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected Blob generate(String owner, String methodName, String desc, MethodContext context) {
		
		Object inliningTarget = context.getInvocationReceiver().getReceiver();
		
		if(inliningTarget==null){
			Object receiver = context.getClassContext().getOwner();
			String fieldName = context.getReceiverFieldName();
			inliningTarget = ReflectionUtil.getFieldObject(receiver, fieldName);	
		}
		
		if(ReflectionUtil.checkTypeInhereentance(inliningTarget, owner)){
			BytecodeGenerator.get().generate(inliningTarget, true, false);
			BytecodeResource resource = BytecodeCacheService.get().getBcClass(inliningTarget);
			if(resource!=null){
				return new Blob();
			}	
		}
		return null;
	}


	@Override
	public Blob compile(IGraphNode receiver) {
		return null;
	}
	
}
