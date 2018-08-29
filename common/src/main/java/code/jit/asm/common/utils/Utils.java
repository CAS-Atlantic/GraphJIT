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
import java.io.InputStream;

public class Utils {

	private static String RUNOS = System.getProperty("os.name").toLowerCase();
	
	public static String getTemplDir(){
		if(RUNOS.contains(Constants.WIN)){
			return Constants.WIN_TEMP_DIR;	
		}else
			return Constants.UNIX_TEMP_DIR; 
	}

	public static String generateMHClassName(String name) {
		name = Constants.MH_PREFIX+name.substring(name.lastIndexOf(Constants.DOT)+1);
		return name;
	}
	
	public static boolean isLegalGenerated(String className){
		return className.startsWith(Constants.MH_PREFIX);
	}
	
	public static String generateMethodNodeKey(String methodName, String desc){
		return methodName.trim()+desc.trim();
	}
	

	public static String buildResourceName(Class cls){
		StringBuffer buffer = new StringBuffer(Constants.SLASH);
		buffer.append(cls.getName().replace((char)Constants.int_DOT, (char)Constants.int_SLASH));
		buffer.append(Constants.CLASS_SUFFIX);
		return buffer.toString();
	}
	
	
	public static String builtFakeFieldNameForArray(String arrayFieldName, int index){
		return arrayFieldName+Constants.SEPARATOR+index;
	}
	public static InputStream getInputStreamFromClass(Class cls){
		return cls.getResourceAsStream(buildResourceName(cls));
	}
	
	public static String getOption(String optionName, String defaultValue){
		return System.getProperty(optionName, defaultValue); 
	}
}
