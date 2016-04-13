package com.qijiabin.thrift.rpc.demo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ========================================================
 * 日 期：2016年4月13日 下午12:15:11
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：服务端启动
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class Server {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			new ClassPathXmlApplicationContext("classpath:spring-context-thrift-server.xml");
			Thread.sleep(Integer.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
