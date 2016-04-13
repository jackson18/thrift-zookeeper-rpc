package com.qijiabin.thrift.rpc.demo;

import org.apache.thrift.TException;

public class HelloServiceImpl implements HelloSerivce.Iface{

	@Override
	public String hello(String msg) throws TException {
		return "server : " + msg;
	}

}
