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
package code.jit.asm.services;

import code.jit.asm.backplane.InlineCode;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.rules.RuleKind;

/**
 * @author shijiex
 *
 */
public class ConfigurationService {
	
	private static  ConfigurationService _instance = new ConfigurationService();

	
	
	public  InlineCode INLINE_CODE = InlineCode.METHOD_INLINE;

	public  boolean SKIP_STATICMETHOD = false;
	
	public RuleKind RULE = RuleKind.SIMPLE;
	
	public boolean inlineDirect = true;
	private void initInlineDirect(){
		if(System.getProperty(Constants.INLINE_DIRECT_METHODHANDLE, "false").equals("true")){
			inlineDirect = true;
		}else{
			inlineDirect = false;
		}
	}
	
	/**
	 *  Load external configurations (From DB, XML, or JSON) to setup configurations, 
	 *  and loads up all filters if necessary. 
	 *  
	 *  We can use ORM mapper or something else.. Lots of solutions.. 
	 *  
	 *  Use default value first.
	 */
	private ConfigurationService(){
		initInlineDirect();
	}
	
	public static ConfigurationService get(){
		return _instance;
	}
	
	
}
