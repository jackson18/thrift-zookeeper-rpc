package com.qijiabin.demo.zookeeper;

/**
 * ========================================================
 * 日 期：2016年4月12日 上午11:18:52
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：发布服务地址及端口到服务注册中心(zookeeper服务器)
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public interface AddressRegister {
	
	/**
	 * 发布服务接口
	 * @param service 服务接口名称，一个产品中不能重复
	 * @param version 服务接口的版本号，默认1.0.0
	 * @param address 服务发布的地址和端口
	 */
	void register(String service,String version,String address);
	
	/**
	 * 取消服务注册
	 * @param name
	 * @param version
	 * @param address
	 */
	void unregister(String name,String version,String address);
	
	/**
	 * 关闭
	 */
	void close();
	
}

