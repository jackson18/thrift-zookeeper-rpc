package com.qijiabin.thrift.rpc.demo.service.support;

import java.util.Random;

import org.apache.thrift.TException;

import com.qijiabin.thrift.rpc.demo.service.HelloSerivce;

public class HelloServiceImpl implements HelloSerivce.Iface{

	@Override
	public String hello(String msg) throws TException {
		try {
			Thread.sleep(new Random().nextInt(3000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "server : " + msg;
	}

}
