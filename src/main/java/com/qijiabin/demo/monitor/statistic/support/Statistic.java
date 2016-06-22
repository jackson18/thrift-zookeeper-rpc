package com.qijiabin.demo.monitor.statistic.support;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class Statistic {

	private String serviceName;
	private String serviceVersion;
	private Class clazz;
	private Method method;
	private Object[] args;
	private int concurrent;
	private long takeTime;
	private boolean isError;
	
	public Statistic() {
	}
	
	public Statistic(String serviceName, String serviceVersion, Method method, int concurrent, long takeTime, boolean isError) {
		super();
		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.method = method;
		this.concurrent = concurrent;
		this.takeTime = takeTime;
		this.isError = isError;
	}

	public Statistic(String serviceName, String serviceVersion, Class clazz, Method method, Object[] args,
			int concurrent, long takeTime, boolean isError) {
		super();
		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.clazz = clazz;
		this.method = method;
		this.args = args;
		this.concurrent = concurrent;
		this.takeTime = takeTime;
		this.isError = isError;
	}

	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceVersion() {
		return serviceVersion;
	}
	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
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
	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
	public int getConcurrent() {
		return concurrent;
	}
	public void setConcurrent(int concurrent) {
		this.concurrent = concurrent;
	}
	public long getTakeTime() {
		return takeTime;
	}
	public void setTakeTime(long takeTime) {
		this.takeTime = takeTime;
	}
	public boolean getIsError() {
		return isError;
	}
	public void setIsError(boolean isError) {
		this.isError = isError;
	}
	
}

