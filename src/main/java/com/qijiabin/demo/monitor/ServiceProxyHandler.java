package com.qijiabin.demo.monitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	
    protected ServiceProxyHandler(Object service, String serviceName, String serviceVersion){
    	this.service=service;
    	this.serviceName = serviceName;
    	this.serviceVersion = serviceVersion;
    }
    
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(log.isDebugEnabled()){
			if(args!=null) {
				for(int i=0;i<args.length;i++){
					log.debug("*************--->service arg.{}={}", i, args[i]==null?"null":args[i].toString());
				}
			}
		}
		long startTime=System.currentTimeMillis();
		Object result=null;  
		try{
	        result=method.invoke(this.service, args);  
		}catch(Exception e){
			throw e;
		}
		long endTime=System.currentTimeMillis();
		if(log.isDebugEnabled()) {
			log.debug("*************--->serviceName:{},serviceVersion:{}", this.serviceName, this.serviceVersion);
			log.debug("*************--->call {} cost time={}", method.getName(), (endTime-startTime));
			log.debug("************--->call {} result={}", method.getName(), (result==null?"null":result.toString()));
		}
		return result;
	}
    
}

