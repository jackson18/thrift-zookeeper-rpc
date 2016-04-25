package com.qijiabin.demo.thrift;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.async.TAsyncClientFactory;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.qijiabin.demo.monitor.MonitorService;
import com.qijiabin.demo.monitor.support.SimpleMonitorService;
import com.qijiabin.demo.thrift.AsyncThriftClientPoolFactory.PoolOperationCallBack;
import com.qijiabin.demo.zookeeper.AddressProvider;


/**
 * ========================================================
 * 日 期：2016年4月19日 下午12:12:00
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：异步客户端代理
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AsyncThriftServiceClientProxyFactory implements FactoryBean, InitializingBean,Closeable {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Integer maxActive = 32;// 最大活跃连接数
	private Integer maxIdle = 1; 
	private Integer minIdle = 0;
	// 连接空闲时间，默认3分钟，-1：关闭空闲检测
	private Integer idleTime = 180000;
	private AddressProvider addressProvider;
	private Object proxyClient;
	private Class<?> objectClass;
	private GenericObjectPool<TAsyncClient> pool;
	private static TAsyncClientManager clientManager = null;
	private TProtocolFactory protocol = new TBinaryProtocol.Factory();
	private MonitorService monitorService;
	private Boolean isMonitor;
	
	
	private PoolOperationCallBack callback = new PoolOperationCallBack() {
		@Override
		public void make(TAsyncClient client) {
			logger.info("create");
		}

		@Override
		public void destroy(PooledObject<TAsyncClient> client) {
			logger.info("destroy");
		}
	};
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(clientManager == null) {
			clientManager = new TAsyncClientManager();
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// 加载Iface接口
		objectClass = classLoader.loadClass(addressProvider.getService() + "$AsyncIface");
		// 加载Client.Factory类
		Class<TAsyncClientFactory<TAsyncClient>> fi = (Class<TAsyncClientFactory<TAsyncClient>>) classLoader.loadClass(addressProvider.getService() + "$AsyncClient$Factory");
		TAsyncClientFactory<TAsyncClient> clientFactory = fi.getConstructor(TAsyncClientManager.class,TProtocolFactory.class).newInstance(clientManager,protocol);
		AsyncThriftClientPoolFactory clientPool = new AsyncThriftClientPoolFactory(addressProvider, clientFactory, callback);
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxTotal(maxActive);
		poolConfig.setMinEvictableIdleTimeMillis(idleTime);
		poolConfig.setTimeBetweenEvictionRunsMillis(idleTime * 2L);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(false);
		poolConfig.setTestWhileIdle(false);
		pool = new GenericObjectPool<TAsyncClient>(clientPool, poolConfig);
		if (isMonitor) {
			this.monitorService = new SimpleMonitorService();
		}
		proxyClient = Proxy.newProxyInstance(classLoader, new Class[] { objectClass }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				TAsyncClient client = pool.borrowObject();
				long start = System.currentTimeMillis(); // 记录起始时间戮
				if (isMonitor) {
					monitorService.getConcurrent(objectClass, method).incrementAndGet(); // 并发计数
				}
				try {
					for(Object obj :args) {
						Class clazz = AsyncMethodCallback.class;
						if(clazz.isAssignableFrom(obj.getClass())) {
							Method m = obj.getClass().getMethod("setPool",GenericObjectPool.class);
							m.invoke(obj, pool);
							m = obj.getClass().getMethod("setClient",TAsyncClient.class);
							m.invoke(obj, client);
						}
					}
					return method.invoke(client, args);
				} catch (Exception e) {
					pool.returnObject(client);
					if (isMonitor) {
						monitorService.collect(objectClass, method, start, true);
					}
					throw e;
				} finally {
					if (isMonitor) {
						monitorService.collect(objectClass, method, start, false);
						monitorService.getConcurrent(objectClass, method).decrementAndGet(); // 并发计数
					}
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
	
	@Override
	public void close() {
		if(pool!=null){
			try {
				pool.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (addressProvider != null) {
			try {
				addressProvider.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}
	public void setIdleTime(Integer idleTime) {
		this.idleTime = idleTime;
	}
	public void setServerAddressProvider(AddressProvider addressProvider) {
		this.addressProvider = addressProvider;
	}
	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}
	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	public void setIsMonitor(Boolean isMonitor) {
		this.isMonitor = isMonitor;
	}
	
}

