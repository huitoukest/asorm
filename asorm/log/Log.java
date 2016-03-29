package com.tingfeng.asorm.log;

public class Log{

	public static void Error(Object o,Exception e){
		android.util.Log.e(o.getClass().getName(), e.toString());
		e.printStackTrace();
	}

	public static void Error(Object o,String s){
		android.util.Log.e(o.getClass().getName(), s!=null?s:"NULL POINT");
	}
	public static void Error(String msg,Object o,Exception e){
		android.util.Log.e(o.getClass().getName(),msg+": "+e.toString());
		e.printStackTrace();
	}
	
	public static void Info(String msg,Object o){
		
		android.util.Log.i(o.getClass().getName(), msg!=null?msg:"NULL POINT");
	}
	
	public static void Info(String msg){
		
		android.util.Log.i("", msg!=null?msg:"NULL POINT");
	}

	public static void Error(Class<?> class1, String string) {
		android.util.Log.e(class1.getName(), string!=null?string:"NULL POINT");
	}
}
