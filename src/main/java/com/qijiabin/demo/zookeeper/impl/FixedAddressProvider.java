package com.qijiabin.demo.zookeeper.impl;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.InitializingBean;

import com.qijiabin.demo.zookeeper.ThriftServerAddressProvider;

/**
 * ========================================================
 * 日 期：2016年4月13日 下午2:52:19
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：通过配置获取服务地址
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class FixedAddressProvider implements ThriftServerAddressProvider,InitializingBean{
	
	// 注册服务
	private String service;
	private String serverAddress;
	private final List<InetSocketAddress> container = new CopyOnWriteArrayList<InetSocketAddress>();
    private Queue<InetSocketAddress> inner = new LinkedList<InetSocketAddress>();

    public FixedAddressProvider(){}
    
    public FixedAddressProvider(String service,String serverAddress){
    	this.service = service;
    	this.serverAddress = serverAddress;
    }


	@Override
	public void afterPropertiesSet() throws Exception {
		String[] hostnames = serverAddress.split(",");//"ip:port:weight,ip:port"
        for (String hostname : hostnames) {
            String[] address = hostname.split(":");
            InetSocketAddress sa = new InetSocketAddress(address[0],Integer.valueOf(address[1]));
            //根据权重值
            Integer weight = 1;//权重值
            if(address.length == 3){
            	weight = Integer.valueOf(address[2]);
            }
            //权重越高,list中占有的数据条目越多
            for(int i=0; i< weight; i++){
                container.add(sa);
            }
        }
        //随机
        Collections.shuffle(container);
        inner.addAll(container);
	}
	
	@Override
	public List<InetSocketAddress> findServerAddressList() {
		return Collections.unmodifiableList(container);
	}

	@Override
	public InetSocketAddress selector() {
		if(inner.isEmpty()){
            inner.addAll(container);
        }
        return inner.poll();
	}

	@Override
	public void close() {
	}
	
	@Override
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
}

