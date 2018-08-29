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
package code.jit.asm.common.utils;

/**
 * @author shijiex
 *
 */
public class Constants {

	public final static String CONSTRUCTOR = "<init>";
	
	public final static String STATIC_INIT = "<clinit>";
	
	public final static String JAVA = "java";
	
	public final static String EMPTY_DESC = "()V"; 
	
	public final static String SEPARATOR = "_";
	
	public final static String INVOKEEXACT = "invokeExact";

	public static final String METHODHANDLE = "MethodHandle";
	
	public static final String WIN = "win";
	
	public static final String CLASS_SUFFIX = ".class";
	
	public static final String WIN_TEMP_DIR = "c:\\temp\\";
	
	public static final String UNIX_TEMP_DIR = "/tmp/";

	public static final String DOT = ".";
	public static final String SLASH = "/";
	
	public static final int int_DOT = '.';
	public static final int int_SLASH = '/';

	public static final String MH_PREFIX = "DYN";
	
	public static final String CHECK_CACHE_ACTION = "cacheAction";
	
	public static final String TIMER_ACTION = "TimerAction";
	
	public static final String CHOOSER_ACTION = "ChooserAction";
	
	public static final String FILTER_ACTION = "FilterAction";
	
	public static final String TRANSFORMER_ACTION = "TransformerAction";
	
	public static final String OBJECTSETTER_ACTION = "ObjectSetterAction";
	public static final String UPDATECACHE_ACTION="updateCacheAction";
	
	public static final String LEFT_BRACKER = "[";
	
	public static final String METHODHANDLE_ARRAY = "[Ljava/lang/invoke/MethodHandle;";
	
	public static final String INLINE_DIRECT_METHODHANDLE="inlineDirect";
	
	public static final String IC_THRESH_VALUE = "ic_thresh";
	public static final String SIZE_THRESH_VALUE = "size_thresh";
	public static final String INVOKE_EXACT = "invokeExact";
	
	public static class Options{
		public static final String DUMP = "dump";    //Default is false
		public static final String BYTECODEJIT="jit_bytecode";   //Default is true
		public static final String SKIPSELECTION="skip_selection";  //Default is true
		public static final String GENERATOR = "generator";   //"code.jit.asm.services.BytecodeGenerator"
		public static final String ENABLECACHE = "enablecache";      //Default is false
		public static final String ENABLEOBJECTCACHE = "enableobjectcache";      //Default is false
		public static final String VALIDATE_BYTECODE = "validate_bytecode";  //Default is false
		public static final String INTERVAL = "interval";
		public static final String THRESHOLD = "threshold";
		public static final String GLOBAL_CACHE = "globalcache";
		public static final String GLOBAL_CACHE_SIZE = "globalcacheSize";
		public static final String GLOBAL_CACHE_RATIO = "globalcachRatio";
		public static final String MAX_BYTECODE_SIZE="max_bytecode_size";
		public static final String CACHE_PER_MH = "cache_per_mh";
		public static final String CACHE_PER_MHCLASS = "cache_per_class";
		public static final String CACHE_TEMPLATE = "cache_template";

	}
}
