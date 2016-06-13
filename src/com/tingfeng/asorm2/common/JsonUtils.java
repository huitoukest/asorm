package com.tingfeng.asorm2.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tingfeng.asorm2.common.JsonFieldProperty.JsonPropertyType;


public class JsonUtils {
	/**
	 * 查看一个字符串是否是一[开头,即是否有可能是数组;
	 */
	private static final Pattern pattern = Pattern.compile("^([\\s]{0,}\\[).*");

	/**
	 * 
	 * @param jsonString json字符串
	 * @param cls 需要转换的class类
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static List<? extends Object> parseToArray(String jsonString,Class<?> cls) throws IllegalAccessException, IllegalArgumentException, InstantiationException{
		 JSONArray jsonArr = JSON.parseArray(jsonString);
		 List<Object> list=new ArrayList<Object>();
		 Object o;
		 for(int i=0;i<jsonArr.size();i++){
			 String jarString=jsonArr.getString(i);
			 Matcher matcher = pattern.matcher(jarString);
			 if(matcher.find()){//如果数组中包含的仍然是一个数组
				 try{
				 List<?> tmp=parseToArray(jarString, cls);
				 list.add(tmp);
				 }catch(JSONException e){
					 o=parseToObject(jarString, cls);				 
					 list.add(o);
				 }
			 }else{
				 o=parseToObject(jarString, cls);				 
				  list.add(o);
			 }		   			 		 
		 }
		 return list;
   }
	
	public static Object parseToObject(String string,Class<?> cls) throws InstantiationException, IllegalAccessException{
		Object o=cls.newInstance();
		 JSONObject jsonObj=(JSONObject) JSONObject.parse(string);
			 Field[] fields=cls.getFields();
			 for(int j=0;j<fields.length;j++){
				 Field field=fields[j];
				 field.setAccessible(true);
				 Annotation[] annotations=field.getAnnotations();
				 //为自定义的Annotation赋值
				 JsonFieldProperty jsonFieldProperty=null;
				 for(int a=0;a<annotations.length;a++){
					 if(annotations[a] instanceof JsonFieldProperty){
						 jsonFieldProperty=(JsonFieldProperty)annotations[a];
						 break;
					 }
				 }
				 Object tmp=null;
				 if(jsonFieldProperty==null||jsonFieldProperty.JsonPropertyType()==null||jsonFieldProperty.JsonPropertyType()==JsonPropertyType.Base){
					 tmp=jsonObj.get(field.getName());
					 ObjectType type=ObjectType.getObjectType(field);
					 if(tmp!=null)
					 {
					   		if(type.equals(ObjectType.Boolean)){
					   			field.set(o, jsonObj.getBoolean(field.getName()));
					   		}else if(type.equals(ObjectType.Date)){
					   			field.set(o, jsonObj.getDate(field.getName()));						  
							}else if(type.equals(ObjectType.Integer)){
								field.set(o, jsonObj.getInteger(field.getName()));
							}else if(type.equals(ObjectType.Long)){
								field.set(o, jsonObj.getLong(field.getName()));
							}else if(type.equals(ObjectType.Float)){
								field.set(o, jsonObj.getFloat(field.getName()));
							}else if(type.equals(ObjectType.Double)){
								field.set(o, jsonObj.getDouble(field.getName()));
							}else if(type.equals(ObjectType.String)){
								field.set(o, jsonObj.getString(field.getName()));
							}else{
								field.set(o, tmp);
								}
					}
				 }else if(jsonFieldProperty.JsonPropertyType()==JsonPropertyType.JsonObject){
					 tmp=jsonObj.getString(field.getName());
					 if(tmp!=null)
					 field.set(o,parseToObject(tmp.toString(), jsonFieldProperty.cls()));
				 }else if(jsonFieldProperty.JsonPropertyType()==JsonPropertyType.JsonList){
					 tmp=jsonObj.getString(field.getName());
					 if(tmp!=null)
					 field.set(o,parseToArray(tmp.toString(), jsonFieldProperty.cls()));
				 }					 				 
			 }
		 return o;
	}
}
