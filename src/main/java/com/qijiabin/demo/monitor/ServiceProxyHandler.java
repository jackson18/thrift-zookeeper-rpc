package com.qijiabin.demo.monitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.qijiabin.demo.monitor.statistic.MonitorService;

/**
 * ========================================================
 * 日 期：2016年5月8日 上午10:59:23
 * 作 者：jackson
 * 版 本：1.0.0
 * 类说明：服务功能动态代理处理类
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class ServiceProxyHandler implements InvocationHandler{
	
	private Object service;
	private String serviceName;
	private String serviceVersion;
	private MonitorService monitorService;
	private Boolean isMonitor;
	
	
    protected ServiceProxyHandler(Object service, String serviceName, String serviceVersion, MonitorService monitorService, boolean isMonitor){
    	this.service=service;
    	this.serviceName = serviceName;
    	this.serviceVersion = serviceVersion;
    	this.isMonitor = isMonitor;
    	this.monitorService = monitorService;
    }
    
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		long startTime=System.currentTimeMillis();	// 记录起始时间戮
		if (isMonitor) {
			this.monitorService.getConcurrent(this.service.getClass(), method).incrementAndGet(); // 并发计数
		}
		
		Object result=null;  
		try{
	        result=method.invoke(this.service, args);  
	        if (isMonitor) {
	        	long endTime=System.currentTimeMillis();	// 记录结束时间戮
	        	long takeTime = endTime - startTime;
	        	int concurrent = this.monitorService.getConcurrent(this.service.getClass(), method).get(); // 当前并发数
	        	this.monitorService.collect(serviceName, serviceVersion, this.service.getClass(), 
	        			method, args, concurrent, takeTime, false);
	        }
		}catch(Exception e){
			if (isMonitor) {
				long endTime=System.currentTimeMillis();	// 记录结束时间戮
				long takeTime = endTime - startTime;
				int concurrent = this.monitorService.getConcurrent(this.service.getClass(), method).get(); // 当前并发数
				this.monitorService.collect(serviceName, serviceVersion, this.service.getClass(), 
						method, args, concurrent, takeTime, true);
			}
			throw e;
		} finally {
			if (isMonitor) {
				this.monitorService.getConcurrent(this.service.getClass(), method).decrementAndGet(); // 并发计数
			}
		}
		return result;
	}
    
}

