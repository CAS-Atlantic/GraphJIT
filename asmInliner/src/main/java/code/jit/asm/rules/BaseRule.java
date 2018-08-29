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

package code.jit.asm.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import code.jit.asm.services.ConfigurationService;
import code.jit.asm.services.InlineFilterService;

public abstract class BaseRule implements IRule {

	
	protected Map<String, Set<String>> _map = new LinkedHashMap<String, Set<String>>();
	
	
	public BaseRule(RuleKind kind){
		InlineFilterService.get().register(kind, this);
		ConfigurationService.get().RULE = kind;
	}
	
	@Override
	public boolean filterCallee(String className, String methodName, String desc) {
		if(_map.containsKey(className)){
			return (_map.get(className).size()!=0 && _map.get(className).contains(methodName)) 
					|| _map.get(className).size() == 0;
					
		}
		return false;
	}
	
	@Override
	public boolean isTypeInlineable(String className) {
		return _map.containsKey(className);
	}
	

	/**
	 *  Check whether this method 
	 */
	@Override
	public boolean isCallerMethodAllowedInlining(String className, String methodName) {
		return isTypeInlineable(className) && _map.get(className).contains(methodName);
	}

	@Override
	public void register(Class cls, String... methods){
		if(!_map.containsKey(cls.getName())){
			Set<String> set = new LinkedHashSet<String>();
			Collections.addAll(set, methods);
			_map.put(cls.getName(), set);
		}else{
			_map.get(cls.getName()).addAll(Arrays.asList(methods));
		}
	}

	@Override
	public void register(String className, String method){
		if(_map.containsKey(className)){
			_map.get(className).add(method);
		}else{
			Set<String> set = new LinkedHashSet<String>();
			set.add(method);
			_map.put(className, set);	
		}
	}
	
}
