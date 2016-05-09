package com.qijiabin.thrift.rpc.demo;

import org.apache.thrift.TException;

import com.qijiabin.demo.monitor.thrift.MonitorServiceBase;

public class HelloWorldServiceImpl extends MonitorServiceBase implements HelloWorldService.Iface {
	
	private String name;
	private String version;
	
	public HelloWorldServiceImpl() {
	}
	
	public HelloWorldServiceImpl(String name, String version) {
		super(name, version);
	}

	@Override
	public String sayHello(String username) throws TException {
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "hello : " + username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
