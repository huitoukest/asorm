package com.tingfeng.asorm.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  对一个属性的描述,可以简单理解为属性名称的中文翻译意思
 * @author dview76
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDescribe {
	/**
	 * 指定属性的描述
	 * @return
	 */
    String describe();
    /**
     * 属性分组的组名
     * @return
     */
    String group() default "default";
    /**
     * 对结果映射其值,目前仅仅支持数值类型/字符串结果映射结果,如1=男,0=女这种格式;
     * 其中,以$符号来代表结果,$=男$,则将其中的$符号替换为结果,之后再进行结果映射的操作;
     * result=value,这种键值对方法,以逗号分隔;
     * 所以如果结果中含有,的,分割可能会出现问题
     * @return
     */
    String resultMapping() default "";
}
