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
import java.util.Arrays;

import com.headius.invokebinder.Binder;


/**
 * Created by headius on 1/25/14.
 */
public class Subjects {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

//    public static final Signature StringIntegerIntegerIntegerString = Signature
//            .returning(String.class)
//            .appendArg("a", String.class)
//            .appendArg("b1", Integer.class)
//            .appendArg("b2", Integer.class)
//            .appendArg("b3", Integer.class)
//            .appendArg("c", String.class);
//
//    public static final Signature StringIntegerIntegerInteger = Signature
//            .returning(String.class)
//            .appendArg("a", String.class)
//            .appendArg("b1", Integer.class)
//            .appendArg("b2", Integer.class)
//            .appendArg("b3", Integer.class);
//
//    public static final Signature StringIntegersString = Signature
//            .returning(String.class)
//            .appendArg("a", String.class)
//            .appendArg("bs", Integer[].class)
//            .appendArg("c", String.class);

    public static final MethodHandle StringIntegersStringHandle = Binder
                    .from(String.class, String.class, Integer[].class, String.class)
                    .invokeStaticQuiet(LOOKUP, Subjects.class, "stringIntegersString");

    public static final MethodHandle StringIntegersHandle = Binder
            .from(String.class, String.class, Integer[].class)
            .invokeStaticQuiet(LOOKUP, Subjects.class, "stringIntegers");

    public static String stringIntegersString(String a, Integer[] bs, String c) {
        return Arrays.deepToString(new Object[]{a, bs, c});
    }

    public static String stringIntegers(String a, Integer[] bs) {
        return Arrays.deepToString(new Object[]{a, bs});
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

    public static void voidFoldFun(String str){
    	System.out.println("voidFoldFun: "+ str);
    }

    public static String addYahoo4Fold(String str){
    	return "yahoo"+str;
    }
}
