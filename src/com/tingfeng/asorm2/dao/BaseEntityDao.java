package com.tingfeng.asorm2.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.tingfeng.asorm2.common.JsonUtils;
import com.tingfeng.asorm2.common.ObjectType;
import com.tingfeng.asorm2.entity.BaseEntity;
import com.tingfeng.asorm2.entity.EntityManager;
import com.tingfeng.asorm2.entity.EntityFieldProperty;
import com.tingfeng.asorm2.entity.EntityFieldProperty.FieldType;
import com.tingfeng.asorm2.log.Log;

/**
 *Dao中不会管理事务,事务在Service层中通过代理进行管理
 *@author dview
 */
public class BaseEntityDao{
	private static BaseEntityDao baseEntityDao=null;
	private static SqliteCRUDHelper sqliteCRUDHelper=SqliteCRUDHelper.getSqliteCRUDHelper();
	/**
	 * 下面规定允许的数据类型
	 */		
	@JSONField(serialize=false)
	private final String type_boolean="class java.lang.Boolean",
		   type_date="class java.util.Date",
		   type_float="class java.lang.Float",
		   type_double="class java.lang.Double",
		   type_long="class java.lang.Long",
		   type_integer="class java.lang.Integer",
		   type_string="class java.lang.String";
	private BaseEntityDao() {
		super();
	}
	
	public static SqliteCRUDHelper getSqliteCRUDHelperI(){
		return sqliteCRUDHelper;
	}
	
	public static BaseEntityDao getBaseEntityDaoI(){
		synchronized (BaseEntityDao.class) {	
			if(baseEntityDao==null)
			{
				baseEntityDao=new BaseEntityDao();
			}
			return baseEntityDao;
		}
	}
	
	/**
	 * 得到当前的最大的id号码的值
	 * @param db
	 * @return
	 */
	public <T extends BaseEntity> Long getMaxId(SQLiteDatabase db,T t){
    	return getSqliteCRUDHelperI().getMaxId(db,t.getTableName(),t.getPrimaryKeyName());
	}
	
	/**
	 * 得到当前的最小的id号码的值
	 * @param db
	 * @return
	 */
	public <T extends BaseEntity> Long getMinId(SQLiteDatabase db,T t){
		return getSqliteCRUDHelperI().getMinId(db,t.getTableName(),t.getPrimaryKeyName());
	}
	
	/**
	 * 得到当前的的记录总数
	 * @param db
	 * @return
	 */
	public <T extends BaseEntity> Long getCount(SQLiteDatabase db,Class<T> cls){		
		return getSqliteCRUDHelperI().getCount(db,EntityManager.getPrimaryKeyNameByCls(cls));
	}
	/**
	 * 返回Entity所保存到数据库中的属性列表
	 * @param entity
	 * @return
	 */
	protected <T extends BaseEntity> List<String> getEntityColumnNameList(SQLiteDatabase db,Class<T> cls){
		   List<String> list=new ArrayList<String>();
		   Class<?> clazz=cls;
		   Field[] fs=clazz.getDeclaredFields();		
		   String filedName=null;
		   for(Field field:fs){
			   field.setAccessible(true);
			   filedName=field.getName();
			   Annotation[] as=field.getAnnotations();
			   @SuppressWarnings("unused")
			   Class<?> fclass=null;
			   FieldType fType=null;
			   for(int i=0;i<as.length;i++){
				   Annotation a=as[i];
				   if(a instanceof EntityFieldProperty){
					   EntityFieldProperty ef=(EntityFieldProperty)a;
					   if(ef!=null){
						   fclass=ef.cls();
						   fType=ef.FieldType();
						}
					   break;
				   }
			   }
			   if(fType==null||fType!=FieldType.Transient)
			   list.add(filedName);
		   }
		   return list;
	}
	
	/**
	 *得到除开指定名称的属性列 
	 */
	public <T extends BaseEntity> String[] getEntityColumnNames(SQLiteDatabase db,Class<T> cls,Boolean isRepacePrimaryKeyName,String... exceptCoulums){
		List<String> nameList=getEntityColumnNameList(db,cls);
		if(isRepacePrimaryKeyName==null){
			isRepacePrimaryKeyName=true;
		}
		if(exceptCoulums!=null){
			for(String s:exceptCoulums){
				nameList.remove(s);
			}
		}
		String[] names=new String[nameList.size()];
		for(int i=0;i<nameList.size();i++){
			names[i]=nameList.get(i);
			if(names[i].toLowerCase(Locale.ENGLISH).equals(EntityManager.getPrimaryKeyNameByCls(cls))){
				names[i]=EntityManager.getPrimaryKeyNameByCls(cls);
			}
		}
		return names;
	}
	
