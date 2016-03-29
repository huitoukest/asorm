package com.tingfeng.asorm.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.database.sqlite.SQLiteDatabase;

import com.dview.p242.utils.log.Log;
import com.tingfeng.asorm.db.DataBaseManager;
import com.tingfeng.asorm.exception.ReturnException;

/**
 * 用于android Sqlite3的事务同一管理的类,
 * 返回一个类的代理对象,拦截其中有SqliteTransaction注解的方法,并加入事务处理
 * @author dview76
 *
 */
public class MySqliteTransactionProxy{
	/**
	 * 记录当前事务"开启"的次数;
	 * 用于在server层方法之间互相调用的时候,防止事务多次开启和关闭;
	 */
	 private final static ThreadLocal<Integer> mOpenCounter = new ThreadLocal<Integer>();
	 
	 private MySqliteTransactionProxy(){	
	 }
	 /**
	  * 
	  * @param interfaceClass 接口类
	  * @param proxyedObj 实现接口的的实例
	  * @return (实现接口的)代理方法
	  */
	 @SuppressWarnings("unchecked")
	public static <T,E extends T> T getSqliteTransactionProxy(Class<T> interfaceClass,final E proxyedObj){
		 Object proxy=Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
	                new Class[] { interfaceClass }, new InvocationHandler() {			 
	                  /*
	                     * @param proxy : 当前代理类的一个实例； 若在invoke()方法中调用proxy的非final方法，将造成无限循环调用.
	                     */
	                    @Override
	                    public Object invoke(Object object, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	                      if(MySqliteTransactionProxy.mOpenCounter.get()==null) 
	                    		  MySqliteTransactionProxy.mOpenCounter.set(0);  
	                    	//通过加锁,可以保证被代理方法的顺序执行,也就是说,如果在被代理的service层完成所有和数据库相关的任务处理;
	                    	  //并且一个方法代表一个事务,那么就能够标志事务的正确性以及数据库的"单任务"写入状态;
			                      ThreadLocal<Integer> openCounter=MySqliteTransactionProxy.mOpenCounter;
			                      Log.Info("调用的方法是：" + method.getName());
			                  	  //进行事务类型判断  
			                  	  Annotation[] annotations=method.getAnnotations();
			                  	  TransActionType transActionType=null;  
			                  	  for(int i=0;i<annotations.length;i++){
			                  		  Annotation a=annotations[i];
			                  		  if(a instanceof SqliteTransaction)
			                  		  {
			                  			  SqliteTransaction sqliteTransaction=(SqliteTransaction) a;
			                  			  transActionType=sqliteTransaction.Type();
			                  			  break;
			                  		  }
			                  	  }
			                  	  Object result=null;
	                  	synchronized("MySqliteTransactionProxy"){
		                  	  if(transActionType!=null){
			                  		 SQLiteDatabase db=DataBaseManager.getInstance().openWriteDatabase();	
			                  		 try{
					                  		 if(transActionType!=TransActionType.Read){			               		                   			   
						                  			 /**
							                  		   * 不同线程打开此数据库时,会等待上一个线程关闭数据之后打开
							                  		   */	
							                  			  if(openCounter.get()==0)
							                  			  {//如果当前线程没有打开事务
							                  				  db.beginTransaction();
							                  				  openCounter.set(1);		                  				 		                  				  
							                  			  }else{
							                  				openCounter.set(openCounter.get()+1);
							                  			  }
					                  		 }
				                  			 result = method.invoke(proxyedObj, args);	
				                  			 
				                  			 if(openCounter.get()<=1&&transActionType!=TransActionType.Read){	
				                  				db.setTransactionSuccessful();
				                  			 }
				                  		 }catch(Exception e){
				                  			  	  if(ReturnException.isReturnException.get()==true){
				                  			  		  result=ReturnException.returnValue.get();
				                  			  	  }else{
						                  			  Log.Error(object.getClass(), e);
						                  			  throw e;
					                  			  }
				                  		  }finally{
					                  			if(openCounter.get()<=1)
					                  			  {//如果当前线程只是打开一次事务)
					                  				if(transActionType!=TransActionType.Read){						                  						
						                  					db.endTransaction();
						                  			  }
						                  			  DataBaseManager.getInstance().closeWriteDatabase();
						                  			  openCounter.set(0);
					                  			  }else {
					                  				openCounter.set(openCounter.get()-1);
					                  			  }
				                  		  }	                  		  	                  		 
		                  	  }else{
		                  		  result = method.invoke(proxyedObj, args);	
		                  	  }  
		                  	  	  ReturnException.isReturnException.set(false);
		      			  		  ReturnException.returnValue.set(null);
			                  	  return result;  
                    }//end synchronized
                }
                }); 
		 return (T) proxy;
	 }
}
