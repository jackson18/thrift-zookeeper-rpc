package com.qijiabin.thrift.rpc.demo;

import org.apache.thrift.TException;

import com.qijiabin.demo.monitor.thrift.MonitorServiceBase;

/**
 * ========================================================
 * 日 期：2016年5月9日 下午6:17:46
 * 作 者：qijiabin
 * 版 本：1.0.0
 * 类说明：服务实现类
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class HelloWorldServiceImpl extends MonitorServiceBase implements HelloWorldService.Iface {
	
	@Override
	public String sayHello(String username) throws TException {
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "hello : " + username;
	}

}