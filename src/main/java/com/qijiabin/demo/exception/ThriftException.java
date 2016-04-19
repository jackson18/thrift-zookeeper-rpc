package com.qijiabin.demo.exception;

/**
 * ========================================================
 * 日 期：2016年4月13日 下午12:14:23
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：自定义异常
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class ThriftException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public ThriftException(){
		super();
	}
	
	public ThriftException(String msg){
		super(msg);
	}
	
	public ThriftException(Throwable e){
		super(e);
	}
	
	public ThriftException(String msg,Throwable e){
		super(msg,e);
	}
}
