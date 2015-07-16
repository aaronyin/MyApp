package com.buaair.carsmart.utils;

import android.util.Log;

public class LogUtil {

	private static boolean debug = true;

	public static void d(String msg) {
		if (debug) {
			Log.d(getCaller(), msg);
		}
	}
	
	public static void e(String msg){
		if (debug) {
			Log.e(getCaller(), msg);
		}
	}
	
	public static void e(String msg,Throwable tr){
		if (debug) {
			Log.e(getCaller(), msg, tr);
		}
	}

	private static String getCaller() {
		StackTraceElement stack[] = Thread.currentThread().getStackTrace();
//		for (int i = 0 ; i < stack.length; i ++) {
//			StackTraceElement ste = stack[i];
//			System.out.println(i+" :  called by " + ste.getClassName() + "."
//					+ ste.getMethodName() + "/" + ste.getFileName());
//			
//		}
		return stack[4].getFileName();
	}

}
