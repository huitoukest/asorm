package com.tingfeng.asorm2.db;

import java.util.LinkedList;

import android.database.sqlite.SQLiteDatabase;

import com.tingfeng.asorm2.common.DataBaseTaskI;
import com.tingfeng.asorm2.common.TransActionType;

/**
 * 
 * @author dview76
 * 管理一些数据的读写任务
 */
public class DataBaseTaskManager {
	private static LinkedList<DataBaseTaskI> writeTaskList=new LinkedList<DataBaseTaskI>();
	private static LinkedList<DataBaseTaskI> readTaskList=new LinkedList<DataBaseTaskI>();
	private static DataBaseManager dbManager=DataBaseManager.getInstance();
	/**
	 * 
	 * @param task 数据库的读写任务
	 * @param type type 写和读写操作选择write，读操作选择read
	 */
	public static synchronized void addDataBaseTask(DataBaseTaskI task,TransActionType type){
		boolean runTask=false;
		if(type.equals(TransActionType.Read)){			
			if(readTaskList.isEmpty()){
				runTask=true;
			}
			readTaskList.add(task);
			if(runTask){
				runReadTask();
			}
		}else{
			if(writeTaskList.isEmpty()){
				runTask=true;
			}
			writeTaskList.add(task);
			if(runTask){
				runWriteTask();
			}
		}	
	}
	/**
	 * 默认为写方式
	 * @param task
	 */
	public static synchronized void addDataBaseTask(DataBaseTaskI task){
		addDataBaseTask(task, TransActionType.Read);
	}
	
	/**
	 * 移出列表首位的那个Task
	 * @param type
	 */
	public static synchronized void removeDaBaseHeadTask(TransActionType type){
		if(type.equals(TransActionType.Read)){
			readTaskList.remove(0);
		}else{
			writeTaskList.remove(0);
		}
	}
	
	private static synchronized void runWriteTask(){
		if(!writeTaskList.isEmpty()){
			final DataBaseTaskI task=writeTaskList.get(0);
			Thread t=new Thread(new Runnable() {
				@Override
				public void run() {
					SQLiteDatabase db=dbManager.getWritableDatabase();
					boolean successTrans=true;
					try{
							db.beginTransaction();
							successTrans=task.doTask(db);
						}catch(Throwable e){
							successTrans=false;
							e.printStackTrace();
						}finally{
							 if(successTrans){
								 db.setTransactionSuccessful();
							 }
							 db.endTransaction();
						}
					/**
					 * 完成一个任务后，移出任务，然后继续下一个任务
					 */
					removeDaBaseHeadTask(TransActionType.Write);
					runWriteTask();
				}
			});
			t.start();
		}
	}
	
	private static synchronized void runReadTask(){
		if(!readTaskList.isEmpty()){
			final DataBaseTaskI task=readTaskList.get(0);
			Thread t=new Thread(new Runnable() {
				@Override
				public void run() {
					SQLiteDatabase db=dbManager.getReadableDatabase();				
					task.doTask(db);					
					/**
					 * 完成一个任务后，移出任务，然后继续下一个任务
					 */
					removeDaBaseHeadTask(TransActionType.Read);
					runReadTask();
				}
			});
			t.start();
		}
	}
}
