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

package test.code.jit.asm.methodhandle;

import static org.junit.Assert.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.tree.MethodNode;

import test.code.jit.asm.methodhandle.Functions.BlahException;

import com.headius.invokebinder.Binder;

import code.jit.asm.common.Blob;
import code.jit.asm.common.PluginType;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.plugins.IPlugin;
import code.jit.asm.plugins.MethodHandlePlugin;
import code.jit.asm.rules.MethodHandleRule;
import code.jit.asm.rules.RuleKind;
import code.jit.asm.services.BytecodeGenerator;

public class MethodHandleTest {

	MethodHandleRule rule;
	
	@Before
	public void setUp() throws Exception {
		IPlugin plugin = new MethodHandlePlugin(PluginType.METHODHANDLE);
		 rule = new MethodHandleRule(RuleKind.METHODHANDLE);
		System.out.println("Start Method handle Junit tests....");
	}

	
	@Test
	public void testGWTHandle_MI(){
			//@SXU: GWDHandle
			try {
				MethodHandle guard = Functions.getFooGuard();
				MethodHandle target = Functions.getTrueTarget();
				MethodHandle fallback = Functions.getFalseTarget();
				
				MethodHandle handle = MethodHandles.guardWithTest(guard, target, fallback);

				//Test the original is true
				String trueRes = (String)handle.invokeExact("foo", "false");
				assertEquals("foo", trueRes);
				String falseRes = (String)handle.invokeExact("foo1", "false");
				assertEquals("false", falseRes);
				
				//init the Rule.
				String className = "TGWD"; 
				String methodName = "sayHello";
				//Object generatedMH = handle.get("TGWD", "sayHello");
				//rule.register(generatedMH.getClass(), methodName);
				Object obj = BytecodeGenerator.get().generate(handle);
				

//				//Method Handle test.
				MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, Functions.getGWDMethodType());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals(trueRes, (String)generatedHandle.invokeExact(obj, "foo", "false"));
				assertEquals(falseRes, (String)generatedHandle.invokeExact(obj, "fool", "false"));
				
			} catch (NoSuchMethodException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	@Test
	public void testBruteArgument_MI(){
		//@SXU BruteArgumentMoverHandle + InsertHandle+Permutehandle
	      
		try {
			MethodHandle target = Subjects.concatHandle();

	        Binder binder1 = Binder
	                .from(String.class, String.class, Object.class)
	                .drop(1);

	        MethodHandle handle = Binder
	                .from(binder1)
	                .insert(1, "world")
	                .invoke(target);

	        
	        assertEquals(MethodType.methodType(String.class, String.class, Object.class), handle.type());
	        String res = (String) handle.invokeExact("Hello, ", new Object());
	        
	        String ExpectedRes = "Hello, world";
	        assertEquals(ExpectedRes, res);
	        
			Object obj = BytecodeGenerator.get().generate(handle);

			//			//Method Handle test.
			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals(ExpectedRes, (String)generatedHandle.invokeExact(obj, "Hello, ", new Object()));
				        
			//Another one. 
	        MethodHandle target3 = Subjects.concat3StringHandle();
	        MethodHandle handle3 = Binder
	                .from(String.class, String.class, String.class)
	                .insert(2,  "hello, ")
	                .invoke(target3);
	        
	        assertEquals(MethodType.methodType(String.class, String.class, String.class), handle3.type());
	        //System.out.println("Result: "+ (String) handle3.invokeExact("xu","zhe"));
	        assertEquals("xuzhehello, ", (String) handle3.invokeExact("xu", "zhe"));
	        
	        obj = BytecodeGenerator.get().generate(handle3);
	        generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle3.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String) handle3.invokeExact("xu", "zhe"), (String)generatedHandle.invokeExact(obj, "xu", "zhe"));
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testCast_MI(){
		//AsTypeHandle. and ExplictCastHandle
		MethodHandle target;
		try {
			//@SXU AsTypeHandle
			target = Functions.mixedStringdHandle();
			MethodHandle handle = Binder
		                .from(String.class, Object.class, Integer.class)
		                .convert(target.type())
		                .invoke(target);

		     assertEquals(MethodType.methodType(String.class, Object.class, Integer.class), handle.type());
		     assertEquals("foo", (String) handle.invokeExact((Object) "foo", (Integer) 5));

	        Object obj = BytecodeGenerator.get().generate(handle);
		    MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String) handle.invokeExact((Object) "foo", (Integer) 5), (String)generatedHandle.invokeExact(obj, (Object) "foo", (Integer) 5));

			
			//////////////2 SXU. ExplictCastHandle.
			 target = Functions.mixedStringdHandle();
		     handle = Binder
		                .from(String.class, Object.class, byte.class)
		                .cast(target.type())
		                .invoke(target);

		     assertEquals(MethodType.methodType(String.class, Object.class, byte.class), handle.type());
		     assertEquals("foo", (String)handle.invokeExact((Object)"foo", (byte)5));
		     
		     obj = BytecodeGenerator.get().generate(handle);
			 generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			 generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			 assertEquals((String) handle.invokeExact((Object) "foo", (byte) 5), (String)generatedHandle.invokeExact(obj, (Object) "foo", (byte) 5));
		     
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFrom_MI() throws Throwable{
		 MethodHandle target = Functions.concatHandle();
		 //@SXU: BRuteArgumentMover.
         Binder binder1 = Binder
	                .from(String.class, String.class, Object.class)
	                .drop(1);

	        MethodHandle handle = Binder.from(binder1)
	                .insert(1, "world")
	                .invoke(target);
	        
	        assertEquals(MethodType.methodType(String.class, String.class, Object.class), handle.type());
	        assertEquals("Hello, world", (String) handle.invokeExact("Hello, ", new Object()));
	        
	        Object obj = BytecodeGenerator.get().generate(handle);
		    MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String) handle.invokeExact("Hello, ", new Object()), (String)generatedHandle.invokeExact(obj, "Hello, ", new Object()));
	}

	@Test
	public void testInsert_MI() throws Throwable{
		MethodHandle target = Functions.intLongHandle();
		//@SXU: BruteARgument..
        MethodHandle handle = Binder
                .from(String.class)
                .insert(0, new Class[]{int.class, long.class}, 2, 1L)
                .invoke(target);

        assertEquals(MethodType.methodType(String.class), handle.type());
        assertEquals("intLong ok", (String) handle.invokeExact());

        Object obj = BytecodeGenerator.get().generate(handle);
	    MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
		generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
		assertEquals((String) handle.invokeExact(), (String)generatedHandle.invokeExact(obj));
	}
	
	 @Test
	   public void testFilter_MI() throws Throwable {
		 //@SXU FilterArgument
	        MethodHandle target = Functions.concatHandle();
	        MethodHandle filter = Functions.getAddBazFilter();
	        MethodHandle handle = Binder
	                .from(String.class, String.class, String.class)
	                .filter(0, filter, filter)
	                .invoke(target);

	        assertEquals(MethodType.methodType(String.class, String.class, String.class), handle.type());
	        assertEquals("foobazbarbaz", (String)handle.invokeExact("foo", "bar"));

	        Object obj = BytecodeGenerator.get().generate(handle);
		    MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String)handle.invokeExact("foo", "bar"), (String)generatedHandle.invokeExact(obj, "foo", "bar"));
			
			// @SXU This obj should be equal with previosu one.
