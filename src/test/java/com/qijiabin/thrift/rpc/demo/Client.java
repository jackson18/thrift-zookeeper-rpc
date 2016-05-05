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

import com.qijiabin.thrift.rpc.demo.service.HelloSerivce;
import com.qijiabin.thrift.rpc.demo.service.SaySerivce;

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
	 * 并发测试
	 */
	public static void spring() {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("spring-context-thrift-client.xml");
			
			//helloSerivce
			HelloSerivce.Iface helloSerivce = (HelloSerivce.Iface) context.getBean("helloSerivce");
			HelloSerivce.Iface helloSerivce2 = (HelloSerivce.Iface) context.getBean("helloSerivce2");
			Thread.sleep(1000);
			
			System.out.println(Thread.currentThread().getName()+"  "+helloSerivce.hello("hello"));
			
			ExecutorService pool = Executors.newFixedThreadPool(8);
			for (int i = 0; i < 5; i++) {
				pool.submit(new TThread(helloSerivce2));
			}
			
			//saySerivce
			SaySerivce.Iface saySerivce = (SaySerivce.Iface) context.getBean("saySerivce");
			Thread.sleep(1000);
			System.out.println(Thread.currentThread().getName()+"  "+saySerivce.say("say"));

			
			Thread.sleep(Integer.MAX_VALUE);
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
				for (int i = 0; i < 50; i++) {
					System.out.println(Thread.currentThread().getName()+"  "+helloSerivce.hello("hello222"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * socket测试
	 */
	public static void simple() {
		try {
			TSocket socket = new TSocket("192.168.1.87", 9000);
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
