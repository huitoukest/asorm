package com.tingfeng.asorm2.db;

import java.util.LinkedList;

import android.database.sqlite.SQLiteDatabase;

import com.tingfeng.asorm2.common.DataBaseTaskI;
import com.tingfeng.asorm2.common.TransActionType;

/**
 * 
 * @author dview76
 * 管理一些数据的读写任务
 * 通过ThreadLocal来标记启动事务的线程，这样线程之间互相调用的时候，可以通过父线程来获取当前数据库标记；
 */
public class DataBaseTaskManager {
	private static ThreadLocal<LinkedList<DataBaseTaskI>> writeTaskListThread=new ThreadLocal<LinkedList<DataBaseTaskI>>();
	private static ThreadLocal<LinkedList<DataBaseTaskI>> readTaskListThread=new ThreadLocal<LinkedList<DataBaseTaskI>>();
	private static ThreadLocal<SQLiteDatabase> dbThread=new ThreadLocal<SQLiteDatabase>();
	/**
	 * 当前线程运行的任务的索引；
	 */
	private static ThreadLocal<Integer> currentIndex=new ThreadLocal<Integer>();
	private static DataBaseManager dbManager=DataBaseManager.getInstance();
		
	/** 
	 * @param task 数据库的读写任务
	 * @param type type 写和读写操作选择write，读操作选择read
	 */
	public static synchronized void addDataBaseTask(DataBaseTaskI task,TransActionType type){
		boolean runTask=false;
		if(type.equals(TransActionType.Read)){
			if(readTaskListThread.get().isEmpty()){
				runTask=true;
			}
			readTaskListThread.get().add(task);
			if(runTask){
				runReadTask();
			}
		}else{
			if(writeTaskListThread.get().isEmpty()){
				runTask=true;
			}
			writeTaskListThread.get().add(task);
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
		 removeDaTask(type,0);
	}
	/**
	 * 移出列表首位的那个Task
	 * @param type
	 */
	public static synchronized void removeDaTask(TransActionType type,int index){
		if(type.equals(TransActionType.Read)){
			readTaskListThread.get().remove(index);
		}else{
			writeTaskListThread.get().remove(index);
		}
	}
	/**
	 * 测试Service的方法之间互相调用，是否会发生死锁现象
	 */
	private static synchronized void runWriteTask(){
		if(!writeTaskListThread.get().isEmpty()){
			if(currentIndex.get()==null)
				currentIndex.set(0);
			final DataBaseTaskI task=writeTaskListThread.get().get(currentIndex.get());
			Thread t=new Thread(new Runnable() {
				@Override
				public void run() {
					currentIndex.set(currentIndex.get()+1);
					SQLiteDatabase db=dbThread.get();
					boolean manageTransaction=false;
					if(db==null){
						dbManager.getWritableDatabase();
						dbThread.set(db);
						manageTransaction=true;
					}
					boolean successTrans=true&&manageTransaction;
					
					try{
							if(manageTransaction)
						    db.beginTransaction();
							successTrans=task.doTask(db);
						}catch(Throwable e){
							successTrans=false;
							e.printStackTrace();
						}finally{
							 if(successTrans){
								 db.setTransactionSuccessful();
							 }
							 if(manageTransaction)
							 {
								 db.endTransaction();
								 dbThread.set(null);
							 }
						}
					runWriteTask();
					/**
					 * 完成一个任务后，移出任务，然后继续下一个任务
					 */
					removeDaTask(TransActionType.Write,currentIndex.get());
					currentIndex.set(currentIndex.get()-1);					
				}
			});
			if(currentIndex.get()>0){
				t.run();//如果是Service之间互相调用，那么不开启线程
			}else{
				t.start();
			}			
		}
	}
	
	private static synchronized void runReadTask(){
		if(!readTaskListThread.get().isEmpty()){
			final DataBaseTaskI task=readTaskListThread.get().get(0);
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
