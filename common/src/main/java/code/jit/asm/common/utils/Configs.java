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

package code.jit.asm.common.utils;

public class Configs {

	public static boolean _enableDump = false;
	public static boolean _globalcache = true;
	public static boolean _cache_per_MH = false;
	public static boolean _cache_per_MHClass = false;
	public static boolean _cache_template = false;
	static {
		_enableDump =System.getProperty(Constants.Options.DUMP, "false").equals("true");
		
		_globalcache=System.getProperty(Constants.Options.GLOBAL_CACHE, "true").equals("true");
		
		_cache_per_MH=System.getProperty(Constants.Options.CACHE_PER_MH, "true").equals("true");
		
		_cache_per_MHClass=System.getProperty(Constants.Options.CACHE_PER_MHCLASS, "true").equals("true");
		
		_cache_template = System.getProperty(Constants.Options.CACHE_TEMPLATE, "true").equals("true");
	}
}
