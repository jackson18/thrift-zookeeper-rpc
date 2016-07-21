package com.qijiabin.demo.thrift;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
	private Integer maxIdle = 1; 
	private Integer minIdle = 0;
	private Long	maxWaitMillis = -1l;
	// 连接空闲时间，默认3分钟，-1：关闭空闲检测
	private Integer idleTime = 180000;
	private AddressProvider serverAddressProvider;
	private Object proxyClient;
	private Class<?> objectClass;
	private static AtomicInteger idx = new AtomicInteger(0);
	private volatile Map<InetSocketAddress, GenericObjectPool<TServiceClient>> poolMap = new HashMap<InetSocketAddress, GenericObjectPool<TServiceClient>>();

	
	private PoolOperationCallBack callback = new PoolOperationCallBack() {
		@Override
		public void make(TServiceClient client) {
			log.info("**********--->create client connection");
		}

		@Override
		public void destroy(PooledObject<TServiceClient> client) {
			log.info("**********--->client connection destroy");
		}
	};
	
	
	/**
	 * set方法成功注入到bean实例中之后，进行一些资源的初始化操作
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		objectClass = classLoader.loadClass(serverAddressProvider.getService() + "$Iface");
		
		proxyClient = Proxy.newProxyInstance(classLoader, new Class[] { objectClass }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (poolMap.size() > 0) {
					GenericObjectPool<TServiceClient> pool = getRoundRobin(poolMap);
					TServiceClient client = pool.borrowObject();
					try {
						return method.invoke(client, args);
					} catch (Exception e) {
						throw e;
					} finally {
						pool.returnObject(client);
					}
				} else {
					return null;
				}
			}
		});
		
	}
	
	public void buildPools() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<InetSocketAddress> addressList = serverAddressProvider.findServerAddressList();
		// 遍历最新的zk节点数据，根据最新的节点创建对应的pool
		for (InetSocketAddress address : addressList) {
			if (!poolMap.containsKey(address)) {
				try {
					// 加载Client.Factory类
					Class<TServiceClientFactory<TServiceClient>> fi = (Class<TServiceClientFactory<TServiceClient>>) classLoader.loadClass(serverAddressProvider.getService() + "$Client$Factory");
					TServiceClientFactory<TServiceClient> clientFactory = fi.newInstance();
					ThriftClientPoolFactory clientPool = new ThriftClientPoolFactory(address, clientFactory, callback);
					GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
					poolConfig.setMaxIdle(maxIdle);
					poolConfig.setMinIdle(minIdle);
					poolConfig.setMaxTotal(maxActive);
					poolConfig.setMinEvictableIdleTimeMillis(idleTime);
					poolConfig.setTimeBetweenEvictionRunsMillis(idleTime * 2L);
					poolConfig.setTestOnBorrow(true);
					poolConfig.setTestOnReturn(false);
					poolConfig.setTestWhileIdle(false);
					poolConfig.setMaxWaitMillis(maxWaitMillis);
					
					GenericObjectPool<TServiceClient> pool = new GenericObjectPool<TServiceClient>(clientPool, poolConfig);
					poolMap.put(address, pool);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
		}
		
		// 移除不存在节点对应的pool
		Iterator<Map.Entry<InetSocketAddress, GenericObjectPool<TServiceClient>>> it = poolMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<InetSocketAddress, GenericObjectPool<TServiceClient>> entry = it.next();
			if (!addressList.contains(entry.getKey())) {
				GenericObjectPool<TServiceClient> pool = entry.getValue();
				pool.close();
				it.remove();
			}
		}
	}
	
	/**
	 * 轮循取出连接池
	 * @param pools
	 * @return
	 */
	private GenericObjectPool<TServiceClient> getRoundRobin(Map<InetSocketAddress, GenericObjectPool<TServiceClient>> poolMap) {
		List<GenericObjectPool<TServiceClient>> pools = new ArrayList<GenericObjectPool<TServiceClient>>(poolMap.values()); 
		int index = idx.incrementAndGet();
		GenericObjectPool<TServiceClient> pool = pools.get(index % pools.size());
		log.debug("****************>>>当前使用连接池为： " + pool.toString());
		return pool;
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

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public void setMaxWaitMillis(Long maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

}