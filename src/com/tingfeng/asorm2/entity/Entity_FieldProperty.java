package com.tingfeng.asorm2.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定当前Entity中的属性的属性,即自生的类别和转换的方式
 * @author dview76
 * 当FieldType.Base的时候,将会按照默认识别的类型使用,即此时的cls属性不会生效
 * JsonList,JsonObject,Base,Transient
 * JsonList,JsonObject表示此对象需要转换为一个json对象或者字符串;
 * Transient表示此对象,不进行数据库的存和取操作,选择Transient的时候,cls属性不会生效
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity_FieldProperty {
	
	/**
	 * 指定一个类型是FieldType的,表现为FieldType=默认为FieldType.Base的Annotation
	 * @return
	 */
	FieldType FieldType() default FieldType.Base;
	/**
	 * 指定一个类型是Class的,表现为cls=默认为String.class的Annotation
	 * @return
	 */
	Class<?> cls() default String.class;
	/**
     * 指定当前属性的类型
     *
     */
    public enum FieldType{   	  
    	JsonList,JsonObject,Base,Transient;
    	};
     
    	
}