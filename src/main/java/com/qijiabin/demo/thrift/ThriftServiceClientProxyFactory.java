package com.qijiabin.demo.thrift;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.qijiabin.demo.thrift.ThriftClientPoolFactory.PoolOperationCallBack;
import com.qijiabin.demo.zookeeper.AddressProvider;

/**
 * ========================================================
 * 日 期：2016年4月12日 上午11:56:40
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：客户端代理
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ThriftServiceClientProxyFactory implements FactoryBean, InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(ThriftServiceClientProxyFactory.class);
	// 最大活跃连接数
	private Integer maxActive = 32;
	// 连接空闲时间，默认3分钟，-1：关闭空闲检测
	private Integer idleTime = 180000;
	private AddressProvider serverAddressProvider;
	private Object proxyClient;
	private Class<?> objectClass;
	private volatile GenericObjectPool<TServiceClient> pool;

	
	private PoolOperationCallBack callback = new PoolOperationCallBack() {
		@Override
		public void make(TServiceClient client) {
			log.info("**********--->create client connection");
		}

		@Override
		public void destroy(TServiceClient client) {
			log.info("**********--->client connection destroy");
		}
	};
	
	
	/**
	 * set方法成功注入到bean实例中之后，进行一些资源的初始化操作
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		serverAddressProvider.bindPool(pool);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// 加载Iface接口
		objectClass = classLoader.loadClass(serverAddressProvider.getService() + "$Iface");
		// 加载Client.Factory类
		Class<TServiceClientFactory<TServiceClient>> fi = (Class<TServiceClientFactory<TServiceClient>>) classLoader.loadClass(serverAddressProvider.getService() + "$Client$Factory");
		TServiceClientFactory<TServiceClient> clientFactory = fi.newInstance();
		ThriftClientPoolFactory clientPool = new ThriftClientPoolFactory(serverAddressProvider, clientFactory, callback);
		GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
		poolConfig.maxActive = maxActive;
		poolConfig.minIdle = 0;
		poolConfig.minEvictableIdleTimeMillis = idleTime;
		poolConfig.timeBetweenEvictionRunsMillis = idleTime / 2L;
		pool = new GenericObjectPool<TServiceClient>(clientPool, poolConfig);
		proxyClient = Proxy.newProxyInstance(classLoader, new Class[] { objectClass }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				TServiceClient client = pool.borrowObject();
				try {
					return method.invoke(client, args);
				} catch (Exception e) {
					throw e;
				} finally {
					pool.returnObject(client);
				}
			}
		});
	}

	@Override
	public Object getObject() throws Exception {
		return proxyClient;
	}

	@Override
	public Class<?> getObjectType() {
		return objectClass;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void close() {
		if (serverAddressProvider != null) {
			serverAddressProvider.close();
		}
	}
	
	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public void setIdleTime(Integer idleTime) {
		this.idleTime = idleTime;
	}

	public void setServerAddressProvider(AddressProvider serverAddressProvider) {
		this.serverAddressProvider = serverAddressProvider;
	}

}