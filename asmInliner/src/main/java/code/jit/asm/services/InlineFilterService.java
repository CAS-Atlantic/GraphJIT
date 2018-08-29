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

package code.jit.asm.services;

import java.lang.invoke.MethodHandle;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Type;

import code.jit.asm.backplane.ClassContext;
import code.jit.asm.backplane.InlineCode;
import code.jit.asm.common.utils.ASMUtil;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.rules.IRule;
import code.jit.asm.rules.RuleKind;


/**
 * @author shijiex
 *
 */
public class InlineFilterService {
	
	static InlineFilterService _instance = new InlineFilterService();
	
	static Map<RuleKind, IRule> _rules = new ConcurrentHashMap<RuleKind, IRule>();
	static Set<Type> _primitiveTypes = new LinkedHashSet<Type>(); 
	static {
		_primitiveTypes.add(Type.getType(Boolean.class));
		_primitiveTypes.add(Type.getType(Byte.class));
		_primitiveTypes.add(Type.getType(Double.class));
		_primitiveTypes.add(Type.getType(Float.class));
		_primitiveTypes.add(Type.getType(Integer.class));
		_primitiveTypes.add(Type.getType(Long.class));
		_primitiveTypes.add(Type.getType(Short.class));
		_primitiveTypes.add(Type.getType(Void.class));
		_primitiveTypes.add(Type.getType(String.class));
	}

	//Handle configure it whether it is inlinable. 
	static Set<String> _configuedUninlinable = new LinkedHashSet<String>();
	static {
		_configuedUninlinable.add(Type.getType(MethodHandle[].class).getClassName());
	}
	
	private InlineFilterService(){
		
	}

	public static InlineFilterService get(){return _instance;}
	
	
	public void register(RuleKind kind, IRule rule){
		_rules.put(kind, rule);
	}
	
	public IRule getCurrentRule(){
		return _rules.get(ConfigurationService.get().RULE);
	}
	
	/**
	 * 
	 * @param context
	 * @return true:  Inline YES 
	 *         false: Inline NO
	 */
	public boolean isCalleeConfiguredInlinable(ClassContext context){
		RuleKind kind = ConfigurationService.get().RULE;
		
		if(_rules.containsKey(kind) == false ){
			return false;
		}
		return _rules.get(kind).filterCallee(context.getClassName(), context.getMethodName(), context.getDesc());
	}
	
	public void registerGeneratedClass(String className, String method){
		_rules.get(ConfigurationService.get().RULE).register(className, method);
	}
	
	public boolean isInvocationInlineableCallSite(String owner, String name, String desc){
		return _rules.get(ConfigurationService.get().RULE).filterCallee(ASMUtil.getClassName(owner), name, desc);
	}
	
	public boolean isMethod4InlineVisitCaller(String className, String methodName){
		if(className.startsWith(Constants.JAVA))  return false;
		
		RuleKind kind = ConfigurationService.get().RULE;
		
		if(_rules.containsKey(kind)){
			return _rules.get(kind).isCallerMethodAllowedInlining(className, methodName);
		}
		return false;
	}
	
	
	public boolean isPrimiteType(Type type){
		int sort = type.getSort();
		return (sort < Type.OBJECT && sort!= Type.ARRAY) || 
				(sort == Type.OBJECT && _primitiveTypes.contains(type)) || 
				(sort ==Type.ARRAY && isPrimiteType(type.getElementType()));  
		//return _primitiveTypes.contains(type);
	}
	
	public boolean isTypeInlineable(Type type){
		RuleKind kind = ConfigurationService.get().RULE;
		
		return _rules.containsKey(kind) && _rules.get(kind).isTypeInlineable(type.getClassName());
	}
	
	public boolean isClassFieldInlineable(String hostClass, String fieldType, String fieldName){
		RuleKind kind = ConfigurationService.get().RULE;
		if(isFieldTypeConfiguredSkipInlining(fieldType)) return false;
		if((hostClass.startsWith("java") || fieldType.startsWith("java")) && ConfigurationService.get().INLINE_CODE == InlineCode.METHOD_INLINE) return false;
		
		return _rules.get(kind).isTypeInlineable(fieldType);
	}
	
	public boolean isFieldTypeConfiguredSkipInlining(String fieldClassName){
		return _configuedUninlinable.contains(fieldClassName); 
	}
}
