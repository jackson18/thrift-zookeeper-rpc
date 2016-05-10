package com.qijiabin.demo.monitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger log = LoggerFactory.getLogger(ServiceProxyHandler.class);
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
		}catch(Exception e){
			if (isMonitor) {
				monitorService.collect(this.service.getClass(), method, startTime, true);
			}
			throw e;
		} finally {
			if (isMonitor) {
				monitorService.collect(this.service.getClass(), method, startTime, false);
				monitorService.getConcurrent(this.service.getClass(), method).decrementAndGet(); // 并发计数
			}
		}
		
		long endTime=System.currentTimeMillis();	// 记录结束时间戮
		if(log.isDebugEnabled()){
			if(args!=null) {
				for(int i=0;i<args.length;i++){
					log.debug("*************--->service arg.{}={}", i, args[i]==null?"null":args[i].toString());
				}
			}
			log.debug("*************--->serviceName:{},serviceVersion:{}", this.serviceName, this.serviceVersion);
			log.debug("*************--->call {} cost time={}", method.getName(), (endTime-startTime));
			log.debug("************--->call {} result={}", method.getName(), (result==null?"null":result.toString()));
		}
		return result;
	}
    
}

