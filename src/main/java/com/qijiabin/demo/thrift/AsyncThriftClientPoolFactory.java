package com.qijiabin.demo.thrift;

import java.net.InetSocketAddress;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.async.TAsyncClientFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qijiabin.demo.exception.ThriftException;
import com.qijiabin.demo.zookeeper.AddressProvider;

/**
 * ========================================================
 * 日 期：2016年4月19日 上午11:56:45
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：异步thrift连接池工厂
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class AsyncThriftClientPoolFactory extends BasePooledObjectFactory<TAsyncClient>{

	private Logger logger = LoggerFactory.getLogger(getClass());
	private final AddressProvider addressProvider;
	private final TAsyncClientFactory<TAsyncClient> clientFactory;
	private PoolOperationCallBack callback;
	
	
	public AsyncThriftClientPoolFactory(AddressProvider addressProvider, TAsyncClientFactory<TAsyncClient> clientFactory,
			PoolOperationCallBack callback) {
		super();
		this.addressProvider = addressProvider;
		this.clientFactory = clientFactory;
		this.callback = callback;
	}

	public AsyncThriftClientPoolFactory(AddressProvider addressProvider, TAsyncClientFactory<TAsyncClient> clientFactory) {
		super();
		this.addressProvider = addressProvider;
		this.clientFactory = clientFactory;
	}
	
	@Override
	public TAsyncClient create() throws Exception {
		InetSocketAddress address = addressProvider.selector();
		if(address==null){
			new ThriftException("No provider available for remote service");
		}
		TNonblockingSocket transport = new TNonblockingSocket(address.getHostString(), address.getPort());
		TAsyncClient client = this.clientFactory.getAsyncClient(transport);
		if (callback != null) {
			try {
				callback.make(client);
			} catch (Exception e) {
				logger.warn("makeObject:{}", e);
			}
		}
		return client;
	}

	@Override
	public PooledObject<TAsyncClient> wrap(TAsyncClient obj) {
		return new DefaultPooledObject<TAsyncClient>(obj);
	}

	static interface PoolOperationCallBack {
		// 销毁client之前执行
		void destroy(PooledObject<TAsyncClient> client);

		// 创建成功是执行
		void make(TAsyncClient client);
	}
	
}

