package com.tingfeng.asorm.exception;

/**
 * 用于代理对象使用时,需要通过异常方式将结果抛出给代理的对象
 * 然后代理对象捕获此异常,并返回值
 * @author dview76
 *
 */
public class ReturnException extends RuntimeException{

	/**
	 * 用来当前线程的返回值
	 */
	final public static ThreadLocal<Object> returnValue=new ThreadLocal<Object>();
	/**
	 * 用来记录当前线程是否发生了returnException
	 */
	final public static ThreadLocal<Boolean> isReturnException=new ThreadLocal<Boolean>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ReturnException(Object returnValue){
		this(returnValue,null);
	}
	public ReturnException(Object returnValue,String msg){
		super(msg);
		ReturnException.returnValue.set(returnValue);
		ReturnException.isReturnException.set(true);
	}

}
