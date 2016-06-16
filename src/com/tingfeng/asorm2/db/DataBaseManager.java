package com.tingfeng.asorm2.db;


import com.tingfeng.asorm2.common.DataBaseTaskI;
import com.tingfeng.asorm2.common.TransActionType;
import com.tingfeng.asorm2.entity.EntityInfos;
import com.tingfeng.asorm2.entity.EntityManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

public class DataBaseManager {
	private static DataBaseManager instance;
	private static DataBaseOpenHelper databaseHelper;

    @SuppressLint("NewApi")
	public static synchronized void initializeInstance(Context c,final EntityInfos entityInfos){
        if (instance == null) {
            instance = new DataBaseManager();
            databaseHelper =new DataBaseOpenHelper(c,entityInfos);
            if(Build.VERSION.SDK_INT>17)
            { 
            	//启用数据库日志
            	databaseHelper.setWriteAheadLoggingEnabled(true);
            }
            
            DataBaseTaskManager.addDataBaseTask(new DataBaseTaskI() {
				
				@Override
				public boolean doTask(SQLiteDatabase db) {
					try {
						EntityManager.getEntityManager(entityInfos).createAllEntity(db);
						return true;
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
						return false;
					}
				}
			}, TransActionType.Write);
        }                  
    }

    public static synchronized DataBaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DataBaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
	       return databaseHelper.getWritableDatabase();
    }
    
    public synchronized SQLiteDatabase getReadableDatabase(){
    		return databaseHelper.getReadableDatabase();
    }
    
    public synchronized void closeDatabaseConnect() {
	        	databaseHelper.close();
    }
}
