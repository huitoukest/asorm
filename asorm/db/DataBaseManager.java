package com.tingfeng.asorm.db;

import java.util.concurrent.atomic.AtomicInteger;

import com.tingfeng.asorm.entity.EntityInfos;
import com.tingfeng.asorm.entity.EntityManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

public class DataBaseManager {
	private static DataBaseManager instance;
	private static DataBaseOpenHelper databaseHelper;
    /**
     * 缓存当前数据库
     */
    private SQLiteDatabase dbDatabase=null;
    private AtomicInteger mWriteCounter = new AtomicInteger();
    @SuppressLint("NewApi")
	public static synchronized void initializeInstance(Context c,EntityInfos entityInfos){
        if (instance == null) {
            instance = new DataBaseManager();
            databaseHelper =new DataBaseOpenHelper(c,entityInfos);
            if(Build.VERSION.SDK_INT>17)
            { 
            	databaseHelper.setWriteAheadLoggingEnabled(true);
            }
            synchronized("MySqliteTransactionProxy"){
	            SQLiteDatabase db=databaseHelper.getWritableDatabase();
	            db.beginTransaction();
	            try {	            	
		            	EntityManager.getEntityManager(entityInfos).createAllEntity(db);
						db.setTransactionSuccessful();	            	
	            	} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
		            }finally{
		            	db.endTransaction();
		            	if(db!=null)
		            		databaseHelper.close();
		            }
            }
        }
                    
    }

    public static synchronized DataBaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DataBaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }
    /**
     * 由于手动采用的等待机制,所以不能够使用同步锁,防止死锁
     * @return
     */
    public SQLiteDatabase openWriteDatabase() {
    	synchronized("MySqliteTransactionProxy"){
	    	if(mWriteCounter.get() == 0) {
	    		mWriteCounter.incrementAndGet();
	        	dbDatabase=databaseHelper.getWritableDatabase();
	        	return dbDatabase;
	    	}
	    	return dbDatabase;
    	}
    }
    
    public void closeWriteDatabase() {
    	synchronized("MySqliteTransactionProxy"){
	    	//如果不是打开数据库的线程,则不理会;只有打开数据库的线程才能够关闭数据库
	    	if(mWriteCounter.get() == 1) {
	    		mWriteCounter.decrementAndGet();
	        	databaseHelper.close();
	        	dbDatabase=null;
	        }
    	}  
    }
}
