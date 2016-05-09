package com.qijiabin.demo.monitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qijiabin.demo.monitor.thrift.MonitorServiceBase;

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
	private  Object  service;
	
	
    protected ServiceProxyHandler(Object service){
    	this.service=service;
    }
    
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MonitorServiceBase  msb =  (MonitorServiceBase)service;
		ServiceMonitorInfo serviceInfo = msb.getServiceInfo();
		if(log.isDebugEnabled()){
			if(args!=null) {
				for(int i=0;i<args.length;i++){
					log.debug("service arg."+i+"="+(args[i]==null?"null":args[i].toString()));
				}
			}
		}
		long startTime=System.currentTimeMillis();
		Object result=null;  
		try{
	        result=method.invoke(this.service, args);  
		}catch(Exception e){
			if (serviceInfo.isBizMethod(method.getName())) {
				serviceInfo.updateBizMethodInvokeInfo(method.getName(), false, 0);
			}
			throw e;
		}
		long endTime=System.currentTimeMillis();
		if(serviceInfo.isBizMethod(method.getName())){
			serviceInfo.updateBizMethodInvokeInfo(method.getName(), true, (endTime-startTime));
			if(log.isDebugEnabled()) {
				log.debug("call "+method.getName()+" cost time="+(endTime-startTime));
			}
		}
		if(log.isDebugEnabled()) {
			log.debug("call "+method.getName()+"  result="+(result==null?"null":result.toString()));
		}
		return result;
	}
    
}