//			obj = BytecodeGenerator.get().generator(handle);
//			System.out.println(obj.toString());
		
	    }
	 
	 @Test
     public void testFold_MI() throws Throwable {
		   
	    //@SXU  FoldNonVoidHandle, ConstantObjectHandle.
	        MethodHandle target = Functions.concatHandle();
	        MethodHandle fold = Binder
	                .from(String.class, String.class)
	                .drop(0)
	                .constant("yahoo");
	        MethodHandle handle = Binder
	                .from(String.class, String.class)
	                .fold(fold)
	                .invoke(target);

	        assertEquals(MethodType.methodType(String.class, String.class), handle.type());
	        assertEquals("yahoofoo", (String)handle.invokeExact("foo"));

	        Object obj = BytecodeGenerator.get().generate(handle);
		    MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String)handle.invokeExact("foo"), (String)generatedHandle.invokeExact(obj, "foo"));
	    }
	 
	@Test
	public void testVoidFold_MI() throws Throwable {

		// @SXU FoldVoidHandle, ConstantObjectHandle.
		MethodHandle combiner = Functions.getVoidCombiner();
		MethodHandle target = Functions.getVoiFoldTarget();
		MethodHandle voidfolder = MethodHandles.foldArguments(target, combiner);
		
		assertEquals(MethodType.methodType(String.class, String.class, String.class, String.class),
				voidfolder.type());
		assertEquals("tttvoidFold", (String) voidfolder.invokeExact("foo", "zhe", "ttt"));

		Object obj = BytecodeGenerator.get().generate(voidfolder);
		MethodHandle generatedHandle = MethodHandles.publicLookup()
				.findVirtual(obj.getClass(), Constants.INVOKE_EXACT,
						voidfolder.type());
		generatedHandle = generatedHandle.asType(generatedHandle.type()
				.changeParameterType(0, Object.class));
		assertEquals((String) voidfolder.invokeExact("foo", "zhe", "ttt"),
				(String) generatedHandle.invokeExact(obj, "foo", "zhe", "ttt"));
	}
	   
	    @Test
	    public void testFoldStatic_MI() throws Throwable {
	    	
	    	   //@SXU  FoldNonVoidHandle
//	        MethodHandle target = Functions.concatHandle();
//	        MethodHandle handle = Binder
//	                .from(MethodHandles.lookup(), String.class, String.class)
//	                .foldStatic(Functions.class, "alwaysYahooStatic")
//	                .invoke(target);
//
//	        assertEquals(MethodType.methodType(String.class, String.class), handle.type());
//	        assertEquals("yahoofoo", (String)handle.invokeExact("foo"));
//
//	        Object obj = BytecodeGenerator.get().generator(handle);
//	        MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKEEXACT, handle.type());
//			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
//			assertEquals((String)handle.invokeExact("foo"), (String)generatedHandle.invokeExact(obj, "foo"));
	    }
	    
	    @Test
	    public void testThrowException_MI() throws Throwable {
	        MethodHandle handle = Binder
	                .from(void.class, BlahException.class)
	                .throwException();

	        assertEquals(MethodType.methodType(void.class, BlahException.class), handle.type());
	        try {
	            handle.invokeExact(new BlahException());
	            assertTrue("should not reach here", false);
	        } catch (BlahException be) {
	        }
	        
//	        Object obj = BytecodeGenerator.get().generator(handle);
//	        MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKEEXACT, handle.type());
//			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
//			try{
//				generatedHandle.invokeExact(new BlahException());
//				assertEquals(true, false);
//			}catch (BlahException be1) {
//				assertEquals(true, true);
//	        }
	        
	    }
	    
	    @Test
	    public void testSimpleCatchException_MI(){
	    	MethodHandle tryTarget = Functions.getZeroToFooAndRaise();
	    	MethodHandle catchHandle = Functions.getCatchException();
	    	Class throwableClass = catchHandle.type().parameterType(0);
	    	
	    	MethodHandle handle = MethodHandles.catchException(tryTarget, throwableClass, catchHandle);

	    	List<String> strs = new LinkedList<String>();
	    	strs.add("xu");
	    	try {
				handle.invokeExact(strs);
			} catch (Throwable e) {
				e.printStackTrace();
			}

	    	assertEquals(strs.get(0), "foo");
	    	assertEquals(strs.get(1), throwableClass.getName());
	    	//////////////////////////////////
	    	{
	    		// This is used to confirm the template is correct..
	    		strs.clear();
		    	strs.add("xu");
	    	
	    	}
	    	
	    	////////////////////////////////////////
	    	// Now I go to verify my compiler. 
	    	
	    	strs.clear();
	    	strs.add("xu");

	    	
	    	//////////////////////
			try {
				Object obj = BytecodeGenerator.get().generate(handle);
 			    MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				generatedHandle.invokeExact(obj, strs);
				assertEquals(strs.get(0), "foo");
		    	assertEquals(strs.get(1), throwableClass.getName());

			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  
			 
	    }
	    
	    @Test
	    public void testTryFinally2() throws Throwable {
	    	//FoldVoid. + CatchException + DirectHandle
	        MethodHandle next = Functions.getFinallyTarget();

	        MethodHandle combiner = null;
	        {
	        	MethodHandle tryTarget = Functions.getZeroToFooAndRaise();
		    	MethodHandle catchHandle = Functions.getCatchException();
		    	Class throwableClass = catchHandle.type().parameterType(0);
		    	combiner = MethodHandles.catchException(tryTarget, throwableClass, catchHandle);
	        }
	         
	        MethodHandle handle = MethodHandles.foldArguments(next, combiner);
	        
	        // I comment this because there is bugs in the TryFinally Implementation. 
//	        MethodHandle handle = Binder
//	                .from(void.class, List.class)
//	                .tryFinally(combiner)
//	                .invokeStatic(MethodHandles.lookup(), Functions.class, "setZeroToFooAndRaise");

	        assertEquals(MethodType.methodType(void.class, List.class), handle.type());
	        List<String> stringAry = new LinkedList<String>();
	        stringAry.add("sxu");
	       
	        handle.invokeExact(stringAry);
	        assertEquals(stringAry.get(0), "foofinally");
	        assertEquals(stringAry.get(1), "test.code.jit.asm.methodhandle.Functions$BlahException");
	       
	    	//////////////////////
			try {
				 stringAry = new LinkedList<String>();
			     stringAry.add("sxu");
				Object obj = BytecodeGenerator.get().generate(handle);
 			    MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				generatedHandle.invokeExact(obj, stringAry);
				assertEquals(stringAry.get(0), "foofinally");
		        assertEquals(stringAry.get(1), "test.code.jit.asm.methodhandle.Functions$BlahException");
		       
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

}
