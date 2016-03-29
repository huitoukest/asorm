package com.tingfeng.asorm.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定当前json字符串对应反序列化的方式JsonPropertyType
 * JsonList,JsonObject,Base,Transient
 * 当JsonPropertyType.Base的时候,将会按照默认识别的类型使用,即此时的cls属性不会生效
 * JsonList,JsonObject表示此对象是一个json对象或者字符串;
 * 选择Transient的时候,cls属性不会生效,此属性不进行反序列化操作
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonFieldProperty{
	
	/**
	 * 指定一个类型是JsonPropertyType的,表现为JsonPropertyType=默认为JsonPropertyType.Base的Annotation
	 * @return
	 */
	JsonPropertyType JsonPropertyType() default JsonPropertyType.Base;
	/**
	 * 指定一个类型是Class的,表现为cls=默认为String.class的Annotation
	 * @return
	 */
	Class<?> cls() default String.class;
	/**
     * 指定当前属性的类型
     *
     */
    public enum JsonPropertyType{   	  
    	JsonList,JsonObject,Base,Transient;
    	};
     
    	
}