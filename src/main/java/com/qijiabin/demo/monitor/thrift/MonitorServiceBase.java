package com.qijiabin.demo.monitor.thrift;

import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import com.qijiabin.demo.monitor.ServerBase;
import com.qijiabin.demo.monitor.ServiceMonitorInfo;

/**
 * ========================================================
 * 日 期：2016年5月8日 上午10:55:35
 * 作 者：jackson
 * 版 本：1.0.0
 * 类说明：服务监控基类
 * 服务实现类需要继承此类
 * ========================================================
 * 修订日期     修订人    描述
 */
public abstract class MonitorServiceBase implements MonitorService.Iface {
	
	private ServiceMonitorInfo serviceInfo;
	private ServerBase server;
	
	
	@Override
	public String getName() throws TException {
		return this.serviceInfo.getServiceName();
	}

	@Override
	public String getVersion() throws TException {
		return this.serviceInfo.getServiceVersion();
	}

	@Override
	public List<BizMethodInfo> getServiceBizMethods() throws TException {
		return this.serviceInfo.getBizMethodInfoList();
	}

	@Override
	public Map<String, BizMethodInvokeInfo> getBizMethodsInvokeInfo() throws TException {
		return this.serviceInfo.getBizMethodInvokeInfoMap();
	}

	@Override
	public BizMethodInvokeInfo getBizMethodInvokeInfo(String methodName) throws TException {
		return this.serviceInfo.getBizMethodInvokeInfoMap().get(methodName);
	}

	@Override
	public void setOption(String key, String value) throws TException {
		this.server.setOption(key, value);
	}

	@Override
	public Map<String, String> getOptions() throws TException {
		return this.server.getOptions();
	}
	
	public ServerBase getServer() {
		return server;
	}
	
	public void setServer(ServerBase server) {
		this.server = server;
	}
	
	public ServiceMonitorInfo getServiceInfo() {
		return serviceInfo;
	}
	
	public void setServiceInfo(ServiceMonitorInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}
	
}

