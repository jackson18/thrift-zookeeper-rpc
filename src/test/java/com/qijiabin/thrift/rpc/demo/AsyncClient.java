package com.qijiabin.thrift.rpc.demo;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.qijiabin.demo.thrift.AsyncThriftServiceClientProxyFactory;
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
			final ApplicationContext context = new ClassPathXmlApplicationContext("spring-context-thrift-client-async.xml");
			
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
			
			//关闭连接的钩子
			Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                	Map<String,AsyncThriftServiceClientProxyFactory> clientMap = context.getBeansOfType(AsyncThriftServiceClientProxyFactory.class);
                	for(Entry<String, AsyncThriftServiceClientProxyFactory> client : clientMap.entrySet()){
                		System.out.println("serviceName : "+client.getKey() + ",class obj: "+client.getValue());
                		client.getValue().close();
                	}
                }
            });
			
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
			} finally {
				//回收链接资源
				giveBackResrouce();
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

