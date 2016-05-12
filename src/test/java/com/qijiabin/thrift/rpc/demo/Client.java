package com.qijiabin.thrift.rpc.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		spring();
	}
	
	/**
	 * 并发测试
	 */
	public static void spring() {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("spring-context-thrift-client.xml");
			Thread.sleep(1000);
			
			HelloWorldService.Iface helloSerivce = (HelloWorldService.Iface) context.getBean("helloSerivce2");
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
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

