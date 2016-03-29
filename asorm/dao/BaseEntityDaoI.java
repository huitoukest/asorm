package com.tingfeng.asorm.dao;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tingfeng.asorm.entity.BaseEntity;

public interface BaseEntityDaoI {

	public abstract SQLiteDatabase getDataBase();

	/**
	 * 得到当前的最大的id号码的值
	 * @param db
	 * @return
	 */
	public abstract <T extends BaseEntity> Long getMaxId(T t);

	/**
	 * 得到当前的最小的id号码的值
	 * @param db
	 * @return
	 */
	public abstract <T extends BaseEntity> Long getMinId(T t);

	/**
	 * 得到当前的的记录总数
	 * @param db
	 * @return
	 */
	public abstract <T extends BaseEntity> Long getCount(Class<T> cls);

	/**
	 *得到除开指定名称的属性列 
	 */
	public abstract <T extends BaseEntity> String[] getEntityColumnNames(
			Class<T> cls, Boolean isRepacePrimaryKeyName,
			String... exceptCoulums);

	/**失败返回null
	 *  传入代Id值的Entity的值实例
	 * @param t 返回t
	 * @return
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> T get(Class<T> cls, String id)
			throws Exception;

	/**失败返回null
	 *  传入代Id值的Entity的值实例
	 * @param t 返回t
	 * @return
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> T get(T t) throws Exception;

	/**手动的条件搜索
	 * @return
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> T get(Class<T> cls,
			String[] columns, String selection, String[] selectionArgs,
			String orderBy) throws Exception;

	/**失败返回null
	 *  传入代Id值的Entity的值实例
	 * @param t 返回t
	 * @param exceptCoulums 不需要取出的数据列的名称
	 * @return
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> T get(Class<T> cls, String id,
			String... exceptCoulums) throws Exception;

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
	public abstract <T extends BaseEntity> List<T> getList(Class<T> cls,
			String selection, String[] selectionArgs, String orderBy,
			String limit, String... exceptCoulums) throws Exception;

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
	public abstract <T extends BaseEntity> List<T> getList(Class<T> cls,
			String selection, String[] selectionArgs, String orderBy,
			String limit) throws Exception;

	/**
	 * 获取数据库中的所有的记录
	 * @param db
	 * @param cls
	 * @return
	 * @throws Exception
	 */
	public abstract <T extends BaseEntity> List<T> getList(Class<T> cls)
			throws Exception;

	public abstract <T extends BaseEntity> void saveOrUpdate(T t)
			throws Exception;

	/**
	 * 
	 * @param t
	 * @return 插入返回1
	 * @param columnName 如果指定的字段,有相同的值存在于数据库,那么就更新数据库,否则保存
	 * @throws Exception
	 */
	public abstract <T extends BaseEntity> void saveOrUpdate(T t,
			String columnName) throws Exception;

	/**
	 * 先删除,后保存,没有则不删除
	 * @param db
	 * @param t
	 * @throws Exception
	 */
	public abstract <T extends BaseEntity> void deleteAndSave(T t)
			throws Exception;

	/**
	 * 
	 * @param db
	 * @param list
	 * @return
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> void saveOrUpdateList(List<T> list)
			throws Exception;

	/**
	 * 
	 * @param db
	 * @param list
	 * @param column 指定列的值相同就更新,否则就保存
	 * @throws Exception
	 */
	public abstract <T extends BaseEntity> void saveOrUpdateList(List<T> list,
			String column) throws Exception;

	/**
	 *删除后保存所有 
	 * @param db
	 * @param list
	 * @return
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> void deleteAndSaveList(List<T> list)
			throws Exception;

	public abstract <T extends BaseEntity> int update(T t) throws Exception;

	/**
	 * 
	 * @param t
	 * @param notUpdateColumns 不需要更新的字段名称的数组
	 * @return
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> int update(T t,
			String[] notUpdateColumns) throws Exception;

	public abstract <T extends BaseEntity> int save(T t) throws Exception;

	public abstract <T extends BaseEntity> int delete(Class<T> cls, String id)
			throws Exception;

	public abstract <T extends BaseEntity> int delete(T t) throws Exception;

	public abstract <T extends BaseEntity> int deleteList(Class<T> cls,
			String ids) throws Exception;

	public abstract <T extends BaseEntity> int deleteList(List<T> ts)
			throws Exception;

	public abstract <T extends BaseEntity> int deleteAll(Class<T> cls)
			throws Exception;

	/**
	 * 
	 * @param db
	 * @param sqlString
	 * @param selectionArgs sql中?占位符的参数
	 * @param columns 需要出去的列的名称,没有会赋值null;取出的列只支持float/string/blob/string/null这几种类型;
	 * *其中二进制会转换成为byte[]类型;除开这些类型外,系统会默认用string来取出数据
	 * @return List<Object[]>
	 */
	public abstract <T extends BaseEntity> List<Object[]> getColumns(
			String sqlString, String[] selectionArgs, String... columns);

	/**
	 * 判断一个属性是否是静态变量,此类暂时不用
	 * @param field
	 */
	public abstract boolean isStaticField(Field field);

	//通过Cursor自动将值赋值到实体
	public abstract <T extends BaseEntity> T initFromCursor(Cursor c,
			Class<T> cls) throws IllegalAccessException,
			IllegalArgumentException, InstantiationException;

	public abstract <T extends BaseEntity> T initFromCursor(Cursor c, T t)
			throws IllegalAccessException, IllegalArgumentException,
			InstantiationException;

	public abstract <T extends BaseEntity> ContentValues getContentValues(T t)
			throws IllegalAccessException, IllegalArgumentException;

	/**
	 * 返回该类属性的键值对,键和值均为String类型
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public abstract <T extends BaseEntity> Map<String, Object> getMapValues(T t)
			throws IllegalAccessException, IllegalArgumentException;

	public abstract <T extends BaseEntity> void saveToDataBase(T t)
			throws Exception;

	public abstract <T extends BaseEntity> void updateToDataBase(T t)
			throws Exception;

	/**
	 * 
	 * @param tableName
	 * @param db
	 * @param columnName 指定此此表的一个列名称,更新所有相同的记录
	 * @throws Exception
	 */
	public abstract <T extends BaseEntity> void updateToDataBaseByColumn(T t,
			String columnName) throws Exception;

	/**
	 * 
	 * @param tableName
	 * @param data
	 * @param notUpdateColumns 不需要跟新的字段,区分大小写
	 * @throws Exception 
	 */
	public abstract <T extends BaseEntity> void updateToDataBase(T t,
			String[] notUpdateColumns) throws Exception;

}