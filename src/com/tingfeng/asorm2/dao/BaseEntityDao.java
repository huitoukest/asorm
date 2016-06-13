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
import com.tingfeng.asorm2.db.DataBaseManager;
import com.tingfeng.asorm2.entity.BaseEntity;
import com.tingfeng.asorm2.entity.EntityManager;
import com.tingfeng.asorm2.entity.Entity_FieldProperty;
import com.tingfeng.asorm2.entity.Entity_FieldProperty.FieldType;
import com.tingfeng.asorm2.log.Log;

/**
 *Dao中不会管理事务,事务在Service层中通过代理进行管理
 *@author dview
 */
public class BaseEntityDao implements BaseEntityDaoI{
	private static BaseEntityDaoI baseEntityDaoI=null;
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
	
	private  DataBaseManager dbManager;
	private BaseEntityDao() {
		super();
		this.dbManager=DataBaseManager.getInstance();
	}
	
	public static BaseEntityDaoI getBaseEntityDaoI(){
		synchronized (BaseEntityDao.class) {	
			if(baseEntityDaoI==null)
			{
				baseEntityDaoI=new BaseEntityDao();
			}
			return baseEntityDaoI;
		}
	}
	
	protected SQLiteDatabase openDataBase(){
		  return dbManager.openWriteDatabase(); 
	}
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getDataBase()
	 */
	@Override
	public SQLiteDatabase getDataBase(){
	  SQLiteDatabase db=dbManager.openWriteDatabase();
			  if(db==null){
				  Log.Error(this.getClass(),"\nsqliteDataBase is null,have you set Transaction before use it?\n");
			  }
	  return db;
	}
	
