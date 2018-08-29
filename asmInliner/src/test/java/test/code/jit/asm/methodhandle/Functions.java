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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;
import java.util.List;

import com.headius.invokebinder.Binder;


public class Functions {

	protected static final Lookup LOOKUP = MethodHandles.lookup();

    public String concatVirtual(String a, String b) {
        return a + b;
    }

    public static boolean isStringFoo(String a) {
        return a.equals("foo");
    }

    public static String addBar(String a) {
        return a + "bar";
    }

    public static String addBaz(String a) {
        return a + "baz";
    }

    public static void setZeroToFoo(String[] ary) {
        ary[0] = "foo";
    }

    public static void setZeroToFooAndRaise(String[] ary) throws BlahException {
        ary[0] = "foo";
        if(ary.length!=6){
        	throw new BlahException();	
        }
        
    }

    public static int setZeroToFooReturnInt(String[] ary) {
        ary[0] = "foo";
        return 1;
    }

    public static int setZeroToFooReturnIntAndRaise(String[] ary) throws BlahException {
        ary[0] = "foo";
        throw new BlahException();
    }

    public static void finallyLogic(String[] ary) {
        ary[0] = ary[0] + "finally";
    }

    /**
     *  Below two with methodtype (String)* is used for FoldVoid Test.. We have some bugs in handling Array type currently.
     * @param ary
     */
    public static void finallyLogic(List ary) {
    	System.out.println("Finally: "+ary.get(0).toString());
        ary.set(0, ary.get(0)+"finally");
    }
    public static void setZeroToFooAndRaise(List ary) throws BlahException {
    	ary.set(0, "foo");
        throw new BlahException();
    }

    public static void cacthHandle(BlahException be, List ary){   //BlahException
    	System.out.println("catchHandle: "+ be.toString());
    	ary.add(be.toString());
    }


    public void voidFoldCombiner(String[] ary){

    }

    public  void foldTarget(String[] ary) {
        ary[0] = ary[0] + "finally";
    }


    public static String[] varargs(String arg0, String... args) {
        return args;
    }

    public static class BlahException extends Exception {}

    public static class Fields {
        public String instanceField = "initial";
        public static String staticField = "initial";
    }

