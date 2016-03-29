package com.tingfeng.asorm.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  对一个类的描述,对类中属性以及属性分组的排序顺序的描述
 * @author dview76
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldSortDescribe {
	
    /**
     * 属性分组的组名,键入一个Integer的值为排序值,默认100
     * 格式:分组名称=排序值,分组名称=排序值
     * @return
     */
    String groupSort() default "";
    /**
     * 在分组的内部进行排序的序号,默认100,
     * 格式属性名称=排序值,属性名称=排序值
     * @return
     */
    String fieldSort() default "";
}
