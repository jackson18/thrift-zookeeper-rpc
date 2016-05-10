package com.qijiabin.demo.monitor;

import java.lang.reflect.Proxy;

/**
 * ========================================================
 * 日 期：2016年5月8日 上午10:58:44
 * 作 者：jackson
 * 版 本：1.0.0
 * 类说明：服务功能动态代理类
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class ServiceProxy {

	/**
	 * 生成代理类
	 * @param service
	 * @param serviceName
	 * @param serviceVersion
	 * @return
	 */
	public Object wrapper(Object service, String serviceName, String serviceVersion) {
		return Proxy.newProxyInstance(
				service.getClass().getClassLoader(), 
				service.getClass().getInterfaces(),
				new ServiceProxyHandler(service, serviceName, serviceVersion));
	}

}