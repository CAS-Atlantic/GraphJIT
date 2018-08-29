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

package code.jit.asm.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hello world!
 *
 */
public class GeneratorFactory 
{
 
    private static Map<String, IGenerator> _instances = new ConcurrentHashMap<String, IGenerator>();
    
    public static IGenerator get(String clsName, PluginType type) throws ClassNotFoundException{
    	IGenerator generator =  init(clsName);
    	if(generator!=null){
    		generator.initPlugin(type);
    	}
    	return generator;
    }
    
    private static IGenerator init(String clsName) throws ClassNotFoundException{
    	
    	if(_instances.containsKey(clsName)) return _instances.get(clsName);
    	
    	Class cls = Class.forName(clsName);
    	
    	if(cls!=null && IGenerator.class.isAssignableFrom(cls)){
    		try {
				IGenerator generator = (IGenerator) cls.newInstance();
				_instances.put(clsName, generator);
				return generator;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return null;
    }
}
