package com.tingfeng.asorm2.entity;

import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

/**
 * 传入Entity位置,将会自动创建表(不会更新)
 * @author dview76
 *
 */
public interface EntityInfos {
	/**
	 * 返回Entity类
	 * @return
	 */
	public HashSet<Class<? extends BaseEntity>> getEntityClasses();
	/**
	 * 返回数据库的名称;
	 * @return
	 */
	public String getDataBaseName();
	/**
	 * 返回数据库的版本;
	 * @return
	 */
	public int getDataBaseVersion();
	/**
	 * 当数据库版本变化的时候先调用此方法;
	 * 然后返回true表示调用默认方法,删除所有数据然后重新建立表格;
	 * 然会false表示不调用默认方法,此方法不会自己管理事务;
	 * @return
	 */
	public boolean onDataBaseVersionChange(SQLiteDatabase db);
	/**
	 * 返回Entity所在的java类包
	 * @return
	 */
	//public HashSet<String> getEntityPackages();
}