	/**失败返回null
	 *  传入代Id值的Entity的值实例
	 * @param t 返回t
	 * @return
	 * @throws Exception 
	 */
	public <T extends BaseEntity> T get(SQLiteDatabase db,Class<T> cls,String id) throws Exception{	   
    	Cursor c=null;
    	try {   		
			c=getSqliteCRUDHelperI().getCursor(db,cls.newInstance().getTableName(), null,EntityManager.getPrimaryKeyNameByCls(cls)+"=?", new String[]{id}, null, null, null,null);
	    	if(c!=null&&c.moveToNext())
	    	{ T t=this.initFromCursor(db,c,cls);
	    	  return t;
	    	}else{
	    		return null;
	    	}
		} catch (Exception e) {
			Log.Error(this.getClass().getName()+"T get:",e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();			
		}   		       	
	}
	/**失败返回null
	 *  传入代Id值的Entity的值实例
	 * @param t 返回t
	 * @return
	 * @throws Exception 
	 */
	public <T extends BaseEntity> T get(SQLiteDatabase db,T t) throws Exception{	   
    	Cursor c=null;
    	try {   		
			c=getSqliteCRUDHelperI().getCursor(db,t.getTableName(), null, t.getPrimaryKeyName()+"=?", new String[]{t.getPrimaryKeyValue()+""}, null, null, null,null);
	    	if(c!=null&&c.moveToNext())
	    	{ t=this.initFromCursor(db,c,t);
	    	  return t;
	    	}else{
	    		return null;
	    	}
		} catch (Exception e) {
			Log.Error(this.getClass().getName()+"T get:",e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();			
		}   		       	
	}
	/**手动的条件搜索
	 * @return
	 * @throws Exception 
	 */
	public <T extends BaseEntity> T get(SQLiteDatabase db,Class<T> cls,String[] columns,String selection, String[] selectionArgs, String orderBy) throws Exception{	    	  
		
		Cursor c=null;
    	try {   		
    		T t=cls.newInstance();
			c=getSqliteCRUDHelperI().getCursor(db, t.getTableName(),columns, selection, selectionArgs, null, null, orderBy,"0,1");
	    	if(c!=null&&c.moveToNext())
	    	{ t=cls.newInstance();
	    	  t= this.initFromCursor(db,c,t);
	    	  return t;
	    	}else{
	    		return null;
	    	}
		} catch (Exception e) {
			Log.Error(this.getClass().getName()+"T get:",e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();			
		}   		       	
	}
	
	/**失败返回null
	 *  传入代Id值的Entity的值实例
	 * @param t 返回t
	 * @param exceptCoulums 不需要取出的数据列的名称
	 * @return
	 * @throws Exception 
	 */
	public <T extends BaseEntity> T get(SQLiteDatabase db,Class<T> cls,String id,String... exceptCoulums) throws Exception{	    			
    	T t=cls.newInstance();
    	Cursor c=null;
    	try {
    		String[] names=getEntityColumnNames(db,t.getClass(), true,exceptCoulums);
	    	c=getSqliteCRUDHelperI().getCursor(db,t.getTableName() ,names, t.getPrimaryKeyName()+"=?", new String[]{id}, null, null, null,null);
 			if(c!=null&&c.moveToNext())
	    	{ t=this.initFromCursor(db,c,t);
	    	  return t;
	    	}else{
	    		return null;
	    	}
		} catch (Exception e) {
			Log.Error(this.getClass().getName()+"T get:",e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();			
		}   		       	
	}
	
	
	/**
	 * 
	 * 失败返回空数组
	 * @param db
	 * @param cls
	 * @param selection
	 * @param selectionArgs
	 * @param orderBy
	 * @param limit select * from table_name limit N,M //N序号从0开始
	 * @param exceptCoulums 指定不从数据库取出的列
	 * @return
	 * @throws Exception
	 */
	public <T extends BaseEntity> List<T> getList(SQLiteDatabase db,Class<T> cls,String selection, String[] selectionArgs, String orderBy,String limit,String... exceptCoulums) throws Exception{
		 
		 List<T> ts=new ArrayList<T>();
		 Cursor c = null;
		 try {
			 T t=cls.newInstance();
			 String[] names=getEntityColumnNames(db,cls, true, exceptCoulums);
			   c=getSqliteCRUDHelperI().getCursor(db,t.getTableName(),names, selection, selectionArgs, null, null, orderBy,limit);
			 while(c!=null&&c.moveToNext()){
				 t=cls.newInstance();
				 t=this.initFromCursor(db,c,t);
				 if(!ts.contains(t))
				 ts.add(t);
			 }		 
		} catch (Exception e) {
			Log.Error("getList:"+cls.getName(),e.toString());
			throw e;
		}finally{
			 if(c!=null) c.close();				
		}
		 return ts;
	    }
	/**
	 * 失败返回空数组
	 * @param db
	 * @param cls
	 *@param selection
	 * @param selectionArgs
	 * @param orderBy
	 * @param limit select * from table_name limit N,M //N序号从0开始
	 * @return
	 * @throws Exception 
	 */
	public <T extends BaseEntity> List<T> getList(SQLiteDatabase db,Class<T> cls,String selection, String[] selectionArgs, String orderBy,String limit) throws Exception{
		 
		 List<T> ts=new ArrayList<T>();
		 Cursor c = null;
		 try {
			 T t=cls.newInstance();
			 c=getSqliteCRUDHelperI().getCursor(db,t.getTableName(),null, selection, selectionArgs, null, null, orderBy,limit);
			 while(c!=null&&c.moveToNext()){
				 t=cls.newInstance();
				 t=this.initFromCursor(db,c,t);
				 if(!ts.contains(t))
				 ts.add(t);
			 }		 
		} catch (Exception e) {
			Log.Error("getList:"+cls.getName(),e.toString());
			throw e;
		}finally{
			 if(c!=null) c.close();				
		}
		 return ts;
	    }
	/**
	 * 获取数据库中的所有的记录
	 * @param db
	 * @param cls
	 * @return
	 * @throws Exception
	 */
	public <T extends BaseEntity> List<T> getList(SQLiteDatabase db,Class<T> cls) throws Exception{
		 
		 List<T> ts=new ArrayList<T>();
		 Cursor c = null;
		 try {
			 T t=cls.newInstance();
			 c=getSqliteCRUDHelperI().getCursor(db,t.getTableName(),null,null,null,null,null,null,null);
			 while(c!=null&&c.moveToNext()){
				 t=cls.newInstance();
				 t=this.initFromCursor(db,c,t);
				 if(!ts.contains(t))
				 ts.add(t);
			 }		 
		} catch (Exception e) {
			Log.Error("getList:"+cls.getName(),e.toString());
			throw e;
		}finally{
			 if(c!=null) c.close();				
		}
		 return ts;
	    }
	 
	/**
	 * 
	 * @param t
	 * @return 插入返回1
	 * @param columnName 如果指定的字段,有相同的值存在于数据库,那么就更新数据库,否则保存
	 * @throws Exception
	 */
	public <T extends BaseEntity> void saveOrUpdate(SQLiteDatabase db,T t) throws Exception{
		 
		 Cursor c = null;
		 try {
		
		    c=getSqliteCRUDHelperI().getCursor(db,t.getTableName(), null, t.getPrimaryKeyName()+"=?", new String[]{t.getPrimaryKeyValue()+""}, null, null, null,null);	
			if(c!=null&&c.moveToNext())
		    	{//如果已经存在,则更新,否则insert
		    		this.updateToDataBase(db,t);
		    	    return;
		    	}		      
			 this.saveToDataBase(db,t);
			 return;
		} catch (Exception e) {
			Log.Error("saveOrUpdate:"+t.getClass().getName(),e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();
		}
	 }
	 
	public <T extends BaseEntity> void saveOrUpdate(SQLiteDatabase db,T t,String columnName) throws Exception{
		 
		 Cursor c = null;
		 try {
			 c=getSqliteCRUDHelperI().getCursor(db,t.getTableName(),  null, columnName+"=?", new String[]{t.getClass().getField(columnName).get(t)+""}, null, null, null,null);
			if(c!=null&&c.moveToNext())
		    	{//如果已经存在,则更新,否则insert
		    		this.updateToDataBaseByColumn(db,t,columnName);
		    	    return;
		    	}		      
			 this.saveToDataBase(db,t);
			 return;
		} catch (Exception e) {
			Log.Error("saveOrUpdate:"+t.getClass().getName(),e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();
		}
	 }
	 
		/**
		 * 先删除,后保存,没有则不删除
		 * @param db
		 * @param t
		 * @throws Exception
		 */
	public <T extends BaseEntity> void deleteAndSave(SQLiteDatabase db,T t) throws Exception{
		 
		 try {
			 this.delete(db,t);
			 this.save(db, t);
		} catch (Exception e) {
			Log.Error("saveOrUpdate:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 
	/**
	 * 
	 * @param db
	 * @param list
	 * @param column 指定列的值相同就更新,否则就保存
	 * @throws Exception
	 */
	public <T extends BaseEntity> void saveOrUpdateList(SQLiteDatabase db,List<T> list) throws Exception{
		
		try{
			for(T t:list){
				saveOrUpdate(db,t);					
			}
		}catch(Exception e){
			throw new Exception("saveOrUpdateList: "+e.toString());
		}
	}
	

	public <T extends BaseEntity> void saveOrUpdateList(SQLiteDatabase db,List<T> list,String column) throws Exception{
		
		try{
			for(T t:list){
				saveOrUpdate(db,t,column);					
			}
		}catch(Exception e){
			throw new Exception("saveOrUpdateList: "+" Fail");
		}
	}
	
	/**
	 *删除后保存所有 
	 * @param db
	 * @param list
	 * @return
	 * @throws Exception 
	 */
	public <T extends BaseEntity> void deleteAndSaveList(SQLiteDatabase db,List<T> list) throws Exception{
		try{
			for(T t:list){
				deleteAndSave(db,t);					
			}
		}catch(Exception e){
			throw new Exception("saveOrUpdateList: "+" Fail");
		}
	}
	

	public <T extends BaseEntity> int update(SQLiteDatabase db,T t) throws Exception{
		 
		 try {
			this.updateToDataBase(db,t);
    	    return 2;
		} catch (Exception e) {
			Log.Error("update:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	/**
	 * 
	 * @param t
	 * @param notUpdateColumns 不需要更新的字段名称的数组
	 * @return
	 * @throws Exception 
	 */
	public <T extends BaseEntity> int update(SQLiteDatabase db,T t,String[] notUpdateColumns) throws Exception{		 
		 try {
			this.updateToDataBase(db,t,notUpdateColumns);
    	    return 2;
		} catch (Exception e) {
			Log.Error("update:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 

	public <T extends BaseEntity> int save(SQLiteDatabase db,T t) throws Exception{
		 try {		 
			 this.saveToDataBase(db,t);
			 return 1;
		} catch (Exception e) {
			Log.Error("save:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 

	public <T extends BaseEntity> int delete(SQLiteDatabase db,Class<T> cls,String id) throws Exception{
		 if(id.equals("0"))
				throw new Exception("删除的_id号码不能够是0,请稍后再试!");  
		 try {
	   		getSqliteCRUDHelperI().delete(db, cls.newInstance().getTableName(),EntityManager.getPrimaryKeyNameByCls(cls), id);
	   		return 1;
		} catch (Exception e) {
			Log.Error("delete:"+this.getClass().getName(),e.toString());
			throw e;
		}
	 }

	public <T extends BaseEntity> int delete(SQLiteDatabase db,T t) throws Exception{
		 if(t.getPrimaryKeyValue().equals("0"))
				throw new Exception("删除的_id号码不能够是0,请稍后再试!");  
		 try {
	   		getSqliteCRUDHelperI().delete(db, t.getTableName(),t.getPrimaryKeyName(), t.getPrimaryKeyValue()+"");
	   		return 1;
		} catch (Exception e) {
			Log.Error("delete:"+this.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 

	public <T extends BaseEntity> int deleteList(SQLiteDatabase db,Class<T> cls,String ids) throws Exception{
		   try {
			   getSqliteCRUDHelperI().deleteList(db,cls.newInstance().getTableName(),EntityManager.getPrimaryKeyNameByCls(cls), ids);
		    return 1;
		   } catch (Exception e) {
				Log.Error("deleteList:"+this.getClass().getName(),e.toString());
				throw e;
		}
	 }
	 

	public <T extends BaseEntity> int deleteList(SQLiteDatabase db,List<T> ts) throws Exception{
		   try {
			   StringBuffer sb=new StringBuffer();
			   int i=0;
			   Class<? extends BaseEntity> cls=null; 
			   for(T t:ts){
				   if(i++>0)
					   sb.append(",");
				   else cls=t.getClass();
				   sb.append(t.getPrimaryKeyValue());
			   }
			   this.deleteList(db,cls, sb.toString());
		    return 1;
		   } catch (Exception e) {
				Log.Error("deleteList:"+this.getClass().getName(),e.toString());
				throw e;
		}
	 }
	 

	public <T extends BaseEntity> int deleteAll(SQLiteDatabase db,Class<T> cls) throws Exception{
		 try {
			getSqliteCRUDHelperI().deleteAll(db,cls.newInstance().getTableName());
		    return 1;
		   } catch (Exception e) {
				Log.Error("deleteAll:"+this.getClass().getName(),e.toString());
				throw e;
		}
	 }	 
	/**
	 * 
	 * @param db
	 * @param sqlString
	 * @param selectionArgs sql中?占位符的参数
	 * @param columns 需要出去的列的名称,没有会赋值null;取出的列只支持float/string/blob/string/null这几种类型;
	 * *其中二进制会转换成为byte[]类型;除开这些类型外,系统会默认用string来取出数据
	 * @return List<Object[]>
	 */
	public <T extends BaseEntity> List<Object[]> getColumns(SQLiteDatabase db,String sqlString,String[] selectionArgs,String...columns){
		 List<Object[]> list=new ArrayList<Object[]>();
		 Object[] objs=null;
		 Cursor cursor=getSqliteCRUDHelperI().getCursor(db, sqlString, selectionArgs);
		 while(cursor.moveToNext()){
		    objs=new Object[columns.length];
		    try{
			    for(int i=0;i<columns.length;i++){
			    	String ss=columns[i];
			        int index=cursor.getColumnIndex(ss);
			        if(index==-1)
			        continue;
			    	int columnType =cursor.getType(index);
			    	switch (columnType) {
			    	case Cursor.FIELD_TYPE_NULL:
			    		 objs[i]=null;
			    		 break;		    	
					case Cursor.FIELD_TYPE_INTEGER:
						objs[i]=cursor.getInt(index);
						 break;
					case Cursor.FIELD_TYPE_BLOB:
						objs[i]=cursor.getBlob(index);
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						objs[i]=cursor.getFloat(index);
						break;
					case Cursor.FIELD_TYPE_STRING:
						objs[i]=cursor.getString(index);
					break;
					default:
						objs[i]=cursor.getString(index);
						break;
					}		    	
			    }
			    list.add(objs);
		    }catch(ClassCastException e){
		    	e.printStackTrace();
		    	Log.Error("BaseAndroidDao:getColumns:",e.toString());
		    }		    
		 }
		 return list;
	 }
	    	
			/**
			 * 判断一个属性是否是静态变量,此类暂时不用
			 * @param field
			 */
			@JSONField(serialize=false)
	    	public boolean isStaticField(Field field){
	    		  boolean isStatic = Modifier.isStatic(field.getModifiers());
	    		  return isStatic;
	    	}
	    	private <T extends BaseEntity> T setFieldValue(Cursor c,T t) throws IllegalAccessException, IllegalArgumentException, InstantiationException{
	    		if(c==null) return null;
    		    T obj=t;
    			Class<?> clazz=t.getClass();
    			Field[] fs=clazz.getDeclaredFields();		
    			for(Field f:fs){
    				int index=0;//cursor游标中的索引,某个字段
    				try{
    				f.setAccessible(true);//强制获取,设置值
    				Annotation[] as=f.getAnnotations();
    				Class<?> fclass=null;
    				FieldType fType=null;
    				   for(int i=0;i<as.length;i++){
    					   Annotation a=as[i];
    					   if(a instanceof EntityFieldProperty){
    						   EntityFieldProperty ef=(EntityFieldProperty)a;
    						   if(ef!=null){
    						   fclass=ef.cls();
    						   fType=ef.FieldType();
    						   }
    						   break;
    					   }
    				   }
    				 ObjectType type=ObjectType.getObjectType(f);
    				 String name=f.getName();
    				 if(name.toLowerCase(Locale.ENGLISH).equals(t.getPrimaryKeyName()))
    				 {
    					 name=obj.getTableName();
    				 }
    				  index=c.getColumnIndex(name);
    				  if(index==-1)
    					  continue;
    				   //按照基础六类属性来处理
    				   if(fType==null||fType==FieldType.Base){
    					   		if(type.equals(ObjectType.Boolean)){
    					   			int result=c.getInt(index);
    					   			f.set(obj, (Boolean)(result==1));
    					   		}else if(type.equals(ObjectType.Date)){
    							  Long m=c.getLong(index);
    							  if(m!=null)
    							   {
    								  f.set(obj, new Date(m));
    							   }else{
    								   f.set(obj,null);
    							   }							  
    							}else if(type.equals(ObjectType.Integer)){
    								 f.set(obj,c.getInt(index));
    							}else if(type.equals(ObjectType.Long)){
    								f.set(obj,c.getLong(index));
    							}else if(type.equals(ObjectType.Float)){
    								f.set(obj,c.getFloat(index));
    							}else if(type.equals(ObjectType.Double)){
    								f.set(obj,c.getDouble(index));
    							}else{
    								 f.set(obj,c.getString(index));
    							}
    				   }else if(fType==FieldType.Transient){
    				       continue;
    				   }else if(fType==FieldType.JsonObject){
    					   Object jobj=null;
    					   if(c.getString(index)!=null)
    					   JsonUtils.parseToObject(c.getString(index), fclass);
    					   f.set(obj,jobj);
    				   }else if(fType==FieldType.JsonList){
    					   List<?> objs = null;
    					   if(c.getString(index)!=null)
    					   objs=JsonUtils.parseToArray(c.getString(index), fclass);
    					   f.set(obj,objs);
    				   }
    				} catch (Exception e) {
    					Log.Error(this.getClass().getName(), e.toString());
    					e.printStackTrace();
    				    continue;
    				}//end try
    			}//end for
    		return obj;
	    	}
	    	/**
	    	 * 为本实体类赋值
	    	 * @throws IllegalArgumentException 
	    	 * @throws IllegalAccessException 
	    	 * @throws InstantiationException 
	    	 */
	    	@JSONField(serialize=false)
	    	private <T extends BaseEntity> T setFieldValue(Cursor c,Class<T> cls) throws IllegalAccessException, IllegalArgumentException, InstantiationException{
	    		 return setFieldValue(c, cls.newInstance());
	    	}
	       /**
	        * 以键值对的方式,返回单签类的,属性的名称和之所对应的值
	        * @return
	        * @param isChangeIdString 是否改变属性名称为id的键为BaseEntity.primaryKeyName所代表的属性值
	        * @throws IllegalArgumentException 
	        * @throws IllegalAccessException 
	        */
	       @JSONField(serialize=false)
	       private <T extends BaseEntity> Map<String,Object> getFieldValue(boolean isChangeIdString,T obj) throws IllegalAccessException, IllegalArgumentException{
	    	   Map<String,Object> maps=new HashMap<String,Object>();
	    	   Class<? extends BaseEntity> clazz=obj.getClass();
	    	   Field[] fs=clazz.getDeclaredFields();		
	    	   for(Field field:fs){
	    		   field.setAccessible(true);
	    		   Annotation[] as=field.getAnnotations();
	    		   @SuppressWarnings("unused")
	    		   Class<?> fclass=null;
	    		   FieldType fType=null;
	    		   for(int i=0;i<as.length;i++){
	    			   Annotation a=as[i];
	    			   if(a instanceof EntityFieldProperty){
	    				   EntityFieldProperty ef=(EntityFieldProperty)a;
	    				   if(ef!=null){
	    					   fclass=ef.cls();
	    					   fType=ef.FieldType();
	    				   }
	    				   break;
	    			   }
	    		   }
	    		   ObjectType type=ObjectType.getObjectType(field);
	    		   String name=field.getName();
	    		   if(name.toLowerCase(Locale.ENGLISH).equals(obj.getPrimaryKeyName())&&isChangeIdString)
	    			 {
	    				 name=obj.getTableName();
	    			 }
	    		   //按照基础六类属性来处理
	    		   if(fType==null||fType==FieldType.Base){		  
	    			   if(field.get(obj)==null){
	    					  maps.put(name, null);
	    				  }else if(type.equals(ObjectType.Boolean)){
	    					  if((Boolean)field.get(obj))
	    				       {
	    						  maps.put(name,1);
	    				       }
	    				        else{
	    				        	maps.put(name,0);
	    					  }
	    				  }else if(type.equals(ObjectType.Date)){
	    					  Date d=(Date) field.get(obj);
	    					  maps.put(name, d.getTime());
	    				  }else {
	    					  maps.put(name,field.get(obj));  
	    				  }		   
	    		   }else if(fType==FieldType.Transient){
	    		       continue; 
	    		   }else if(fType==FieldType.JsonObject){
	    			   if(field.get(obj)==null)
	    			   {
	    				   maps.put(name,"{}");
	    			   }else{
	    				   String jsonString=JSON.toJSONString(field.get(obj));
	    				   maps.put(name,jsonString);  
	    			   }
	    		   }else if(fType==FieldType.JsonList){
	    			   if(field.get(obj)==null)
	    			   {
	    				   maps.put(name,"[]");
	    			   }else{
	    				   String jsonString=JSON.toJSONString(field.get(obj));
	    				   maps.put(name,jsonString);  
	    			   }
	    		   }
	    	   }	
	    	   return maps;
	       }

			@JSONField(serialize=false)
	    	public <T extends BaseEntity> T initFromCursor(SQLiteDatabase db,Cursor c,Class<T> cls) throws IllegalAccessException, IllegalArgumentException, InstantiationException{
	    		return setFieldValue(c,cls);						
	    	}

			public <T extends BaseEntity> T initFromCursor(SQLiteDatabase db,Cursor c,T t) throws IllegalAccessException, IllegalArgumentException, InstantiationException{
	    		return setFieldValue(c,t);						
	    	}
	    	
	
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> ContentValues getContentValues(SQLiteDatabase db,T t) throws IllegalAccessException, IllegalArgumentException
	    	{
	    		ContentValues cv;
	    		cv=new ContentValues();
	    		Map<String,Object> maps=getFieldValue(true,t);
	    		Set<String> keys=maps.keySet();		
	    		for(String s:keys){
	    			try{  Object obj=maps.get(s);			 
	    				  //String typeString=obj.getClass().getName();
	    				  ObjectType type=ObjectType.getObjectType(obj.getClass());
	    				 /* if(obj==null){
	    					  cv.put(s,"");
	    				  }else*/ 
	    				  if(type.equals(ObjectType.Boolean)){
	    					  cv.put(s,(Boolean)(obj));  
	    				  }else if(type.equals(ObjectType.Date)){
	    					  cv.put(s,((Date)(obj)).getTime());  
	    					}else if(type.equals(ObjectType.Integer)){
	    						cv.put(s,(Integer)(obj));  
	    					}else if(type.equals(ObjectType.Long)){
	    						cv.put(s,((Long)(obj)));  
	    					}else if(type.equals(ObjectType.Float)){
	    						cv.put(s,(Float)(obj));  
	    					}else if(type.equals(ObjectType.Double)){
	    						cv.put(s,(Double)(obj));  
	    					}else if(type.equals(ObjectType.String)){
	    						cv.put(s,(String)(obj));
	    					}else{
	    						cv.put(s,JSON.toJSONString(obj));  
	    					}
	    			} catch (Exception e) {
	    				Log.Error(this.getClass().getName(), e.toString());
	    				e.printStackTrace();
	    			    continue;
	    			}		
	    		}//end for
	    		return cv;
	    	}
			/**
			 * 返回该类属性的键值对,键和值均为String类型
			 * @return
			 * @throws IllegalAccessException
			 * @throws IllegalArgumentException
			 */
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> Map<String,Object> getMapValues(SQLiteDatabase db,T t) throws IllegalAccessException, IllegalArgumentException
	    	{
	    	   return getFieldValue(false,t);	
	    	}
	    	
	    	

			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void saveToDataBase(SQLiteDatabase db,T t) throws Exception{		
	    		if(t.getPrimaryKeyValue()==0)
	    			throw new Exception("存储的_id号码不能够是0,请稍后再试!");
	    		Map<String,Object> maps=getFieldValue(true,t);
	    		Set<String> keys=maps.keySet();		
	    		Object[] objs=new Object[keys.size()];
	    		String q="";
	    		String sql="insert into "+t.getTableName().trim()+"(";
	    		int i=0;
	    		for(String s:keys){
	    			Object obj=maps.get(s);
	    			 if(i!=0)
	    			  {
	    				  sql+=",";
	    			      q+=",";
	    			  }
	    			  sql+=s;
	    			  q+="?";
	    			  objs[i]=obj;
	    			i++;			
	    		}
	    		sql+=") values ("+q+")";
	    		db.execSQL(sql, objs);
	    	}	
	    	
	
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void updateToDataBase(SQLiteDatabase db,T t) throws Exception{
	    		if(t.getPrimaryKeyValue()==0)
	    			throw new Exception("更新的_id号码不能够是0,请稍后再试!");
	    		this.updateToDataBaseByColumn(db,t,t.getPrimaryKeyName());
	    	}
			/**
			 * 
			 * @param tableName
			 * @param db
			 * @param columnName 指定此此表的一个列名称,更新所有相同的记录
			 * @throws Exception
			 */
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void updateToDataBaseByColumn(SQLiteDatabase db,T t,String columnName) throws Exception{
	    		if(columnName==null)
	    			throw new Exception("更新的columnName不能够是null,请稍后再试!");
	    		Map<String,Object> maps=getFieldValue(true,t);
	    		Set<String> keys=maps.keySet();		
	    		Object[] objs=new Object[keys.size()+1];
	    		String sql="update "+t.getTableName().trim()+" set ";
	    		int i=0;
	    		for(String s:keys){
	    			Object obj=maps.get(s);
	    			if(i!=0)
	    			  {
	    				  sql+=",";
	    			  }
	    			  sql+=s+"=?";		  
	    			  objs[i]=obj;
	    			  if(s.equals(columnName)){
	    	              objs[keys.size()]=obj;
	    			  }
	    			i++;			
	    		}		
	    		sql=sql+" where "+columnName+"=?";		
	    		db.execSQL(sql, objs);
	    	}
	    	
			/**
			 * 
			 * @param tableName
			 * @param data
			 * @param notUpdateColumns 不需要跟新的字段,区分大小写
			 * @throws Exception 
			 */
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void updateToDataBase(SQLiteDatabase db,T t,String[] notUpdateColumns) throws Exception{
	    		if(t.getPrimaryKeyValue()==0)
	    			throw new Exception("更新的_id号码不能够是0,请稍后再试!");		
	    		Map<String,Object> maps=getFieldValue(true,t);
	    		Set<String> keys=maps.keySet();		
	    		Object[] objs;
	    		Map<String,Object> updateMap=new HashMap<String,Object>();
	    		String sql="update "+t.getTableName().trim()+" set ";
	    		int i=0;
	    		for(String s:keys){//筛选出来需要更新的数据
	    			  boolean need=true;
	    		      if(notUpdateColumns!=null)
	    			  for(String c:notUpdateColumns){
	    					if(c.equals(s))
	    					{
	    						need=false;
	    						break;
	    					}
	    				}
	    		      if(need){
	    		    	  updateMap.put(s,maps.get(s));
	    		      }
	            }		
	    		Set<String> key=updateMap.keySet();
	    		objs=new Object[key.size()+1];
	    		i=0;
	    		for(String s:key){
	    			Object value=updateMap.get(s);
	    			if(i!=0)
	    			  {
	    				  sql+=",";
	    			  }
	    			  sql+=s+"=?";
	    			  objs[i]=value;
	    			  if(s.equals(t.getPrimaryKeyName()))
	    			  {	objs[key.size()]=value; }
	    		  i++;
	    		}		
	    		sql=sql+" where "+t.getPrimaryKeyName()+"=?";	
	    		db.execSQL(sql, objs);
	    	}
}
