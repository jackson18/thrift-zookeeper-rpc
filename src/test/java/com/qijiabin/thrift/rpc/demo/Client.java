package com.qijiabin.thrift.rpc.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		spring();
	}
	
	/**
	 * socket测试
	 */
	public static void simple() {
		try {
			TSocket socket = new TSocket("192.168.1.87", 9000);
			TTransport transport = new TFramedTransport(socket);
			TProtocol protocol = new TBinaryProtocol(transport);
			HelloWorldService.Client client = new HelloWorldService.Client(protocol);
			transport.open();
			System.out.println(client.sayHello("helloword"));
			Thread.sleep(1000);
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 并发测试
	 */
	public static void spring() {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("spring-context-thrift-client.xml");
			HelloWorldService.Iface helloSerivce = (HelloWorldService.Iface) context.getBean("helloSerivce2");
			Thread.sleep(1000);
			
			ExecutorService pool = Executors.newFixedThreadPool(8);
			for (int i = 0; i < 1; i++) {
				pool.submit(new TThread(helloSerivce));
			}
			
			Thread.sleep(Integer.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class TThread extends Thread {
		HelloWorldService.Iface helloSerivce;

		TThread(HelloWorldService.Iface service) {
			helloSerivce = service;
		}

		public void run() {
			try {
				for (int i = 0; i < 1; i++) {
					System.out.println(Thread.currentThread().getName()+" "+(i+1)+" "+helloSerivce.sayHello("hello222"));
					System.out.println(Thread.currentThread().getName()+" "+(i+1)+" "+helloSerivce.getName());
					System.out.println(Thread.currentThread().getName()+" "+(i+1)+" "+helloSerivce.getVersion());
					System.out.println(Thread.currentThread().getName()+" "+(i+1)+" "+helloSerivce.getServiceBizMethods());
					System.out.println(Thread.currentThread().getName()+" "+(i+1)+" "+helloSerivce.getBizMethodInvokeInfo("sayHello"));
					System.out.println(Thread.currentThread().getName()+" "+(i+1)+" "+helloSerivce.getBizMethodsInvokeInfo());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

