package com.qijiabin.thrift.rpc.demo;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.qijiabin.thrift.rpc.demo.service.HelloSerivce;
import com.qijiabin.thrift.rpc.demo.service.HelloSerivce.AsyncClient.hello_call;


/**
 * ========================================================
 * 日 期：2016年4月13日 下午12:15:43
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：异步客户端调用
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
@SuppressWarnings("resource")
public class AsyncClient {
	
	public static void main(String[] args) {
		spring();
	}

	/**
	 * 并发测试
	 */
	public static void spring() {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("spring-context-thrift-client-async.xml");
			
			//helloSerivce
			HelloSerivce.AsyncIface helloSerivce = (HelloSerivce.AsyncIface) context.getBean("helloSerivce");
			Thread.sleep(1000);
			for(int i = 0; i < 1000; i++) {
				try{
					helloSerivce.hello("hello async",new MyCallback());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			Thread.sleep(Integer.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static class MyCallback extends CommonCallback implements AsyncMethodCallback<HelloSerivce.AsyncClient.hello_call> {

		// 返回结果
		@Override
		public void onComplete(hello_call response) {
			System.out.println("onComplete");
			try {
				System.out.println(response.getResult().toString());
			} catch (TException e) {
				e.printStackTrace();
			}
		}

		// 返回异常
		@Override
		public void onError(Exception exception) {
			exception.printStackTrace();
			System.out.println("onError");
		}

	}
	
}

