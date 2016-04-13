package com.qijiabin.demo.zookeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * ========================================================
 * 日 期：2016年4月12日 上午10:30:31
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：thrift server-service地址提供者,以便构建客户端连接池
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public interface ThriftServerAddressProvider {
	
	/**
	 * 获取服务名称
	 * @return
	 */
	public String getService();

	/**
	 * 获取所有服务端地址
	 * @return
	 */
    public List<InetSocketAddress> findServerAddressList();

    /**
     * 选取一个合适的address,可以随机获取等,内部可以使用合适的算法.
     * @return
     */
    public InetSocketAddress selector();

    /**
     * 关闭服务连接
     */
    public void close();
    
}

