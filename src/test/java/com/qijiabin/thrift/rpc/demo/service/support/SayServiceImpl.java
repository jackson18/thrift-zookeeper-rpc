package com.qijiabin.thrift.rpc.demo.service.support;

import java.util.Random;

import org.apache.thrift.TException;

import com.qijiabin.thrift.rpc.demo.service.SaySerivce;

public class SayServiceImpl implements SaySerivce.Iface{

	@Override
	public String say(String msg) throws TException {
		try {
			Thread.sleep(new Random().nextInt(3000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "server2 : " + msg;
	}

}
