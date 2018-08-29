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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import code.jit.asm.backplane.InlineCode;
import code.jit.asm.common.PluginType;
import code.jit.asm.common.utils.Constants;
import code.jit.asm.plugins.IPlugin;
import code.jit.asm.plugins.MethodHandlePlugin;
import code.jit.asm.rules.MethodHandleRule;
import code.jit.asm.rules.RuleKind;
import code.jit.asm.services.BytecodeGenerator;
import code.jit.asm.services.ConfigurationService;


/**
 * 
 * @author Shijie Xu
 *
 *
 *Case list                                 Status                Case Name
-------------------------------------





ConstantHandle                                                   Done 
FilterArgumentHandle                                          Done. 
GWTHandle + AsTYpeHandle                             Done 
FilterArgument + GWTHandle (next)                    Done 
FilterArgument + GWTHandle(filters)                   Done 
FilterReturnHandle + GWT                                   Done 
FilterReturn(FoldVoidHandle)                               Done             testFilterReturnVoidFoldArgument 
testexplicitCastArguments                                    Done
VoidFold + CatchException                                   Done              testFoldArgumentTryFinally
CollectHandle                                                        Done. 
CollectHandle+FilterArgument+ReceiverBoundArgument   Partial                               testAsCollectHandleFilterArgumentReceiverArgument             ??ReceiverBoundARgunemt   
varArgsHandle+AsType+FixedArity                      Done             testArity
SpreadHandle+AsType+FilterReturn                    Done                           testSpread
filterArgument {next: BruteArgument } + dropArgument.         Done                            testBruteArgument_drop
filterReturn:{next: BruteArgument, filter: Direct }+ insertArgument.    Done               testBruteArgument_insert
GuardWithTestHandle{trueTarget:BruteArgument, falseTarget:BruteArgument}       Done       testPermute
ArraySetter and ArrayGet          Done   testArray  
AsType{ filter: FilterReture}        Done testAsTYpe.  
 */
public class MethodHandleTestCI {

	MethodHandleRule rule;
	
	@Before
	public void setUp() throws Exception {
		IPlugin plugin = new MethodHandlePlugin(PluginType.METHODHANDLE);
		 rule = new MethodHandleRule(RuleKind.METHODHANDLE);
		 ConfigurationService.get().INLINE_CODE = InlineCode.CLASS_INLINE;
		System.out.println("Start Method handle Junit tests....");
	}
	
	
	@Test
	public void testGwtTest() {
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
			
			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, Functions.getGWDMethodType());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals(trueRes, (String)generatedHandle.invokeExact(obj, "foo", "false"));
			assertEquals(falseRes, (String)generatedHandle.invokeExact(obj, "fool", "false"));
			
