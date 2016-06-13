package com.tingfeng.asorm2.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqliteTransaction {
	
	/**
	 * 指定一个类型是
	 * @return
	 */
	TransActionType Type() default TransActionType.Read;     
    	
}