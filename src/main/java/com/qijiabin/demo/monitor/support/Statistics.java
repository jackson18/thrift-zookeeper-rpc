package com.qijiabin.demo.monitor.support;

import java.lang.reflect.Method;

/**
 * ========================================================
 * 日 期：2016年4月18日 下午2:16:48
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
@SuppressWarnings("rawtypes")
public class Statistics {
	
	private Class clazz;
	private Method method;
	private int concurrent;
	private long time;
	private boolean error;
	
	
	public Statistics() {
	}
	
	public Statistics(Class clazz, Method method, int concurrent, long time, boolean error) {
		super();
		this.clazz = clazz;
		this.method = method;
		this.concurrent = concurrent;
		this.time = time;
		this.error = error;
	}
	public Class getClazz() {
		return clazz;
	}
	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public int getConcurrent() {
		return concurrent;
	}
	public void setConcurrent(int concurrent) {
		this.concurrent = concurrent;
	}
	
}
