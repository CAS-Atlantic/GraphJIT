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

package code.jit.asm.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;

import code.jit.asm.backplane.IVisitorID;
import code.jit.asm.backplane.InlineCode;
import code.jit.asm.backplane.Mapper;
import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.logging.GraphLogger;
import code.jit.asm.common.utils.ASMUtil;
import code.jit.asm.plugins.IPlugin;
import code.jit.asm.services.ConfigurationService;
import code.jit.asm.services.NameMappingService;

/**
 * 
 * @author Shijie Xu
 *
 */
public class MethodCallInliner extends LocalVariablesSorter implements IVisitorID{

	//private List<TryCatchBlockNode> _blocks = new ArrayList<TryCatchBlockNode>();

	protected MethodContext _context;
	
	private IPlugin _plugin;
	
	private Map<Boolean, IFieldHandler> _fieldHandles = new LinkedHashMap<Boolean, IFieldHandler>();
	
	private static Logger _logger = GraphLogger.get(MethodCallInliner.class);
	
	public MethodCallInliner(int access, String desc, MethodContext context){
		super(Opcodes.ASM5, access, desc, context.getRawMV());
		_context = context;
		//_fieldVisitor = new FieldManipulationVisitor(mv, context);
		_plugin = NameMappingService.get().getPlugin();
		
		

		// handle for Callee Field Handle . 
		_fieldHandles.put(Boolean.TRUE, new IFieldHandler(){

			@Override
			public void remap(int opcode, String owner, String name,
					String desc) {
				/**
				 *  Owner: inlinee => Inliner Owner 
				 *  name : inlinee field name => remapped to name in the Inliner.
				 *  desc : no change. 
				 */
			
				owner = _context.getClassContext().getClassName();
				name  = _context.getClassContext().mapInlineeFieldName(owner, name, _context.getReceiverFieldName());
				_context.getRawMV().visitFieldInsn(opcode, owner, name, desc);
				
			}
			
		});
		
		
		//handle for inliner remap
		_fieldHandles.put(Boolean.FALSE, new IFieldHandler(){

			@Override
			public void remap(int opcode, String owner, String name,
					String desc) {
				
				if(_context.getClassContext().skipVisitFieldInsn(owner, name, desc)){
					//Remember the last GetField on the Caller's field members.
					//_context.setReceiverFieldName(name);
					return;
				}else{
					//XXField instruction does not relate to the LocalVariableSort, so I use the raw visitor here.
					// Otherwise, I should use this.super!
					_context.getRawMV().visitFieldInsn(opcode, owner, name, desc);
				}
			}
			
		});
		
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		
		if(opcode != Opcodes.INVOKEVIRTUAL){
			mv.visitMethodInsn(opcode, owner, name, desc, itf);
			return;
		}
		
		MethodNode mn = _plugin.map(owner, name, desc, _context, this);
		if(mn == null){
			_logger.debug("Inliner does not find MethodNode for {}, {} {} {} and does not do any modification. ", _context.getClassContext().getOwner(), owner, name, desc);
			mv.visitMethodInsn(opcode, owner, name, desc, itf);
			return;
		}
		
		performInline(ASMUtil.isStaticMethod(mn)?Opcodes.INVOKESTATIC:Opcodes.INVOKEVIRTUAL, owner, desc, mn);
	}
	
	protected void performInline(int opcode, String owner, String desc, MethodNode mn){
		_logger.debug("Inliner prepare for inlining {}, {} {}", _context.getClassContext().getOwner(), owner, mn.toString());
		Remapper remapper = Mapper.getMapper(_context, _context.getReceiverFieldName());
		
		Label end = new Label();
		_context.beginInline();
		_context.setCallee(owner);
		
		mn.accept(new InliningAdapter(this,
					opcode == Opcodes.INVOKESTATIC ? Opcodes.ACC_STATIC : 0, desc,
					remapper, end, _context));
		_context.endInline();
		super.visitLabel(end);
		
	}

	
	/**
	 *  Here I want to handle getField, putField, getStatic, and putStatic  
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		//@ASSUMPTION: NONE of putField 
		//_context.cacheLastGetField(owner, name, desc);
		
		if(ConfigurationService.get().INLINE_CODE != InlineCode.CLASS_INLINE){
			super.visitFieldInsn(opcode, owner, name, desc);
			//castIfNecessary(owner, name, desc);
			return ;
		}

		switch(opcode){
		case Opcodes.PUTFIELD:
			_logger.error("Inliner remapper sees putField instruction, fails..");
			break;
		case Opcodes.GETFIELD:
			_fieldHandles.get(_context.inlining()).remap(opcode, owner, name, desc);
			break;
		case Opcodes.GETSTATIC:
		case Opcodes.PUTSTATIC:
			super.visitFieldInsn(opcode, owner, name, desc);
			//System.err.println("Instruction GetStatic/putStatic is not suppported, but I see it in your bytecode sequences. ");
			break;
			
		default:
			_logger.error("The opcode {} Should not be seen here..!", opcode);
		}
		
	}
	
	@Override
	public void visitVarInsn(final int opcode, final int var){
		super.visitVarInsn(opcode, var);;
	}

	@Override
	public String getVisitorName() {
		return MethodCallInliner.class.getName();
	}
}

interface IFieldHandler{
	public void remap(int opcode, String owner, String name, String desc);
}