	protected void closeDataBase(){
		dbManager.closeWriteDatabase();
	}
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getMaxId(T)
	 */
	@Override
	public <T extends BaseEntity> Long getMaxId(T t){
    	return SqliteCRUDUtils.getMaxId(this.getDataBase(),t.getTableName(),t.getPrimaryKeyName());
	}
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getMinId(T)
	 */
	@Override
	public <T extends BaseEntity> Long getMinId(T t){
		return SqliteCRUDUtils.getMinId(this.getDataBase(),t.getTableName(),t.getPrimaryKeyName());
	}
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getCount(java.lang.Class)
	 */
	@Override
	public <T extends BaseEntity> Long getCount(Class<T> cls){		
		return SqliteCRUDUtils.getCount(this.getDataBase(),EntityManager.getPrimaryKeyNameByCls(cls));
	}
	/**
	 * 返回Entity所保存到数据库中的属性列表
	 * @param entity
	 * @return
	 */
	protected <T extends BaseEntity> List<String> getEntityColumnNameList(Class<T> cls){
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
				   if(a instanceof Entity_FieldProperty){
					   Entity_FieldProperty ef=(Entity_FieldProperty)a;
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
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getEntityColumnNames(java.lang.Class, java.lang.Boolean, java.lang.String)
	 */
	@Override
	public <T extends BaseEntity> String[] getEntityColumnNames(Class<T> cls,Boolean isRepacePrimaryKeyName,String... exceptCoulums){
		List<String> nameList=getEntityColumnNameList(cls);
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
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#get(java.lang.Class, java.lang.String)
	 */
	@Override
	public <T extends BaseEntity> T get(Class<T> cls,String id) throws Exception{	   
    	Cursor c=null;
    	try {   		
			c=SqliteCRUDUtils.getCursor(this.getDataBase(),cls.newInstance().getTableName(), null,EntityManager.getPrimaryKeyNameByCls(cls)+"=?", new String[]{id}, null, null, null,null);
	    	if(c!=null&&c.moveToNext())
	    	{ T t=this.initFromCursor(c,cls);
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
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#get(T)
	 */
	@Override
	public <T extends BaseEntity> T get(T t) throws Exception{	   
    	Cursor c=null;
    	try {   		
			c=SqliteCRUDUtils.getCursor(this.getDataBase(),t.getTableName(), null, t.getPrimaryKeyName()+"=?", new String[]{t.getPrimaryKeyValue()+""}, null, null, null,null);
	    	if(c!=null&&c.moveToNext())
	    	{ t=this.initFromCursor(c,t);
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
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#get(java.lang.Class, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public <T extends BaseEntity> T get(Class<T> cls,String[] columns,String selection, String[] selectionArgs, String orderBy) throws Exception{	    	  
		
		Cursor c=null;
    	try {   		
    		T t=cls.newInstance();
			c=SqliteCRUDUtils.getCursor(this.getDataBase(), t.getTableName(),columns, selection, selectionArgs, null, null, orderBy,"0,1");
	    	if(c!=null&&c.moveToNext())
	    	{ t=cls.newInstance();
	    	  t= this.initFromCursor(c,t);
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
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#get(java.lang.Class, java.lang.String, java.lang.String)
	 */
	@Override
	public <T extends BaseEntity> T get(Class<T> cls,String id,String... exceptCoulums) throws Exception{	    			
    	T t=cls.newInstance();
    	Cursor c=null;
    	try {
    		String[] names=getEntityColumnNames(t.getClass(), true,exceptCoulums);
	    	c=SqliteCRUDUtils.getCursor(this.getDataBase(),t.getTableName() ,names, t.getPrimaryKeyName()+"=?", new String[]{id}, null, null, null,null);
 			if(c!=null&&c.moveToNext())
	    	{ t=this.initFromCursor(c,t);
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
	
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getList(java.lang.Class, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
	 */
	 @Override
	public <T extends BaseEntity> List<T> getList(Class<T> cls,String selection, String[] selectionArgs, String orderBy,String limit,String... exceptCoulums) throws Exception{
		 
		 List<T> ts=new ArrayList<T>();
		 Cursor c = null;
		 try {
			 T t=cls.newInstance();
			 String[] names=getEntityColumnNames(cls, true, exceptCoulums);
			   c=SqliteCRUDUtils.getCursor(this.getDataBase(),t.getTableName(),names, selection, selectionArgs, null, null, orderBy,limit);
			 while(c!=null&&c.moveToNext()){
				 t=cls.newInstance();
				 t=this.initFromCursor(c,t);
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
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getList(java.lang.Class, java.lang.String, java.lang.String[], java.lang.String, java.lang.String)
	 */
	 @Override
	public <T extends BaseEntity> List<T> getList(Class<T> cls,String selection, String[] selectionArgs, String orderBy,String limit) throws Exception{
		 
		 List<T> ts=new ArrayList<T>();
		 Cursor c = null;
		 try {
			 T t=cls.newInstance();
			 c=SqliteCRUDUtils.getCursor(this.getDataBase(),t.getTableName(),null, selection, selectionArgs, null, null, orderBy,limit);
			 while(c!=null&&c.moveToNext()){
				 t=cls.newInstance();
				 t=this.initFromCursor(c,t);
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
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getList(java.lang.Class)
	 */
	 @Override
	public <T extends BaseEntity> List<T> getList(Class<T> cls) throws Exception{
		 
		 List<T> ts=new ArrayList<T>();
		 Cursor c = null;
		 try {
			 T t=cls.newInstance();
			 c=SqliteCRUDUtils.getCursor(this.getDataBase(),t.getTableName(),null,null,null,null,null,null,null);
			 while(c!=null&&c.moveToNext()){
				 t=cls.newInstance();
				 t=this.initFromCursor(c,t);
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
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#saveOrUpdate(T)
	 */
	@Override
	public <T extends BaseEntity> void saveOrUpdate(T t) throws Exception{
		 
		 Cursor c = null;
		 try {
		
		    c=SqliteCRUDUtils.getCursor(this.getDataBase(),t.getTableName(), null, t.getPrimaryKeyName()+"=?", new String[]{t.getPrimaryKeyValue()+""}, null, null, null,null);	
			if(c!=null&&c.moveToNext())
		    	{//如果已经存在,则更新,否则insert
		    		this.updateToDataBase(t);
		    	    return;
		    	}		      
			 this.saveToDataBase(t);
			 return;
		} catch (Exception e) {
			Log.Error("saveOrUpdate:"+t.getClass().getName(),e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();
		}
	 }
	 
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#saveOrUpdate(T, java.lang.String)
	 */
	 @Override
	public <T extends BaseEntity> void saveOrUpdate(T t,String columnName) throws Exception{
		 
		 Cursor c = null;
		 try {
			 c=SqliteCRUDUtils.getCursor(this.getDataBase(),t.getTableName(),  null, columnName+"=?", new String[]{t.getClass().getField(columnName).get(t)+""}, null, null, null,null);
			if(c!=null&&c.moveToNext())
		    	{//如果已经存在,则更新,否则insert
		    		this.updateToDataBaseByColumn(t,columnName);
		    	    return;
		    	}		      
			 this.saveToDataBase(t);
			 return;
		} catch (Exception e) {
			Log.Error("saveOrUpdate:"+t.getClass().getName(),e.toString());
			throw e;
		}finally{
			if(c!=null) c.close();
		}
	 }
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#deleteAndSave(T)
	 */
	 @Override
	public <T extends BaseEntity> void deleteAndSave(T t) throws Exception{
		 
		 try {
			 this.delete(t);
			 this.save( t);
		} catch (Exception e) {
			Log.Error("saveOrUpdate:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#saveOrUpdateList(java.util.List)
	 */
	@Override
	public <T extends BaseEntity> void saveOrUpdateList(List<T> list) throws Exception{
		
		try{
			for(T t:list){
				saveOrUpdate(t);					
			}
		}catch(Exception e){
			throw new Exception("saveOrUpdateList: "+e.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#saveOrUpdateList(java.util.List, java.lang.String)
	 */
	@Override
	public <T extends BaseEntity> void saveOrUpdateList(List<T> list,String column) throws Exception{
		
		try{
			for(T t:list){
				saveOrUpdate(t,column);					
			}
		}catch(Exception e){
			throw new Exception("saveOrUpdateList: "+" Fail");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#deleteAndSaveList(java.util.List)
	 */
	@Override
	public <T extends BaseEntity> void deleteAndSaveList(List<T> list) throws Exception{
		try{
			for(T t:list){
				deleteAndSave(t);					
			}
		}catch(Exception e){
			throw new Exception("saveOrUpdateList: "+" Fail");
		}
	}
	
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#update(T)
	 */
	@Override
	public <T extends BaseEntity> int update(T t) throws Exception{
		 
		 try {
			this.updateToDataBase(t);
    	    return 2;
		} catch (Exception e) {
			Log.Error("update:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#update(T, java.lang.String[])
	 */
	 @Override
	public <T extends BaseEntity> int update(T t,String[] notUpdateColumns) throws Exception{		 
		 try {
			this.updateToDataBase(t,notUpdateColumns);
    	    return 2;
		} catch (Exception e) {
			Log.Error("update:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 
	/* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#save(T)
	 */
	@Override
	public <T extends BaseEntity> int save(T t) throws Exception{
		 try {		 
			 this.saveToDataBase(t);
			 return 1;
		} catch (Exception e) {
			Log.Error("save:"+t.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#delete(java.lang.Class, java.lang.String)
	 */
	@Override
	public <T extends BaseEntity> int delete(Class<T> cls,String id) throws Exception{
		 if(id.equals("0"))
				throw new Exception("删除的_id号码不能够是0,请稍后再试!");  
		 try {
	   		SqliteCRUDUtils.delete(this.getDataBase(), cls.newInstance().getTableName(),EntityManager.getPrimaryKeyNameByCls(cls), id);
	   		return 1;
		} catch (Exception e) {
			Log.Error("delete:"+this.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#delete(T)
	 */
	@Override
	public <T extends BaseEntity> int delete(T t) throws Exception{
		 if(t.getPrimaryKeyValue().equals("0"))
				throw new Exception("删除的_id号码不能够是0,请稍后再试!");  
		 try {
	   		SqliteCRUDUtils.delete(this.getDataBase(), t.getTableName(),t.getPrimaryKeyName(), t.getPrimaryKeyValue()+"");
	   		return 1;
		} catch (Exception e) {
			Log.Error("delete:"+this.getClass().getName(),e.toString());
			throw e;
		}
	 }
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#deleteList(java.lang.Class, java.lang.String)
	 */
	@Override
	public <T extends BaseEntity> int deleteList(Class<T> cls,String ids) throws Exception{
		   try {
			   SqliteCRUDUtils.deleteList(this.getDataBase(),cls.newInstance().getTableName(),EntityManager.getPrimaryKeyNameByCls(cls), ids);
		    return 1;
		   } catch (Exception e) {
				Log.Error("deleteList:"+this.getClass().getName(),e.toString());
				throw e;
		}
	 }
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#deleteList(java.util.List)
	 */
	@Override
	public <T extends BaseEntity> int deleteList(List<T> ts) throws Exception{
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
			   this.deleteList(cls, sb.toString());
		    return 1;
		   } catch (Exception e) {
				Log.Error("deleteList:"+this.getClass().getName(),e.toString());
				throw e;
		}
	 }
	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#deleteAll(java.lang.Class)
	 */
	@Override
	public <T extends BaseEntity> int deleteAll(Class<T> cls) throws Exception{
		 try {
			SqliteCRUDUtils.deleteAll(this.getDataBase(),cls.newInstance().getTableName());
		    return 1;
		   } catch (Exception e) {
				Log.Error("deleteAll:"+this.getClass().getName(),e.toString());
				throw e;
		}
	 }	 
	 /* (non-Javadoc)
	 * @see com.dview.p242.dao.BaseEntityDaoI#getColumns(java.lang.String, java.lang.String[], java.lang.String)
	 */
	 @Override
	public <T extends BaseEntity> List<Object[]> getColumns(String sqlString,String[] selectionArgs,String...columns){
		 List<Object[]> list=new ArrayList<Object[]>();
		 Object[] objs=null;
		 Cursor cursor=SqliteCRUDUtils.getCursor(this.getDataBase(), sqlString, selectionArgs);
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
	    	
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#isStaticField(java.lang.reflect.Field)
			 */
	    	@Override
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
    					   if(a instanceof Entity_FieldProperty){
    						   Entity_FieldProperty ef=(Entity_FieldProperty)a;
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
	    			   if(a instanceof Entity_FieldProperty){
	    				   Entity_FieldProperty ef=(Entity_FieldProperty)a;
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
	    	//通过Cursor自动将值赋值到实体
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#initFromCursor(android.database.Cursor, java.lang.Class)
			 */
	    	@Override
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> T initFromCursor(Cursor c,Class<T> cls) throws IllegalAccessException, IllegalArgumentException, InstantiationException{
	    		return setFieldValue(c,cls);						
	    	}
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#initFromCursor(android.database.Cursor, T)
			 */
	    	@Override
			public <T extends BaseEntity> T initFromCursor(Cursor c,T t) throws IllegalAccessException, IllegalArgumentException, InstantiationException{
	    		return setFieldValue(c,t);						
	    	}
	    	
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#getContentValues(T)
			 */
	    	@Override
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> ContentValues getContentValues(T t) throws IllegalAccessException, IllegalArgumentException
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
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#getMapValues(T)
			 */
	    	@Override
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> Map<String,Object> getMapValues(T t) throws IllegalAccessException, IllegalArgumentException
	    	{
	    	   return getFieldValue(false,t);	
	    	}
	    	
	    	
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#saveToDataBase(T)
			 */
	    	@Override
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void saveToDataBase(T t) throws Exception{		
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
	    		this.getDataBase().execSQL(sql, objs);
	    	}	
	    	
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#updateToDataBase(T)
			 */
	    	@Override
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void updateToDataBase(T t) throws Exception{
	    		if(t.getPrimaryKeyValue()==0)
	    			throw new Exception("更新的_id号码不能够是0,请稍后再试!");
	    		this.updateToDataBaseByColumn(t,t.getPrimaryKeyName());
	    	}
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#updateToDataBaseByColumn(T, java.lang.String)
			 */
	    	@Override
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void updateToDataBaseByColumn(T t,String columnName) throws Exception{
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
	    		this.getDataBase().execSQL(sql, objs);
	    	}
	    	
	    	/* (non-Javadoc)
			 * @see com.dview.p242.dao.BaseEntityDaoI#updateToDataBase(T, java.lang.String[])
			 */
	    	@Override
			@JSONField(serialize=false)
	    	public <T extends BaseEntity> void updateToDataBase(T t,String[] notUpdateColumns) throws Exception{
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
	    		this.getDataBase().execSQL(sql, objs);
	    	}
}
