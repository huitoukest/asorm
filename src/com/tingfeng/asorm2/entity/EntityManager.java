package com.tingfeng.asorm2.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;
import com.tingfeng.asorm2.common.ClassUtil;
import com.tingfeng.asorm2.dao.SqliteCRUDUtils;
import com.tingfeng.asorm2.db.EntityToCreateSqlite3SqlUtils;
import com.tingfeng.asorm2.log.Log;

/**
 * 设置当前所有Entity
 * @author dview76
 *
 */
public class EntityManager {
	private static Set<Class<? extends BaseEntity>> entityClasses=new HashSet<Class<? extends BaseEntity>>();
	private static Map<Class<? extends BaseEntity>, String> entityPKName=new HashMap<Class<? extends BaseEntity>, String>();
	private static Map<Class<? extends BaseEntity>, String> entityTableName=new HashMap<Class<? extends BaseEntity>, String>();
	private static EntityManager entityManager=null;
	private  EntityManager(EntityInfos entityInfos) throws InstantiationException, IllegalAccessException {
		initEntityManager(entityInfos);
	}
	public synchronized static EntityManager getEntityManager(EntityInfos entityInfos) throws InstantiationException, IllegalAccessException{
		if(entityManager==null){
			entityManager=new EntityManager(entityInfos);
		}
		return entityManager;
	}
	/**
	 * 初始化,得到所有Entity的类,报名,主键名称
	 * @param entityInfos
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private <T extends BaseEntity> void initEntityManager(EntityInfos entityInfos) throws InstantiationException, IllegalAccessException{
		Log.Info("init EntityManager!");
		if(entityInfos==null) {
			Log.Info("EntityInfos is NULL!");
			return;
		}
		/*Set<String> packages=entityInfos.getEntityPackages();
		for(String p:packages){
			addEntityToManager(p);
		}*/
		Set<Class<? extends BaseEntity>> tmpClass=entityInfos.getEntityClasses();
		if(tmpClass!=null)
		entityClasses.addAll(tmpClass);
		for(Class<? extends BaseEntity> cls:entityClasses){
			BaseEntity be=(BaseEntity) cls.newInstance();
			putPrimaryKeyNameByCls(cls, be.getPrimaryKeyName());
			putTableNameByCls(cls, be.getTableName());
		}
	}
	
	/**
	 * 添加此包下面的所有类到Entity的List中;
	 * @param pakageName
	 */
	@SuppressWarnings("unchecked")
	protected void addEntityToManager(String packageName){
		if(packageName==null||packageName.trim().length()<1) return;
		List<Class<?>> tmpList=ClassUtil.getClasses(packageName);
		for(Class<?> cls:tmpList){
			if(cls.isAssignableFrom(BaseEntity.class)){
				//如果是BaseEntity的子类				
				entityClasses.add((Class<? extends BaseEntity>)cls);
			}
		}
	}
	
	/**
	 * 此方法不会关闭db,不会管理事务
	 * @param entityInfos
	 * @param db
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T extends BaseEntity> void createAllEntity(SQLiteDatabase db) throws InstantiationException, IllegalAccessException{		
		synchronized("MySqliteTransactionProxy"){
						Log.Info("create Tables start********************************************");
						for(Class<? extends BaseEntity> cls:entityClasses){
						String sql=EntityToCreateSqlite3SqlUtils.getSqlFormClass(cls);
					if(!SqliteCRUDUtils.isTableExisted(db,entityTableName.get(cls)))
						SqliteCRUDUtils.execSQL(db, sql);
						Log.Info(sql);
					}
					Log.Info("create Tables successful********************************************");						
		}
		
	}
	
	/**
	 * 此方法不会关闭db,不会管理事务
	 * @param entityInfos
	 * @param db
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T extends BaseEntity> void onUpdateAllEntity(SQLiteDatabase db) throws InstantiationException, IllegalAccessException{		
		synchronized("MySqliteTransactionProxy"){
			Log.Info("upgrade Tables start********************************************");
			for(Class<? extends BaseEntity> cls:entityClasses){
				String s="DROP TABLE IF EXISTS ";
				String sql=s+entityTableName.get(cls);
				SqliteCRUDUtils.execSQL(db, sql);
				
				Log.Info(sql);
				sql=EntityToCreateSqlite3SqlUtils.getSqlFormClass(cls);
			if(!SqliteCRUDUtils.isTableExisted(db,entityTableName.get(cls)))
				SqliteCRUDUtils.execSQL(db, sql);			
				Log.Info(sql);
			}
			Log.Info("upgrade Tables successful********************************************");
		}
	}
	
	private synchronized  void putPrimaryKeyNameByCls(Class<? extends BaseEntity> key,String value){
		entityPKName.put( key, value);
	}
	public static String getPrimaryKeyNameByCls(Class<? extends BaseEntity> cls){
		return entityPKName.get(cls);
	}
	private synchronized  void putTableNameByCls(Class<? extends BaseEntity> key,String value){
		entityTableName.put( key, value);
	}
	public  static String getTableNameByCls(Class<? extends BaseEntity> cls){
		return entityTableName.get(cls);
	}
}
