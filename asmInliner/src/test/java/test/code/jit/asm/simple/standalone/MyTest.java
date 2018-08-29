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

package test.code.jit.asm.simple.standalone;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MyTest {

    public Object execute(Object... args){
        return run(args);
    }

    
    public Object run(int c, int d){
    	System.out.println("int , int ");
    	return c+d;
    }
    public Object run(int a, float b){
    	System.out.println("int , float ");
        return 0;
    }

    public String run(Integer a, String b){
    	System.out.println("Integer , String");
        return "ss";
    }
    public Object run(Object c, float m, int q){
    	System.out.println("Object, float, int ");
        return new Object();
    }

    public Object run(Object... args){
    	System.out.println("var args ..");
    	return new Object();
    }
    
    public Object run(Integer a, Integer b){
    	return a+b;
    }
    
	public static void main(String[] args){
		MyTest test = new MyTest();
//		test.execute(1,2);
//		test.execute(1,2.0f);
//		test.execute(1,"xuyshijie");
//		test.execute(new Object(),3.0f, 34);
		test.convert(1);
		test.convert(2.0f);
		test.convert(2.0d);
		test.convert(234533l);
	}
	
	public void MHTry(Object... args){
		try {
			MethodHandle mh = MethodHandles.publicLookup().findVirtual(MyTest.class, "run", MethodType.methodType(Object.class, Integer.class, Integer.class));
			Object obj = (Object)mh.invokeExact(this, args);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public void convert(Number x){
		System.out.println("it is "+x);
		
		if(Number.class.isAssignableFrom(Long.class)){
			System.out.println("as,,,");
		}
		
	}
	
	
}



