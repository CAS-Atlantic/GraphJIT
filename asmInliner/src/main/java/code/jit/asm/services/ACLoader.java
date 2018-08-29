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

import java.lang.reflect.Field;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import sun.misc.Unsafe;


/**
 * @author shijiex
 *
 */
@SuppressWarnings("restriction")
public class ACLoader {

	private static ACLoader _loader = new ACLoader();
	private Unsafe UNSAFE = getUnsafe();
	
	
	public static ACLoader getACLoader(){
		return _loader;
	}

	
	private Class loadClass(Class hoster, byte[] bytes){
		
		if(UNSAFE==null){
			throw new IllegalArgumentException("The sun.misc.Unsafe instance failures..");
		}
		return UNSAFE.defineAnonymousClass(hoster, bytes, null);
	}
	
	public Class<?> loadClass(byte[] bytes){
		return loadClass(this.getClass(), bytes);
	}
	
	public Class<?> loadClass(ClassNode classNode){
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return loadClass(cw.toByteArray());
	}
	
	private ACLoader() {
	}
	
	private static Unsafe getUnsafe(){
		Field f;
		try {
			f = Unsafe.class.getDeclaredField("theUnsafe");
 		    f.setAccessible(true);
	        Unsafe unsafe = (Unsafe) f.get(null);
	        return unsafe;

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

	
	
	
}
