package com.tingfeng.asorm2.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
/**
 * 基本的表的增删改查
 * @author dview76
 *
 */
public class SqliteCRUDUtils{	
	
		/****************************************
		 * 查询操作
		 * **************************************/
	 /** 
	  * 返回搜索的cursor;
	  * @param db
	  * @param sqlString
	  * @param selectionArgs sql中?占位符的参数
	  * @return
	  */
	 public static Cursor getCursor(SQLiteDatabase db,String sqlString,String[] selectionArgs){
		 return db.rawQuery(sqlString,selectionArgs);
	 }
	 

    public static Cursor getCursor(SQLiteDatabase db,String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy,String limit) {
        Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy,limit); 
    	return cursor; 
	}
	/**
	 * 得到当前某个字段最大值
	 * @param db
	 * @return
	 */
	public static Long getMaxId(SQLiteDatabase db,String tableName,String columnName){
		Cursor c=null;  	
		c=getCursor(db,tableName,null,"select max("+columnName+") ", null,null,null,null,null);
    	try{
    		if(c.moveToNext()){
    		return c.getLong(c.getColumnIndex(columnName));
    		}else return null;
		}finally{
			if(c!=null) c.close();
		}
	}
		
	/**
	 * 得到当前的某个字段的最小值
	 * @param db
	 * @return
	 */
	public static Long getMinId(SQLiteDatabase db,String tableName,String columnName){
		Cursor c=null;  	
		c=getCursor(db,tableName,null,"select min("+columnName+") ", null,null,null,null,null);
    	try{
			if(c.moveToNext()){
	    		return c.getLong(c.getColumnIndex(columnName));
	    	}else return null;
		}finally{
			if(c!=null) c.close();
		}
	}
		
	/**
	 * 得到当前的的记录总数
	 * @param db
	 * @return
	 */
	public static Long getCount(SQLiteDatabase db,String tableName){		
		Cursor c=null;  	
		c=getCursor(db,tableName,null,"select count(*) ", null,null,null,null,null);
    	try{
			if(c.moveToNext()){
	    		return c.getLong(0);
	    	}else return null;
    	}finally{
			if(c!=null) c.close();
		}
	}
 
	public static boolean isTableExisted(SQLiteDatabase db,String tableName){	
		Cursor c=null;  	
		c=getCursor(db, "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"+tableName+"'", null);
    	try{
			if(c.moveToNext()){
	    		return c.getLong(0)>0;
	    	}else return false;
    	}finally{
			if(c!=null) c.close();
		}
	}
	
	/***************************************************
	 * 删除操作
	 ***************************************************/	
	
	//删除操作 
	public static int delete(SQLiteDatabase db,String tableName,String columnName,String value) 
	{
		String where =columnName + "=?"; 
		String[] whereValue ={value}; 
		return db.delete(tableName, where, whereValue);
	}
	
	//删除操作 
	public static int  delete(SQLiteDatabase db,String table, String whereClause, String[] whereArgs) 
	{ 
		return db.delete(table, whereClause, whereArgs); 
	}
	
	/**
	 * 用in语句删除
	 * @param db
	 * @param tableName
	 * @param columnName
	 * @param values values应该是in语句中的内容,格式为A,B,C
	 * @return
	 * @throws Exception
	 */
	public static int  deleteList(SQLiteDatabase db,String tableName,String columnName,String values) throws Exception{

				   String whereClause=columnName+" in (?)";
				   String[] whereArgs=new String[]{values};
				  return db.delete(tableName, whereClause, whereArgs);		 
		 }
		 
	 public static int deleteAll(SQLiteDatabase db,String tableName) throws Exception{
		return db.delete(tableName,null,null);
	 }	 
		 
/*************************************************************************
 * 保存以及更新等操作
 *************************************************************************/	 		 	
		    				
	public static void execSQL(SQLiteDatabase db,String sql){
		 db.execSQL(sql);
	}
	
	public void execSQL(SQLiteDatabase db,String sql,Object[] objs){
		db.execSQL(sql,objs);
	}
		    	
	/**
	 * 调用原生的insert方法,不推荐
	 * @param tableName
	 * @param cv
	 * @return
	 */
	public static long insert(SQLiteDatabase db,String tableName,ContentValues cv) 
	{ 
		long row = db.insert(tableName, null, cv); 
		return row; 
	} 	
	/**
	 * 调用自己写的方法,insert into person(name,phone) values (?,?)
	 * @param p
	 */
	public static void save(SQLiteDatabase db,String sql,Object[] objs)  
	{
	  db.execSQL(sql, objs);  
	}  
	
	public static void update(SQLiteDatabase db,String sql,Object[] objs){
		 db.execSQL(sql, objs);  
	}		    			    			    			    	
	/**
	 * 修改操作 
	 * @param db
	 * @param tableName
	 * @param id
	 * @param cv
	 */
	public static int update(SQLiteDatabase db,String tableName,String column,String value, ContentValues cv) 
	{ 
		String where = column+ "=?"; 
		String[] whereValue = {value}; 
		return db.update(tableName, cv, where, whereValue); 
	}	
	/**
	 * 修改操作 
	 * @param db
	 * @param tableName
	 * @param cv
	 * @param where
	 * @param whereValue
	 */
	public int update(SQLiteDatabase db,String tableName,ContentValues cv, String where,String[] whereValue) 
	{ 
			return db.update(tableName, cv, where, whereValue); 
	}


}
