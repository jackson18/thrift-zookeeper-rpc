package com.qijiabin.demo.zookeeper.support;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.qijiabin.demo.exception.ThriftException;
import com.qijiabin.demo.zookeeper.AddressRegister;

/**
 * ========================================================
 * 日 期：2016年4月12日 上午11:22:03
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：注册服务列表到Zookeeper
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class ZookeeperAddressRegister implements AddressRegister{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	//zk客户端
	private CuratorFramework zkClient;
	
	public ZookeeperAddressRegister(){}
	
	public ZookeeperAddressRegister(CuratorFramework zkClient){
		this.zkClient = zkClient;
	}


	/**
	 * 注册服务到zk
	 */
	@Override
	public void register(String service, String version, String address) {
		if(zkClient.getState() == CuratorFrameworkState.LATENT){
			zkClient.start();
		}
		if(StringUtils.isEmpty(version)){
			version="1.0.0";
		}
		//临时节点
		try {
			zkClient.create()
				.creatingParentsIfNeeded()
				.withMode(CreateMode.EPHEMERAL)
				.forPath("/"+service+"/"+version+"/"+address);
		} catch (UnsupportedEncodingException e) {
			logger.error("register service address to zookeeper exception:{}",e);
			throw new ThriftException("register service address to zookeeper exception: address UnsupportedEncodingException", e);
		} catch (Exception e) {
			logger.error("register service address to zookeeper exception:{}",e);
			throw new ThriftException("register service address to zookeeper exception:{}", e);
		}
	}
	
	@Override
	public void unregister(String name, String version, String address) {
		String servicePath = "/"+name + "/" + version +"/"+ address;
		try {
			if(zkClient.getState() == CuratorFrameworkState.LATENT) {
				zkClient.start();
			}
			if(zkClient.blockUntilConnected(3000, TimeUnit.MILLISECONDS)) {
				zkClient.delete().forPath(servicePath);
				logger.debug("delete path [{}] successful.", servicePath);
			}
		} catch (InterruptedException e) {
			logger.error("CuratorFramework Client interrupted. Exception message [{}].", e.getMessage());
			throw new ThriftException("unregister service address to zookeeper block interrupted",e);
		} catch (Exception e) {
			logger.error("delete path[{}] failed. Message:{}.", servicePath, e.getMessage());
			throw new ThriftException("unregister service address to zookeeper exception:{}", e);
		}
	}
	
	@Override
	public void close(){
		zkClient.close();
	}
	
	public void setZkClient(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}
	
}

