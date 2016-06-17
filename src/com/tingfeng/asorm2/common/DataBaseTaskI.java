package com.tingfeng.asorm2.common;

import android.database.sqlite.SQLiteDatabase;

/**
 * 用于回调一个数据库任务如CRUD等，
 * 运行在一个单独的额线程中；
 * 一次回调就是一个事务（Transaction）
 * @author dview76
 *
 */
public interface DataBaseTaskI {
	/**
	 * 在调用doDbTask前调用，不管理事务；
	 */
	public void beforeDbTask();
	/**
	 * 
	 * @param db
	 * @return 如果return false，或者发生异常，数据库事务回滚（写操作结果不保存）
	 * 		      对于读操作没有限制
	 */
	public boolean doDbTask(SQLiteDatabase db);
	/**
	 * 在调用doDbTask后调用，不管理事务；
	 */
	public void afterDbTask();
	
}
