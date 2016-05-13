package com.qijiabin.demo.thrift;

import java.net.InetSocketAddress;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qijiabin.demo.exception.ThriftException;
import com.qijiabin.demo.zookeeper.AddressProvider;


/**
 * ========================================================
 * 日 期：2016年4月12日 上午11:48:22
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：thrift连接池工厂
 * ========================================================
 * 修订日期     修订人    描述
 */
public class ThriftClientPoolFactory extends BasePooledObjectFactory<TServiceClient> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private final AddressProvider serverAddressProvider;
	private final TServiceClientFactory<TServiceClient> clientFactory;
	private PoolOperationCallBack callback;
	
	protected ThriftClientPoolFactory(AddressProvider addressProvider, TServiceClientFactory<TServiceClient> clientFactory) throws Exception {
		this.serverAddressProvider = addressProvider;
		this.clientFactory = clientFactory;
	}

	protected ThriftClientPoolFactory(AddressProvider addressProvider, TServiceClientFactory<TServiceClient> clientFactory,
			PoolOperationCallBack callback) throws Exception {
		this.serverAddressProvider = addressProvider;
		this.clientFactory = clientFactory;
		this.callback = callback;
	}
	
	@Override
	public TServiceClient create() throws Exception {
		InetSocketAddress address = serverAddressProvider.selector();
		if(address==null){
			new ThriftException("No provider available for remote service");
		}
		TSocket tsocket = new TSocket(address.getHostName(), address.getPort());
		TTransport transport = new TFramedTransport(tsocket);
		TProtocol protocol = new TBinaryProtocol(transport);
		TServiceClient client = this.clientFactory.getClient(protocol);
		transport.open(); 
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
	public PooledObject<TServiceClient> wrap(TServiceClient obj) {
		return new DefaultPooledObject<TServiceClient>(obj);
	}

	@Override
	public void destroyObject(PooledObject<TServiceClient> client) throws Exception {
		if(callback != null) {
			try{
				callback.destroy(client);
			}catch(Exception e) {
				logger.warn("destroyObject:{}",e);
			}
		}
		logger.info("destroyObject:{}", client);
		TTransport pin = client.getObject().getInputProtocol().getTransport();
		pin.close();
		TTransport pout = client.getObject().getOutputProtocol().getTransport();
		pout.close();
	}
	
	@Override
	public boolean validateObject(PooledObject<TServiceClient> client) {
		TTransport pin = client.getObject().getInputProtocol().getTransport();
		TTransport pout = client.getObject().getOutputProtocol().getTransport();
		return pin.isOpen() && pout.isOpen();
	}

	static interface PoolOperationCallBack {
		// 销毁client之前执行
		void destroy(PooledObject<TServiceClient> client);

		// 创建成功是执行
		void make(TServiceClient client);
	}
	
}

