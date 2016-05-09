package com.qijiabin.demo.monitor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qijiabin.demo.monitor.thrift.BizMethodInfo;
import com.qijiabin.demo.monitor.thrift.MonitorService;
import com.qijiabin.demo.monitor.thrift.MonitorServiceBase;

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

	private static final Logger log = LoggerFactory.getLogger(ServiceProxy.class.getName());
	private ServiceMonitorInfo serviceInfo = new ServiceMonitorInfo();
	private ServerBase server;

	public ServiceProxy(ServerBase server) {
		this.server = server;
	}

	/**
	 * 生成代理类
	 * @param service
	 * @param serviceName
	 * @param serviceVersion
	 * @return
	 */
	public Object wrapper(Object service, String serviceName, String serviceVersion) {
		MonitorServiceBase msb = (MonitorServiceBase) service;
		serviceInfo.setServiceName(serviceName);
		serviceInfo.setServiceVersion(serviceVersion);
		msb.setServiceInfo(serviceInfo);
		msb.setServer(server);
		registerServiceInfo(service);
		return Proxy.newProxyInstance(service.getClass().getClassLoader(), service.getClass().getInterfaces(),
				new ServiceProxyHandler(service));
	}

	/**
	 * 注册服务方法信息，主要是是业务方法，便于后面监控
	 * @param service
	 */
	private void registerServiceInfo(Object service) {
		Class<?>[] interfaces = service.getClass().getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			Class<?> iface = interfaces[i];
			Method[] methods = iface.getMethods();
			for (Method m : methods) {
				if (isMonitorServiceIfaceMethod(m)) {
					continue;
				}
				String methodName = m.getName();
				Class<?>[] type = m.getParameterTypes();
				BizMethodInfo biz = new BizMethodInfo();
				biz.setName(methodName);
				biz.setArgsNum((byte) type.length);
				biz.setArgsType(getArgsType(type));
				if (log.isDebugEnabled()) {
					log.debug("***********--->service registerServiceInfo:{}", biz.toString());
				}
				serviceInfo.addServiceBizMethod(biz);
			}
		}
	}

	private List<String> getArgsType(Class<?>[] types) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < types.length; i++) {
			list.add(types[i].getName());
		}
		return list;
	}

	private Class<MonitorService.Iface> iface = MonitorService.Iface.class;

	private boolean isMonitorServiceIfaceMethod(Method m) {
		Method[] methods = iface.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].equals(m)) {
				return true;
			}
		}
		return false;
	}

}

