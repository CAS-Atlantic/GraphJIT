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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

//import code.jit.asm.backplane.BytecodeClass;


public class ASMUtil {
	
	public static final String DOT = ".";
	public static final String SLASH = "/";
	
	public static String getClassName(String owner){
//		if(slashName.indexOf(SLASH)!=-1){
//			Type type = Type.getObjectType(slashName);
//			//Type type = Type.getType(slashName);
//			return type.getClassName();	
//		}else{
//			return slashName;
//		}
		Type type = Type.getObjectType(owner);
		return type.getClassName();
		
		//return slashName.replaceAll("/", ".");
	}
	
	
	public static String getClassName(Type type){
		return type.getClassName();
	}
	
	public static String getOwnerName(String className){
		return className.replace('.', '/');
	}
	public static boolean isSlashName(String name){
		return name.indexOf(SLASH)!=-1;
	}
	
	
//	public static boolean parseClassNode(byte[] bytes, ClassNode node){
//		InputStream in = new ByteArrayInputStream(bytes);
//		try {
//			ClassReader reader = new ClassReader(in);
//			reader.accept(node, ClassReader.EXPAND_FRAMES);
//			return true;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return false;
//	}
	
	public static boolean parseClassNode(InputStream in, ClassNode node){
		try {
			ClassReader reader = new ClassReader(in);
			reader.accept(node, ClassReader.EXPAND_FRAMES);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static boolean parseClassNode(Class cls, ClassNode node){
		
		try {
			ClassReader reader = new ClassReader(cls.getResourceAsStream(Utils.buildResourceName(cls)));
			reader.accept(node, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static MethodNode getMethodNode(Class cls, String methodName, String desc){
		ClassNode classNode = new ClassNode();
		if(parseClassNode(cls.getClassLoader().getResourceAsStream(cls.getName()), classNode)){
			for(int i=0; i< classNode.methods.size(); i++){
				MethodNode node = (MethodNode) classNode.methods.get(i);
				if(node.name.equals(methodName) && (desc ==null || (desc!=null && node.desc.equals(desc)))){
					return node;
				}
			}	
		}
		return null;
	}
	
	
	public static MethodNode getMethodNode(ClassNode classNode,  String methodName, String desc){
		for(Object oMethod: classNode.methods){
			MethodNode method = (MethodNode)oMethod;
			if(method.name.equals(methodName) && method.desc.equals(desc)){
				return method;
			}
		}
		return null;
	}
	
	public static MethodNode getMethodNode(ClassNode classNode, String methodName){
		for(Object oMethod: classNode.methods){
			MethodNode method = (MethodNode)oMethod;
			if(method.name.equals(methodName)){
				return method;
			}
		}
		return null;
	}

	/**
	 * @param className
	 * @return
	 */
	public static String elimiteAnonymousClassName(String className) {
		if(className.contains(SLASH)){
			className = className.substring(0, className.lastIndexOf(SLASH));
		}
		
		return className;
	} 
	
	
	public static String refineInternalNameFromClass(Class cls){
		String origin = Type.getInternalName(cls);
		if(cls.getName().contains(SLASH)){
			return origin.substring(0, origin.lastIndexOf(SLASH));
		}
		return origin; 
	}
	
	public static boolean isStaticMethod(MethodNode node){
		return ((node.access>>3)&1)!=0;
	}
	
	public static void debug(MethodNode node){
		Iterator<AbstractInsnNode> iter = node.instructions.iterator();
		int i=0;
		while(iter.hasNext()){
			AbstractInsnNode instr = iter.next();
			System.err.println("\n"+(i++)+"  "+instr.getOpcode()+"  "+ instr.getClass());
			if(instr instanceof MethodInsnNode){
				MethodInsnNode method = (MethodInsnNode) instr;
				System.err.println("         =>" + method.getOpcode()+"  "+method.owner+"   "+ method.name);
			}else if(instr instanceof VarInsnNode){
				VarInsnNode varNode = (VarInsnNode) instr;
				System.err.println("         =>" + varNode.getOpcode()+"   "+ varNode.var);
			}
		}
	}
	
	public static String methodType2Desc(String methodType){
		//Type type = Type.get
		return methodType;
	}

}
