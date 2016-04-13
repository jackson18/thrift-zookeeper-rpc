package com.qijiabin.thrift.rpc.demo;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ========================================================
 * 日 期：2016年4月13日 下午12:15:43
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：客户端调用
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
@SuppressWarnings("resource")
public class Client {
	
	public static void main(String[] args) {
		simple();
//		spring();
	}

	public static void spring() {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("spring-context-thrift-client.xml");
			HelloSerivce.Iface helloSerivce = (HelloSerivce.Iface) context.getBean("helloSerivce");
			Thread.sleep(5000);
			for (int i = 0; i < 2; i++) {
				TThread t = new TThread(helloSerivce);
				t.start();
			}
			Thread.sleep(3000000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class TThread extends Thread {
		HelloSerivce.Iface helloSerivce;

		TThread(HelloSerivce.Iface service) {
			helloSerivce = service;
		}

		public void run() {
			try {
				for (int i = 0; i < 10; i++) {
					System.out.println(Thread.currentThread().getName()+"  "+helloSerivce.hello("hello"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void simple() {
		try {
			TSocket socket = new TSocket("192.168.1.87", 9001);
			TTransport transport = new TFramedTransport(socket);
			TProtocol protocol = new TBinaryProtocol(transport);
			HelloSerivce.Client client = new HelloSerivce.Client(protocol);
			transport.open();
			System.out.println(client.hello("helloword"));
			Thread.sleep(3000);
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
