package com.qijiabin.demo.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;

/**
 * ========================================================
 * 日 期：2016年4月12日 上午11:33:48
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：zk工厂类，用于创建zk客户端连接
 * ========================================================
 * 修订日期     修订人    描述
 */
public class ZookeeperFactory implements FactoryBean<CuratorFramework> {

	// zk地址集合
	private String zkHosts;
	// session超时
	private int sessionTimeout = 30000;
	// 连接超时
	private int connectionTimeout = 30000;
	// 共享一个zk链接
	private boolean singleton = true;
	// 全局path前缀,常用来区分不同的应用
	private String namespace;
	// path前缀
	private final static String ROOT = "rpc";
	// zk客户端连接
	private CuratorFramework zkClient;

	
	public CuratorFramework create() throws Exception {
		if (StringUtils.isEmpty(namespace)) {
			namespace = ROOT;
		} else {
			namespace = ROOT +"/"+ namespace;
		}
		return create(zkHosts, sessionTimeout, connectionTimeout, namespace);
	}

	public static CuratorFramework create(String connectString, int sessionTimeout, int connectionTimeout, String namespace) {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
		return builder
				.connectString(connectString)
				.sessionTimeoutMs(sessionTimeout)
				.connectionTimeoutMs(30000)
				.canBeReadOnly(true)
				.namespace(namespace)
				.retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
				.defaultData(null)
				.build();
	}

	public void close() {
		if (zkClient != null) {
			zkClient.close();
		}
	}
	
	public void setZkHosts(String zkHosts) {
		this.zkHosts = zkHosts;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setZkClient(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}

	@Override
	public CuratorFramework getObject() throws Exception {
		if (singleton) {
			if (zkClient == null) {
				zkClient = create();
				zkClient.start();
			}
			return zkClient;
		}
		return create();
	}

	@Override
	public Class<?> getObjectType() {
		return CuratorFramework.class;
	}

	@Override
	public boolean isSingleton() {
		return singleton;
	}
	
}
