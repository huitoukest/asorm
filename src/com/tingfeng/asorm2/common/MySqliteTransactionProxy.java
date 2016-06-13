package com.tingfeng.asorm2.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 返回一个类的代理对象
 * @author dview76
 *
 */
public class MySqliteTransactionProxy{

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
	                 new Class[] {interfaceClass}, new InvocationHandler() {			 
	                  /*
	                     * @param proxy : 当前代理类的一个实例； 若在invoke()方法中调用proxy的非final方法，将造成无限循环调用.
	                     */
	                    @Override
	                    public Object invoke(Object object, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{	       
			                  	  Object result=null;	                  	
		                  		  result = method.invoke(proxyedObj, args);	
		                  		  return result;
	                    }
           }); 
		return (T) proxy;
	 }
}
