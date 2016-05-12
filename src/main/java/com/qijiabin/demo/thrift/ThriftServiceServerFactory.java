package com.qijiabin.demo.thrift;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.Closeable;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;

import org.apache.thrift.TProcessor;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import com.facebook.nifty.core.NettyServerConfig;
import com.facebook.nifty.core.NettyServerConfigBuilder;
import com.facebook.nifty.core.NettyServerTransport;
import com.facebook.nifty.core.ThriftServerDefBuilder;
import com.qijiabin.demo.exception.ThriftException;
import com.qijiabin.demo.monitor.ServiceProxy;
import com.qijiabin.demo.monitor.statistic.MonitorService;
import com.qijiabin.demo.zookeeper.AddressRegister;
import com.qijiabin.demo.zookeeper.IPResolve;
import com.qijiabin.demo.zookeeper.support.LocalNetworkIPResolve;

/**
 * ========================================================
 * 日 期：2016年4月12日 下午12:02:50
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：服务端注册服务工厂
 * ========================================================
 * 修订日期     修订人    描述
 */
public class ThriftServiceServerFactory implements InitializingBean, Closeable {
	
	private static final int BOSS_THREAD_DEFAULT_COUNT = 1;
	private static final int WORKER_THREAD_DEFAULT_COUNT = 4;
	private static final Boolean ZK_NIFTY_SHUTDOWN_HOOK = false;
	//服务名称
	private String name;
	private String hostname = null;
	// 服务注册本机端口
	private Integer port = 8299;
	// 优先级
	private Integer weight = 1;// default
	// 服务实现类
	private Object service;// serice实现类
	// 服务版本号
	private String version;
	// 解析本机IP
	private IPResolve ipResolve;
	// 服务注册
	private AddressRegister addressRegister;
	private ThriftServerDefBuilder thriftServerDefBuilder;
	// NettyServer
	private NettyServerTransport server = null;
	private MonitorService monitorService;
	private Boolean isMonitor;
	

	/**
	 * set方法成功注入到bean实例中之后，进行一些资源的初始化操作
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (isMonitor) {
			this.monitorService.start();
		}
		if (ipResolve == null) {
			ipResolve = new LocalNetworkIPResolve();
		}
		String serverIP = ipResolve.getServerIp();
		if (StringUtils.isEmpty(serverIP)) {
			throw new ThriftException("cant find server ip...");
		}

		hostname = serverIP + ":" + port + ":" + weight;
		Class<?> serviceClass = service.getClass();
		// 获取实现类接口
		Class<?>[] interfaces = serviceClass.getInterfaces();
		if (interfaces.length == 0) {
			throw new IllegalClassFormatException("service-class should implements Iface");
		}
		// reflect,load "Processor";
		TProcessor processor = null;
		String serviceName = null;
		for (Class<?> clazz : interfaces) {
			String cname = clazz.getSimpleName();
			if (!cname.equals("Iface")) {
				continue;
			}
			serviceName = clazz.getEnclosingClass().getName();
			String pname = serviceName + "$Processor";
			try {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				Class<?> pclass = classLoader.loadClass(pname);
				if (!TProcessor.class.isAssignableFrom(pclass)) {
					continue;
				}
				Constructor<?> constructor = pclass.getConstructor(clazz);
				processor = (TProcessor) constructor.newInstance(new ServiceProxy().wrapper(service, name, version, monitorService, isMonitor));
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (processor == null) {
			throw new IllegalClassFormatException("service-class should implements Iface");
		}
		thriftServerDefBuilder = new ThriftServerDefBuilder()
				.listen(port)
				.withProcessor(processor);
		server = new NettyServerTransport(thriftServerDefBuilder.build(),
				defaultThriftServerConfigBuilder().build(),new DefaultChannelGroup());
		server.start();
		// 注册服务
		if (addressRegister != null) {
			addressRegister.register(serviceName, version, hostname);
		}
		//清理资源钩子
		if (ZK_NIFTY_SHUTDOWN_HOOK) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
	            public void run() {  
	                try {  
	                	if(addressRegister != null) {
	                		addressRegister.unregister(name, version, hostname);
	                		addressRegister.close();
	        			}
	                	Thread.sleep(1000 * 5);  
	                	close();
	                } catch (Exception e) {  
						e.printStackTrace();
					}
	            }
	        });			
		}
	}
	
	private NettyServerConfigBuilder defaultThriftServerConfigBuilder() throws Exception {
		try {
			NettyServerConfigBuilder configBuilder = NettyServerConfig.newBuilder();
			configBuilder.setBossThreadCount(BOSS_THREAD_DEFAULT_COUNT);
			configBuilder.setWorkerThreadCount(WORKER_THREAD_DEFAULT_COUNT);
			ExecutorService boss = newFixedThreadPool(BOSS_THREAD_DEFAULT_COUNT);
			ExecutorService workers = newFixedThreadPool(WORKER_THREAD_DEFAULT_COUNT);
			configBuilder.setBossThreadExecutor(boss);
			configBuilder.setWorkerThreadExecutor(workers);
			return configBuilder;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public void close() {
		if (server != null) {
			try {
				server.stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setPort(Integer port) {
		this.port = port;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public void setService(Object service) {
		this.service = service;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setIpResolve(IPResolve ipResolve) {
		this.ipResolve = ipResolve;
	}

	public void setAddressRegister(AddressRegister addressRegister) {
		this.addressRegister = addressRegister;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsMonitor() {
		return isMonitor;
	}

	public void setIsMonitor(Boolean isMonitor) {
		this.isMonitor = isMonitor;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

}