    /**
     * Represents a constructable object that's always equal to other constructables.
     */
    public static class Constructable {
        private final String a, b;
        public Constructable(String a, String b) {
            this.a = a;
            this.b = b;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Constructable)) return false;
            Constructable c = (Constructable)other;
            return a.equals(c.a) && b.equals(c.b);
        }
    }

    public static String alwaysYahooStatic(String ignored) {
        return "yahoo";
    }

    public String alwaysYahooVirtual(String ignored) {
        return "yahoo";
    }


    //This is for Guard WithTest Handle
    public static String printTrueTarget(String a, String b){
    	System.out.println("True Target: "+ a+"  "+b);
    	return a;
    }

    public static  String printFalseTarget(String a, String b){
    	System.out.println("False targetL "+a+"  "+b);
    	return b;
    }


    public static String addYahoo(String a){
    	return a+"yahoo";
    }
    
    public static String addYahoo(String a, String b){
    	return a+"yahoo"+b;
    }
    
    public static String addGoogle(String a){
    	return a+"google";
    }
    public static String addGoogle(String a, String b){
    	return a+"google"+b;
    }
    
    public static int sub(int a, int b){
    	return a-b;
    }

    public static String strCount(Object[] array){
    	return array.length+"hello";
    }

    public static int intCount(Object[] array){
    	return array.length;
    }


    /////////////////////////////////////////////////////////
    
    public static boolean isFooString(String str){
    	//System.out.println("1");
    	 boolean flag = str.equals("foo");
    	 System.out.println("in Functions isFooString "+ flag);
    	//System.out.println("2");
    	return flag;
    }
    
    public static boolean isFoolObject(Object a){
    	if(a instanceof String){
    		return isFooString((String)a);
    	}
    	return false;
    }
    
    public static MethodHandle getFooGuard(){
    	MethodHandle p = null;
    	try {
			 return LOOKUP.findStatic(Functions.class, "isFooString", MethodType.methodType(boolean.class, String.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    
 
    public static MethodHandle getAsTypeFoodGuard(){
    	
		try {
			MethodHandle handle = LOOKUP.findStatic(Functions.class, "isFoolObject", MethodType.methodType(boolean.class, Object.class));
			return handle.asType(handle.type().changeParameterType(0, String.class));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    public static MethodHandle getTrueTarget(){
    	try {
			return LOOKUP.findStatic(Functions.class, "printTrueTarget", MethodType.methodType(String.class, String.class, String.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
    }

    
    public static MethodHandle buildComplicateTarget(){
    	return getFilterReturnGWTHandle(false);    	
    }
    
    public static MethodHandle getFalseTarget(){
    	try {
			return LOOKUP.findStatic(Functions.class, "printFalseTarget", MethodType.methodType(String.class, String.class, String.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    
    
    public static MethodType getGWDMethodType(){
    	return MethodType.methodType(String.class, String.class, String.class);
    }

    
    ////////////////////////////////////////////

    public static String mixed(String a, int b) {
    	return a;
    }

    public static void mixed(String a, int b, float c){
    	System.out.println("Data are: "+a+" "+b+" "+c);
    }
    
    
    public static MethodHandle mixedHandle() throws Exception {
    	return MethodHandles.publicLookup().findStatic(Functions.class, "mixed", MethodType.methodType(void.class, String.class, int.class, float.class));

    }
    
    public static MethodHandle mixedStringdHandle() throws Exception {
    	return MethodHandles.publicLookup().findStatic(Functions.class, "mixed", MethodType.methodType(String.class, String.class, int.class));
    }
    
    
    //////////////////////////////////////////////////
    
    public static String concat3String(String a, String b, String c){
    	return a+b+c;
    }

    public static String concatStatic(String a, String b) {
        return a + b;
    }

    public static String concatStatic(String a, CharSequence b) {
        return a + b;
    }

    public String stringIntegersString2(String a, Integer[] bs, String c) {
        return Arrays.deepToString(new Object[]{a, bs, c});
    }

    public static MethodHandle concatHandle() throws Exception {
        return LOOKUP.findStatic(Subjects.class, "concatStatic", MethodType.methodType(String.class, String.class, String.class));
    }

    public static MethodHandle concatCharSequenceHandle() throws Exception {
        return LOOKUP.findStatic(Subjects.class, "concatStatic", MethodType.methodType(String.class, String.class, CharSequence.class));
    }

    public static MethodHandle concat3StringHandle() throws Exception{
    	return LOOKUP.findStatic(Subjects.class, "concat3String", MethodType.methodType(String.class, String.class, String.class, String.class));
    }

    public static MethodHandle concatYahooFold() throws Exception{
    	return LOOKUP.findStatic(Subjects.class, "addYahoo4Fold", MethodType.methodType(String.class, String.class));
    }
    
    
    /////////////////////////////////////////////////////

    public static String intLong(int a, long b) {
    	System.out.println(a+"  "+b);
        return "intLong ok";
    }

    public static MethodHandle intLongHandle() throws IllegalAccessException, NoSuchMethodException {
        return MethodHandles.lookup().findStatic(Functions.class, "intLong", MethodType.methodType(String.class, int.class, long.class));
    }
    
    ///////////////////////////////////
    
    public static MethodHandle getAddBazFilter() throws Exception{
    	return MethodHandles.lookup().findStatic(Functions.class, "addBaz", MethodType.methodType(String.class, String.class));
    }

    
    /////////////////////////////////////////////////////////////
    public static void combiner(String a, String b){
    	System.out.println("The voidCombiner receives "+ a + " and "+b);
    } 
    
    public static String getVoidFoldTarget(String a, String b, String c){
    	System.out.println("I will drop " + a + "  "+ b);
    	return c+"voidFold";
    }
    
    public static MethodHandle getVoidCombiner() throws Exception{
    	return MethodHandles.lookup().findStatic(Functions.class, "combiner", MethodType.methodType(void.class, String.class, String.class));
    }
    
    public static MethodHandle getVoiFoldTarget() throws Exception{
    	return MethodHandles.lookup().findStatic(Functions.class, "getVoidFoldTarget", MethodType.methodType(String.class, String.class, String.class, String.class));
    }
    
    
    ///////////////////////////////////////////
    public static MethodHandle getCatchException()  {
    	try {
			return MethodHandles.publicLookup().findStatic(Functions.class, "cacthHandle", MethodType.methodType(void.class, BlahException.class, List.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }


    public static MethodHandle getZeroToFooAndRaise()  {
    	try {
			return MethodHandles.publicLookup().findStatic(Functions.class, "setZeroToFooAndRaise", MethodType.methodType(void.class, List.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public static MethodHandle getFinallyTarget()  {
    	try {
			return MethodHandles.publicLookup().findStatic(Functions.class, "finallyLogic", MethodType.methodType(void.class, List.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

    
    /////////////////////////////////////////////////////////////////////

    public static MethodHandle getSimpleFilterArgument(){
    	try{
    		MethodHandle target = concatHandle();
            MethodHandle filter = LOOKUP.findStatic(Functions.class, "addBaz", MethodType.methodType(String.class, String.class));
            
            MethodHandle handle = Binder
                    .from(String.class, String.class, String.class)
                    .filter(0, filter, filter)
                    .invoke(target);
            return handle;
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return null;
    }

    //FilterArgument + GuardWithTest(filters)
   public static MethodHandle getComplexFilterWithGWTFilter(){
   	try{
		MethodHandle target = concatHandle();
        MethodHandle filter = LOOKUP.findStatic(Functions.class, "addBaz", MethodType.methodType(String.class, String.class));
        MethodHandle filter1 = getSimpleGuardWithTestInside();
        MethodHandle handle = Binder
                .from(String.class, String.class, String.class)
                .filter(0, filter1, filter)
                .invoke(target);
        return handle;
	}catch(Exception e){
		e.printStackTrace();
	}
	return null;
	    }
    

   public static MethodHandle getComplexFilterWithGWTNext(){
	   	try{
	   		MethodHandle next = get2GuardWithTestInside();
			//MethodHandle target = concatHandle();
	        MethodHandle filter = LOOKUP.findStatic(Functions.class, "addBaz", MethodType.methodType(String.class, String.class));
	        MethodHandle filter1 = getSimpleGuardWithTestInside();
	        MethodHandle handle = Binder
	                .from(String.class, String.class, String.class)
	                .filter(0, filter1, filter)
	                .invoke(next);
	        return handle;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
		    }

   
   private static MethodHandle getSimpleGuardWithTestInside(){
	   MethodHandle guard = Functions.getFooGuard();
	   MethodHandle trueTarget = Functions.getAddYahooHandle();
	   MethodHandle falseTarget = Functions.getAddGoogleHandle();
	   return MethodHandles.guardWithTest(guard, trueTarget, falseTarget);
   }
   
   private static MethodHandle get2GuardWithTestInside(){
	   MethodHandle guard = Functions.getFooGuard();
	   MethodHandle trueTarget = Functions.getAddYahooHandle2();
	   MethodHandle falseTarget = Functions.getAddGoogleHandle2();
	   return MethodHandles.guardWithTest(guard, trueTarget, falseTarget);
   }
   
   private static MethodHandle getAddYahooHandle2(){
	   try {
			return LOOKUP.findStatic(Functions.class, "addYahoo", MethodType.methodType(String.class, String.class, String.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	return null;
   }
   
   private static MethodHandle getAddYahooHandle(){
	   try {
			return LOOKUP.findStatic(Functions.class, "addYahoo", MethodType.methodType(String.class, String.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	return null;
   }
   
   private static MethodHandle getAddGoogleHandle2(){
	   try {
			return LOOKUP.findStatic(Functions.class, "addGoogle", MethodType.methodType(String.class, String.class, String.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	return null;
   }
   
   private static MethodHandle getAddGoogleHandle(){
	   try {
			return LOOKUP.findStatic(Functions.class, "addGoogle", MethodType.methodType(String.class, String.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	return null;
   }
   
   public static MethodHandle getSimpleGuardWithTestOutSide(){
	   MethodHandle guard = Functions.getFooGuard();
	   MethodHandle trueTarget = Functions.getTrueTarget();
	   MethodHandle falseTarget = Functions.getFalseTarget();
	   return MethodHandles.guardWithTest(guard, trueTarget, falseTarget);
   }
   
      
   ///////////////////////////////////
   
   public static MethodHandle getFilterReturnGWTHandle(boolean isNextVoid){
	   if(isNextVoid){
		   MethodHandle next = getVoidReturnMethodHandle();
		   MethodHandle filter = getEmptyFilterMethodHandle();
		   return MethodHandles.filterReturnValue(next, filter);
	   }else{
		   
		try {
			MethodHandle next = getSimpleGuardWithTestOutSide();
			MethodHandle filter = Functions.getAddBazFilter();
			return MethodHandles.filterReturnValue(next, filter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		   
	   }
	   return null;
   }
   
   public static void printHi(String a, long b){
	   System.out.println(" Say hi to "+a+" " +b);
   } 
   
   
   public static MethodHandle getVoidReturnMethodHandle(){
	   try {
		return MethodHandles.publicLookup().findStatic(Functions.class, "printHi", MethodType.methodType(void.class, String.class, long.class));
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   return null;
   }
   
   public  static String returnVoidMessage(){
	   return "hi";
   }
   private static MethodHandle getEmptyFilterMethodHandle(){
	   try {
		return MethodHandles.publicLookup().findStatic(Functions.class, "returnVoidMessage", MethodType.methodType(String.class));
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   return null;
   }

   public static MethodHandle getConstObjectHandle(boolean isPrimite) {
	   if(!isPrimite){
		   return MethodHandles.constant(String.class, "xushijie");   
	   }else{
		   return MethodHandles.constant(int.class, 4);
	   }
	   
   }
   
   
   public static MethodHandle getFilterReturnFilterHandle(boolean isNextVoid, MethodHandle givenNext){
	   if(isNextVoid){
		   MethodHandle next = getVoidReturnMethodHandle();
		   MethodHandle filter = getEmptyFilterMethodHandle();
		   return MethodHandles.filterReturnValue(next, filter);
	   }else{
		   
		try {
			MethodHandle next = givenNext==null?getSimpleGuardWithTestOutSide():givenNext;
			MethodHandle filter = Functions.getAddBazFilter();
			return MethodHandles.filterReturnValue(next, filter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		   
	   }
	   return null;
   }
 
   
   ///////////////////////////////////////////////////////
   
   public static int anotherSub(float a, Long b){
	   return  Float.valueOf(a).intValue() - b.intValue() ;
   }

   public static MethodHandle getGoogleMiddleMH(){
	   try {
		return MethodHandles.publicLookup().findStatic(Functions.class, "addGoogle", MethodType.methodType(String.class, String.class, String.class));
	} catch (IllegalAccessException | NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   return null;
   }
   
   public static MethodHandle getSubMH(){
	   try {
			return MethodHandles.publicLookup().findStatic(Functions.class, "sub", MethodType.methodType(int.class, int.class, int.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   return null;
   }
   
   public static MethodHandle getAnotherSubMH(){
	   try {
			return MethodHandles.publicLookup().findStatic(Functions.class, "anotherSub", MethodType.methodType(int.class, float.class, Long.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   return null;
   }
   
   public static MethodHandle getVoidFinallyMH(){
	   
	   try {
			return MethodHandles.publicLookup().findStatic(Functions.class, "finallyLogic", MethodType.methodType(void.class, List.class));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	   
   }
   
}
