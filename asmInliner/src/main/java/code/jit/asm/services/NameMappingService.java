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


import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.MethodNode;

import code.jit.asm.plugins.IPlugin;
import code.jit.asm.plugins.MethodHandlePlugin;
import code.jit.asm.plugins.SimplePlugin;
import code.jit.asm.backplane.MethodContext;
import code.jit.asm.common.PluginType;

/**
 * @author shijiex
 *
 */
public class NameMappingService {

	static NameMappingService _instance = new NameMappingService();
	
	/**
	 * 
	 * create a new field name
	 * 
	 * @param className
	 * @param filedName
	 * @return
	 */
	public String mapFieldName(String className, String fieldName){
		return className.replaceAll(".", "_")+"_"+fieldName;
	}
	
	/**
	 *  Map ClassName::methodName to a new MethodName.
	 *  
	 *  We do not need desc parameters here. 
	 * 
	 * @param className
	 * @param methodName
	 * @return
	 */
	public String mapMethodName(String className, String methodName){
		String tmp = methodName;
		if(methodName.equals("<init>")) tmp="init";
		return className.replaceAll(".", "_")+"_"+tmp;
	}
	
	private NameMappingService(){}
	
	public static NameMappingService get(){
		return _instance;
	}
	
	
	//Below are for user registered handlers for invokevirtual
	
	
	//Is it good to use String, instead of Class, as _mappers' keyt?  
	//Seems the String is not sufficient, especially when String (Class) is an interface..So i need add some additional information.
	//
	@Deprecated
	private Map<String, IHooker> _mappers = new LinkedHashMap<String ,IHooker>();
	private static IPlugin _plugin;
	
	final String PLUGIN_NAME = "-xplugin=";
		  
	/**
	 * 
	 * @param owner  Receiver class type
	 * @param methodName. it should be invoke_exact for method handle. 
	 * @param desc. MethodType. 
	 * @return
	 */
	public MethodNode mapping(String owner, String methodName, String desc, MethodContext context){
		return _mappers.get(owner).mapping(owner, methodName, desc, context);
	}
	
	
	public void register(PluginType type, IPlugin plugin){
		//_mappers.put(owner, iMap);
		_plugin = plugin;
	}
	
	public IPlugin getPlugin(){
		if(_plugin == null ){
			 RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
			  List<String> aList = bean.getInputArguments();
			  for(String str : aList){
				  if(str.toLowerCase().indexOf(PLUGIN_NAME)==0){
					  String type = str.substring(PLUGIN_NAME.length());
					  if(type.toLowerCase().equals("simple")){
						  _plugin = new SimplePlugin(PluginType.SIMPLE);
					  }else{
						  _plugin = new MethodHandlePlugin(PluginType.METHODHANDLE);
					  }
				  }
			  }
		}
		return _plugin;
	}
	
}

//All hooks will implement this method and register to the MappingService. 
interface IHooker{
	public MethodNode mapping(String owner, String methodName, String desc, MethodContext context);
}