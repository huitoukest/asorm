package com.tingfeng.asorm2.db;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.tingfeng.asorm2.common.ObjectType;
import com.tingfeng.asorm2.entity.BaseEntity;
import com.tingfeng.asorm2.entity.EntityManager;
import com.tingfeng.asorm2.entity.EntityFieldProperty;
import com.tingfeng.asorm2.entity.EntityFieldProperty.FieldType;


/**
 * 
 * @author dview76
 * 通过Entity的属性名称,得到sqlite3的建表语句
 */
public class EntityToCreateSqlite3SqlUtils {
   private static boolean isTransentProperty(Field f){
	   Annotation[] as=f.getAnnotations();
	   @SuppressWarnings("unused")
	   Class<?> fclass=null;
	   FieldType fType=null;
	   for(int j=0;j<as.length;j++){
		   Annotation a=as[j];
		   if(a instanceof EntityFieldProperty){
			   EntityFieldProperty ef=(EntityFieldProperty)a;
			   if(ef!=null){
				   fclass=ef.cls();
				   fType=ef.FieldType();
			   }
			   break;
		   }
	   }
	   //按照基础六类属性来处理
	   if(fType==null&&fType==FieldType.Transient)
		   return true;
	   return false;
   }
   public static <T extends BaseEntity> String getSqlFormClass(Class<T> cls) throws IllegalAccessException, IllegalArgumentException{
	   Class<?> clazz=cls;
		Field[] fs=clazz.getDeclaredFields();
		String sql="create table "+cls.getSimpleName().trim()+"(";
		for(int i=0;i<fs.length;i++){
		  Field f=fs[i];
	       f.setAccessible(true);
		   if(isTransentProperty(f))
			   continue;
		  String s=f.getName();
		  ObjectType type=ObjectType.getObjectType(f);
		  sql+=s;
		  if(type.equals(ObjectType.Boolean)){
			sql+=" integer";
		  }else if(type.equals(ObjectType.Date)){
			  sql=sql+" decimal(19,0)";
		  }else if(type.equals(ObjectType.Integer)){
			  sql+=" integer";
		  }else if(type.equals(ObjectType.Long)){
			  sql=sql+" decimal(19,0)";
		  }else if(type.equals(ObjectType.Double)){
			  sql=sql+" decimal(19,19)";
		  }else if(type.equals(ObjectType.Float)){
			  sql=sql+" decimal(19,8)";
		  }else if(type.equals(ObjectType.Short)){
			  sql=sql+" decimal(8,0)";
		  }else if(type.equals(ObjectType.Byte)){
			  sql=sql+" decimal(4,0)";
		  }else {
			  sql=sql+" text";
		  }	
		  if(s.equals(EntityManager.getPrimaryKeyNameByCls(cls)))
		  {
			  sql+=" primary key";
		  }
		if(i<fs.length-1)
			sql+=",";
		}
		sql+=")";
		return sql;
   }
}
