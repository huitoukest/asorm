package com.tingfeng.asorm2.db;

import com.tingfeng.asorm2.entity.EntityInfos;
import com.tingfeng.asorm2.entity.EntityManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DataBaseOpenHelper extends android.database.sqlite.SQLiteOpenHelper{
	private EntityInfos entityInfos=null;
	public DataBaseOpenHelper(Context context,EntityInfos entityInfos) {
		this(context, entityInfos.getDataBaseName(), null,entityInfos.getDataBaseVersion(),entityInfos);
	}
	/**
	 * 
	 * @param context
	 * @param name 数据库的名称
	 */
	public DataBaseOpenHelper(Context context, String name,EntityInfos entityInfos) {
		this(context, name, null,entityInfos.getDataBaseVersion(),entityInfos);	
	}
	
	public DataBaseOpenHelper(Context context, String name, CursorFactory factory,EntityInfos entityInfos) {
		this(context, name, factory,entityInfos.getDataBaseVersion(),entityInfos);	
	}
	public DataBaseOpenHelper(Context context, String name, CursorFactory factory,int version,EntityInfos entityInfos){		
		super(context, name, factory,version);
		this.entityInfos=entityInfos;
	}
	
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		return super.getWritableDatabase();
	}
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		return super.getReadableDatabase();
	}
	@Override
	public synchronized void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	//创建table 
	@SuppressLint("NewApi")
	@Override
	public synchronized void onCreate(SQLiteDatabase db) { 
		/*try {
			EntityManager.getEntityManager(entityInfos).createAllEntity(db);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}*/
	}
	
	@SuppressLint("NewApi")
	@Override
	public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(!entityInfos.onDataBaseVersionChange(db))
				return;
			try {
				db.beginTransaction();
				EntityManager.getEntityManager(entityInfos).onUpdateAllEntity(db);
				db.setVersion(newVersion);
				db.setTransactionSuccessful();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}finally{
				db.endTransaction();
			}
	}
	
	@SuppressLint("NewApi")
	@Override
	public synchronized void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(!entityInfos.onDataBaseVersionChange(db))
				return;
			try {
				  db.setVersion(newVersion);
				  db.beginTransaction();
				  EntityManager.getEntityManager(entityInfos).onUpdateAllEntity(db);
				  db.setTransactionSuccessful();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}finally{
				db.endTransaction();
			}		
	}
	@Override
	public synchronized void close() {
		super.close();
	} 
}