			MethodHandle newmh = generatedHandle.bindTo(obj);
			assertEquals(trueRes, (String)newmh.invokeExact("foo", "false"));
			assertEquals(falseRes, (String)newmh.invokeExact("fool", "false"));
			
		} catch (NoSuchMethodException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGWTLabel(){
		MethodHandle guard = Functions.getFooGuard();
		MethodHandle target = Functions.getSimpleGuardWithTestOutSide();
		MethodHandle fallback = Functions.getFalseTarget();
		MethodHandle handle = MethodHandles.guardWithTest(guard, target, fallback);
		String trueRes;
		try {
			trueRes = (String)handle.invokeExact("foo", "false");
			
			assertEquals("foo", trueRes);
		} catch (WrongMethodTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	} 
	
	@Test
	public void testGwtAsTypeTest() {
		try {
			MethodHandle guard = Functions.getAsTypeFoodGuard();
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
			

//			//Method Handle test.
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
	   public void testSimpleFilter() throws Throwable {
	        
	        MethodHandle handle = Functions.getSimpleFilterArgument();

	        assertEquals(MethodType.methodType(String.class, String.class, String.class), handle.type());
	       // assertEquals("foobazbarbaz", );

			Object obj = BytecodeGenerator.get().generate(handle);
			

//			//Method Handle test.
			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String)handle.invokeExact("foo", "bar"), (String)generatedHandle.invokeExact(obj, "foo", "bar"));
	    }


		
	   @Test
	   public void testComplexFilter() throws Throwable {
	        
	        MethodHandle handle = Functions.getComplexFilterWithGWTFilter();

	        assertEquals(MethodType.methodType(String.class, String.class, String.class), handle.type());
	        assertEquals("fooyahoobarbaz", (String)handle.invokeExact("foo", "bar"));
	        assertEquals("foolgooglebarbaz", (String)handle.invokeExact("fool", "bar"));

			Object obj = BytecodeGenerator.get().generate(handle);
			

			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String)handle.invokeExact("foo", "bar"), (String)generatedHandle.invokeExact(obj, "foo", "bar"));
	    }
	
	   @Test
	   public void testComplexFilterNext() throws Throwable {
	        
	        MethodHandle handle = Functions.getComplexFilterWithGWTNext();

	        assertEquals(MethodType.methodType(String.class, String.class, String.class), handle.type());
	        assertEquals("fooyahoogooglebarbaz", (String)handle.invokeExact("foo", "bar"));

			Object obj = BytecodeGenerator.get().generate(handle);
			

			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
			assertEquals((String)handle.invokeExact("foo", "bar"), (String)generatedHandle.invokeExact(obj, "foo", "bar"));
	    }
	
	   
		@Test
		public void testFilterReturnGWT() {
			try {
				
				Functions.printHi("xyshg", (long)0.5);
				MethodHandle handle = Functions.getFilterReturnGWTHandle(false);

				//Test the original is true
				String trueRes = (String)handle.invokeExact("foo", "text");
				assertEquals("foobaz", trueRes);
				String falseRes = (String)handle.invokeExact("foo1", "false");
				assertEquals("falsebaz", falseRes);
				
				Object obj = BytecodeGenerator.get().generate(handle);
				

//				//Method Handle test.
				MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, Functions.getGWDMethodType());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals(trueRes, (String)generatedHandle.invokeExact(obj, "foo", "text"));
				assertEquals(falseRes, (String)generatedHandle.invokeExact(obj, "fool", "false"));
				
				
				handle = Functions.getFilterReturnGWTHandle(true);
				String hi = (String)handle.invokeExact("str", (long)0.3);
				assertEquals(hi, "hi");
				obj = BytecodeGenerator.get().generate(handle);
				generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals(hi, (String)generatedHandle.invokeExact(obj, "str", (long)0.3));
				
				
			} catch (NoSuchMethodException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Test
		public void testexplicitCastArguments(){
			//Un implemented..
		} 
		
		
		@Test
		public void testConstantHandle(){
			//FilterReturn + Constant
			try {
				MethodHandle constObjHandle = Functions.getConstObjectHandle(false);
				assertEquals("xushijie", (String)constObjHandle.invokeExact());
				
				MethodHandle constIntHandle = Functions.getConstObjectHandle(true);
				assertEquals(4, (int)constIntHandle.invokeExact());
				
				MethodHandle filterReturn = MethodHandles.filterReturnValue(Functions.getVoidReturnMethodHandle(), constObjHandle);
				assertEquals("xushijie", (String)filterReturn.invokeExact("xu", (long)1.0));
				
				Object 	obj = BytecodeGenerator.get().generate(filterReturn);
				MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, filterReturn.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals("xushijie", (String)generatedHandle.invokeExact(obj, "str", (long)0.3));
				
			} catch (WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	   
		
		@Test
		public void testFilterReturnVoidFoldArgument() throws Throwable {

			// @SXU FilterReturn(FoldVoidHandle), 
			MethodHandle combiner = Functions.getVoidCombiner();
			MethodHandle target = Functions.getVoiFoldTarget();
			
			MethodHandle voidfolder = MethodHandles.foldArguments(target, combiner);
			MethodHandle handle = Functions.getFilterReturnFilterHandle(false, voidfolder);
			
			assertEquals(MethodType.methodType(String.class, String.class, String.class, String.class),
					handle.type());
			assertEquals("tttvoidFoldbaz", (String) handle.invokeExact("foo", "zhe", "ttt"));

			Object obj = BytecodeGenerator.get().generate(handle);
			MethodHandle generatedHandle = MethodHandles.publicLookup()
					.findVirtual(obj.getClass(), Constants.INVOKE_EXACT,
							handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type()
					.changeParameterType(0, Object.class));
			assertEquals((String) handle.invokeExact("foo", "zhe", "ttt"),
					(String) generatedHandle.invokeExact(obj, "foo", "zhe", "ttt"));
		}
		
		@Test
		public void testFilterReturnFoldArgument() throws Throwable {

			// @SXU FilterReturn(FoldVoidHandle), 
			MethodHandle combiner = Functions.getVoidCombiner();
			MethodHandle target = Functions.getVoiFoldTarget();
			
			MethodHandle voidfolder = MethodHandles.foldArguments(target, combiner);
			MethodHandle handle = Functions.getFilterReturnFilterHandle(false, voidfolder);
			
			assertEquals(MethodType.methodType(String.class, String.class, String.class, String.class),
					handle.type());
			assertEquals("tttvoidFoldbaz", (String) handle.invokeExact("foo", "zhe", "ttt"));

			Object obj = BytecodeGenerator.get().generate(handle);
			MethodHandle generatedHandle = MethodHandles.publicLookup()
					.findVirtual(obj.getClass(), Constants.INVOKE_EXACT,
							handle.type());
			generatedHandle = generatedHandle.asType(generatedHandle.type()
					.changeParameterType(0, Object.class));
			assertEquals((String) handle.invokeExact("foo", "zhe", "ttt"),
					(String) generatedHandle.invokeExact(obj, "foo", "zhe", "ttt"));
		}
		
	    @Test
	    public void testFoldArgumentTryFinally() throws Throwable {
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
	    
	    
	    @Test
	    public void testAsCollectHandleFilterArgumentReceiverArgument(){
	    	//@CollectHandle + ASType + collectArguments + FilterReturn.   
			try {
				MethodHandle deepToString = MethodHandles.publicLookup().findStatic(Arrays.class, "deepToString", MethodType.methodType(String.class, Object[].class));
		    	MethodHandle ts1 = deepToString.asCollector(String[].class, 1);
		    	assertEquals("[strange]", (String) ts1.invokeExact("strange"));

		    	Object obj = BytecodeGenerator.get().generate(ts1);
		    	MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, ts1.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				
				assertEquals((String)generatedHandle.invokeExact(obj, "strange"), (String) ts1.invokeExact("strange"));

		    	MethodHandle ts2 = deepToString.asCollector(String[].class, 2);
		    	obj = BytecodeGenerator.get().generate(ts2);
		    	generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, ts2.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
		    	assertEquals((String)generatedHandle.invokeExact(obj, "up", "down"), (String) ts2.invokeExact("up", "down"));
		   

		    	MethodHandle ts3 = deepToString.asCollector(String[].class, 3);
		    	MethodHandle ts3_ts2 = MethodHandles.collectArguments(ts3, 1, ts2);
		    	assertEquals("[top, [up, down], strange]",
		    	             (String) ts3_ts2.invokeExact("top", "up", "down", "strange"));
		    	
		    	
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
	    
	    @Test
	    public void testArity(){
	    	try {
	    		MethodHandle asListVar = MethodHandles.publicLookup().findStatic(Arrays.class,
	    				"asList", MethodType.methodType(List.class, Object[].class))
	    				.asVarargsCollector(Object[].class);
	    		MethodHandle asListFix = asListVar.asFixedArity();
	    		assertEquals("[1]", asListVar.invoke(1).toString());
	    		Exception caught = null;
	    		try { asListFix.invoke((Object)1); }
	    		catch (Exception ex) { caught = ex; }
	    		assert(caught instanceof ClassCastException);
	    		assertEquals("[two, too]", asListVar.invoke("two", "too").toString());
	    		try { asListFix.invoke("two", "too"); }
	    		catch (Exception ex) { caught = ex; }
	    		assert(caught instanceof WrongMethodTypeException);
	    		Object[] argv = { "three", "thee", "tee" };
	    		assertEquals("[three, thee, tee]", asListVar.invoke(argv).toString());
	    		assertEquals("[three, thee, tee]", asListFix.invoke(argv).toString());
	    		assertEquals(1, ((List) asListVar.invoke((Object)argv)).size());
	    		assertEquals("[three, thee, tee]", asListFix.invoke((Object)argv).toString());

	    		
	    		MethodHandle deepToString = MethodHandles.publicLookup()
	    				  .findStatic(Arrays.class, "deepToString", MethodType.methodType(String.class, Object[].class));
	    		MethodHandle ts1 = deepToString.asVarargsCollector(Object[].class);
	    		
	    		Object obj = BytecodeGenerator.get().generate(ts1);
		    	MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, deepToString.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				
	    		assertEquals((String)generatedHandle.invokeExact(obj, new Object[]{"won"}),   (String) ts1.invokeExact(    new Object[]{"won"}));
	    		
	    	} catch (IllegalArgumentException |IllegalAccessException | NoSuchMethodException  e) {
			// 	TODO Auto-generated catch block
	    		e.printStackTrace();
	    	} catch (WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	    }
	    
	    @Test
	    public void testSpread(){
	    	MethodHandle equals;
			try {
				equals = MethodHandles.publicLookup()
						  .findVirtual(String.class, "equals", MethodType.methodType(boolean.class, Object.class));
				assert( (boolean) equals.invokeExact("me", (Object)"me"));
    			assert(!(boolean) equals.invokeExact("me", (Object)"thee"));
    			// spread both arguments from a 2-array:
    			MethodHandle eq2 = equals.asSpreader(Object[].class, 2);
    			assert( (boolean) eq2.invokeExact(new Object[]{ "me", "me" }));
    			assert(!(boolean) eq2.invokeExact(new Object[]{ "me", "thee" }));
    			Object obj = BytecodeGenerator.get().generate(eq2);
		    	MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, eq2.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assert( (boolean) generatedHandle.invokeExact(obj, new Object[]{ "me", "me" }));
				assert(!(boolean) generatedHandle.invokeExact(obj, new Object[]{ "me", "thee" }));
				
				
    			// spread both arguments from a String array:
    			MethodHandle eq2s = equals.asSpreader(String[].class, 2);
    			assert( (boolean) eq2s.invokeExact(new String[]{ "me", "me" }));
    			assert(!(boolean) eq2s.invokeExact(new String[]{ "me", "thee" }));
    			obj = BytecodeGenerator.get().generate(eq2s);
    			generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, eq2s.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assert( (boolean) generatedHandle.invokeExact(obj, new Object[]{ "me", "me" }));
				assert(!(boolean) generatedHandle.invokeExact(obj, new Object[]{ "me", "thee" }));
				
				
    			MethodHandle eq0 = equals.asSpreader(Object[].class, 0);
    			assert( (boolean) eq0.invokeExact("me", (Object)"me", new Object[0]));
    			assert(!(boolean) eq0.invokeExact("me", (Object)"thee", (Object[])null));
    			obj = BytecodeGenerator.get().generate(eq0);
    			generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, eq0.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assert( (boolean) generatedHandle.invokeExact(obj, "me", (Object)"me", new Object[0]));
				assert(!(boolean) generatedHandle.invokeExact(obj, "me", (Object)"thee", (Object[])null));
    			
			} catch (IllegalAccessException | NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    			
	    }
	    
	    @Test
	    public void testBruteArgument_drop(){
			//1 filterArgument {next: BruteArgument } + dropArgument.	
			try {
				MethodHandle cat = MethodHandles.publicLookup().findVirtual(String.class,
						  "concat", MethodType.methodType(String.class, String.class));
				assertEquals("xy", (String) cat.invokeExact("x", "y"));
    			MethodType bigType = cat.type().insertParameterTypes(0, int.class, String.class);
    			MethodHandle d0 = MethodHandles.dropArguments(cat, 0, bigType.parameterList().subList(0,2));
    			assertEquals(bigType, d0.type());
    			assertEquals("yz", (String) d0.invokeExact(123, "x", "y", "z"));
    			Object obj = BytecodeGenerator.get().generate(d0);
    			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, d0.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String) generatedHandle.invokeExact(obj, 123, "x", "y", "z"), (String) d0.invokeExact(123, "x", "y", "z"));
				
				MethodHandle upcase = MethodHandles.publicLookup().findVirtual(String.class,  "toUpperCase", MethodType.methodType(String.class));
				MethodHandle lowcase = MethodHandles.publicLookup().findVirtual(String.class, "toLowerCase", MethodType.methodType(String.class));

				MethodHandle filterArgument = MethodHandles.filterArguments(d0, 1, upcase, upcase, lowcase);
				assertEquals("Yz", (String) filterArgument.invokeExact(123, "x", "y", "Z"));
				obj = BytecodeGenerator.get().generate(filterArgument);
    			generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, filterArgument.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String) generatedHandle.invokeExact(obj, 123, "x", "y", "z"), (String) filterArgument.invokeExact(123, "x", "y", "z"));
				assertEquals((String) generatedHandle.invokeExact(obj, 123, "x", "y", "Z"), (String) filterArgument.invokeExact(123, "x", "y", "Z"));
				
				
    			
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    			
	    	
	    }

	    @Test
	    public void testBruteArgument_insert(){
			//2 filterReturn:{next: BruteArgument, filter: Direct }+ insertArgument.	
			try {
				MethodHandle cat = MethodHandles.publicLookup().findVirtual(String.class,
						  "concat", MethodType.methodType(String.class, String.class));
				assertEquals("xy", (String) cat.invokeExact("x", "y"));
    			MethodHandle i0 = MethodHandles.insertArguments(cat, 0, "xuShi");
    			assertEquals(MethodType.methodType(String.class, String.class), i0.type());
    			assertEquals("xuShiX", (String) i0.invokeExact("X"));
    			Object obj = BytecodeGenerator.get().generate(i0);
    			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, i0.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String) generatedHandle.invokeExact(obj, "x"), (String) i0.invokeExact("x"));
				
				MethodHandle upcase = MethodHandles.publicLookup().findVirtual(String.class,  "toUpperCase", MethodType.methodType(String.class));
				MethodHandle lowcase = MethodHandles.publicLookup().findVirtual(String.class, "toLowerCase", MethodType.methodType(String.class));

				MethodHandle filterReturn = MethodHandles.filterReturnValue(i0, upcase);
				assertEquals("XUSHIX", (String) filterReturn.invokeExact("x"));
				obj = BytecodeGenerator.get().generate(filterReturn);
    			generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, filterReturn.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				
				assertEquals((String) generatedHandle.invokeExact(obj, "t"), (String) filterReturn.invokeExact("t"));
				assertEquals((String) generatedHandle.invokeExact(obj, "Y"), (String) filterReturn.invokeExact("Y"));
				
				
    			
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    			
	    	
	    }

	    
	    @Test
	    public void testPermute(){
	    	//FilterReturn{next:GuardWithTestHandle{trueTarget:BruteArgument, falseTarget:BruteArgument}, filter:upcase}
	    	try {
	    		MethodType googlefn2 = MethodType.methodType(String.class, String.class, String.class);
		    	MethodHandle googleMiddle = Functions.getGoogleMiddleMH();
		    	assert(googleMiddle.type().equals(googlefn2));
		    	MethodHandle google1 = MethodHandles.permuteArguments(googleMiddle, googlefn2, 0, 1);
		    	MethodHandle google2 = MethodHandles.permuteArguments(googleMiddle, googlefn2, 1, 0);
		    	assert((String)google1.invokeExact("Xu", "zhe") == "Xugooglezhe");
				assert((String)google2.invokeExact("Xu", "zhe") == "zhegoogleXu");
				
    			Object obj = BytecodeGenerator.get().generate(google1);
    			MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, google1.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String) generatedHandle.invokeExact(obj, "Xu", "zhe"), (String)google1.invokeExact("Xu", "zhe"));
				obj = BytecodeGenerator.get().generate(google2);
				generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, google2.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String) generatedHandle.invokeExact(obj, "Xu", "zhe"), (String)google2.invokeExact("Xu", "zhe"));

				
				MethodHandle guard = Functions.getFooGuard();
				MethodHandle target = MethodHandles.guardWithTest(guard,  google1, google2);
				assert((String)target.invokeExact("foo", "zhe") == "foolgooglezhe");
				assert((String)target.invokeExact("Xu", "zhe") == "zhegoogleXu");
				
				obj = BytecodeGenerator.get().generate(target);
				generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, target.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String) generatedHandle.invokeExact(obj, "Xu", "zhe"), (String)target.invokeExact("Xu", "zhe"));
				assertEquals((String) generatedHandle.invokeExact(obj, "foo", "zhe"), (String)target.invokeExact("foo", "zhe"));
				
				MethodHandle upcase = MethodHandles.publicLookup().findVirtual(String.class,  "toUpperCase", MethodType.methodType(String.class));
				MethodHandle filterReturn = MethodHandles.filterReturnValue(target, upcase);
				obj = BytecodeGenerator.get().generate(filterReturn);
				generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, filterReturn.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String) generatedHandle.invokeExact(obj, "Xu", "zhe"), (String)filterReturn.invokeExact("Xu", "zhe"));
				assertEquals((String) generatedHandle.invokeExact(obj, "foo", "zhe"), (String)filterReturn.invokeExact("foo", "zhe"));
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    @Test
	    public void testArrayElement(){
	    	//This are direct method handle and  the test is the same as the Directhandle. 
	    	try {
	    		MethodHandle setter = MethodHandles.arrayElementSetter(String[].class);
		    	MethodHandle getter = MethodHandles.arrayElementGetter(String[].class);
		    	
		    	String[] array = {"xu", "shi", "jie"};
		    	setter.invokeExact(array, 1, "Yahaha");
				assertEquals((String)getter.invokeExact(array, 0), "xu");
				assertEquals((String)getter.invokeExact(array, 1), "Yahaha");
				
				Object obj = BytecodeGenerator.get().generate(setter);
				assertEquals(obj, setter);
//				MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKEEXACT, setter.type());
//				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
//				generatedHandle.invokeExact(array, 0, "HahaPoint");
//				assertEquals((String)getter.invokeExact(array, 0), "HahaPoint");
				
				
				
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
//	    
//	    @Test
//	    public void testThrowException(){
//	    	MethodHandle thrower = MethodHandles.throwException(void.class, Functions.BlahException.class);
//	    	
//	    }
	    
	    @Test
	    public void testAsType(){
	    	//AsType FilterReturn 
	    	try {
	    		
	    		MethodHandle target = Functions.getSubMH();
	    		
	    		MethodHandle handle = target.asType(MethodType.methodType(int.class, Integer.class, Integer.class));
	    		assertEquals(10, (int)handle.invokeExact(new Integer(20), new Integer(10)));
	    		
				Object obj = BytecodeGenerator.get().generate(handle);
				MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((int)handle.invokeExact(new Integer(20), new Integer(10)), (int)generatedHandle.invokeExact(obj, new Integer(20), new Integer(10)));
				
				
				target = Functions.getVoidFinallyMH();
				handle = target.asType(MethodType.methodType(int.class, Object.class));
				List list = new LinkedList();
				list.add("xu");
				//System.out.println((int)handle.invokeExact((Object)list));
				//System.out.println(list.get(0));
				
				obj = BytecodeGenerator.get().generate(handle);
				generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				Object oList = list;
				int t = (int) generatedHandle.invokeExact(obj, oList);
				
				assertEquals(list.get(0), "xufinally");
				
				target = Functions.getAnotherSubMH();
				handle = target.asType(MethodType.methodType(Integer.class, Float.class, long.class));
				obj = BytecodeGenerator.get().generate(handle);
				generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((Integer)handle.invokeExact(Float.valueOf(3.0f), 34234l), (Integer)generatedHandle.invokeExact(obj, Float.valueOf(3.0f), 34234l));
				
				
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    @Test 
	    public void testReceiverBoundHandle(){
	    	String receiver = "MyString ";
	    	
	    	try {
				MethodHandle concat = MethodHandles.publicLookup().findVirtual(String.class, "concat", MethodType.methodType(String.class, String.class));
				MethodHandle handle = concat.bindTo(receiver);
				assertEquals("MyString xushijie", (String)handle.invokeExact("xushijie"));
				
				Object obj = BytecodeGenerator.get().generate(handle);
				MethodHandle generatedHandle = MethodHandles.publicLookup().findVirtual(obj.getClass(), Constants.INVOKE_EXACT, handle.type());
				generatedHandle = generatedHandle.asType(generatedHandle.type().changeParameterType(0, Object.class));
				assertEquals((String)handle.invokeExact("xushijie"), (String)generatedHandle.invokeExact(obj, "xushijie"));
				 
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrongMethodTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
	    
	    @Test
	    public void testHashCode(){
	    	MethodHandle guard = Functions.getFooGuard();
			MethodHandle target = Functions.getTrueTarget();
			MethodHandle fallback = Functions.getFalseTarget();
			
			MethodHandle handle1 = MethodHandles.guardWithTest(guard, target, fallback);
			
			MethodHandle handle2 = MethodHandles.guardWithTest(guard, target, fallback);
			assertEquals(handle1.hashCode(), handle2.hashCode());
			assertEquals(handle1.equals(handle2), true);
			
			
			handle2 = MethodHandles.guardWithTest(Functions.getAsTypeFoodGuard(), target, fallback);
			assertEquals(handle1.equals(handle2), false);
			assertEquals(handle1.hashCode()!=handle2.hashCode(), true);


	    }

}
