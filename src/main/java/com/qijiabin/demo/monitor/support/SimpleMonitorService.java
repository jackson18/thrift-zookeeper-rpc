package com.qijiabin.demo.monitor.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qijiabin.demo.monitor.MonitorService;
import com.qijiabin.demo.monitor.bean.Statistic;

/**
 * ========================================================
 * 日 期：2016年4月18日 下午2:46:08
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：数据搜集器
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class SimpleMonitorService implements MonitorService{

	private static final Logger logger = LoggerFactory.getLogger(SimpleMonitorService.class);
	private final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();
	private final BlockingQueue<Statistic> queue;
	private final Thread writeThread;
	private volatile boolean running = true;
	private static SimpleMonitorService INSTANCE = null;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    

	public static SimpleMonitorService getInstance() {
        return INSTANCE;
    }
	
	public SimpleMonitorService() {
		queue = new LinkedBlockingQueue<Statistic>(100000);
        writeThread = new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        write(); // 记录统计日志
                    } catch (Throwable t) { // 防御性容错
                        logger.error("Unexpected error occur at write stat log, cause: " + t.getMessage(), t);
                        try {
                            Thread.sleep(5000); // 失败延迟
                        } catch (Throwable t2) {
                        }
                    }
                }
            }
        });
        writeThread.setDaemon(true);
        writeThread.setName("SimpleMonitorAsyncWriteLogThread");
        writeThread.start();
        INSTANCE = this;
	}
	
	/**
	 * 将统计数据写入数据库
	 * @throws Exception
	 */
	private void write() throws Exception {
		Statistic entity = queue.take();
		StringBuilder sb = new StringBuilder();
		sb.append("?service=").append(entity.getService())
		.append("&method=").append(entity.getMethod())
		.append("&time=").append(entity.getTime())
		.append("&concurrent=").append(entity.getConcurrent())
		.append("&createTime=").append(sdf.format(entity.getCreateTime()))
		.append("&isError=").append(entity.getIsError());
		String loginUrl = HTTP_GET_URL + sb.toString();
		//使用http请求写入数据库
		requestGet(loginUrl);
	}
	
	/**
	 * http get请求
	 * @param urlWithParams
	 */
	public static void requestGet(String urlWithParams) {
        try {
			CloseableHttpClient httpclient = HttpClientBuilder.create().build();
			HttpGet httpget = new HttpGet(urlWithParams);   
			//配置请求的超时设置
			RequestConfig requestConfig = RequestConfig.custom()  
			        .setConnectionRequestTimeout(100)
			        .setConnectTimeout(100)  
			        .build();  
			httpget.setConfig(requestConfig); 
			httpclient.execute(httpget);        
			httpget.releaseConnection();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 信息采集
	 */
	@Override
	public void collect(Statistic statistic) {
		queue.offer(statistic);
	}
	
	/**
	 * 信息采集
	 * @param clazz
	 * @param method
	 * @param args
	 * @param start
	 * @param error
	 */
	@Override
	public void collect(Class<?> clazz, Method method, long start, boolean error) {
		long end = System.currentTimeMillis(); // 记录结束时间戮
		long time = end - start;
		int concurrent = getConcurrent(clazz, method).get(); // 当前并发数
		int isError = error?1:0;
		Statistic entity = new Statistic(clazz.getName(), method.getName(), time, concurrent, new Date(), isError);
		collect(entity);
    }
    
	/**
	 * 获取并发计数器
	 * @param clazz
	 * @param method
	 * @return
	 */
	@Override
    public AtomicInteger getConcurrent(Class<?> clazz, Method method) {
        String key = clazz.getName() + "." + method.getName();
        AtomicInteger concurrent = concurrents.get(key);
        if (concurrent == null) {
            concurrents.putIfAbsent(key, new AtomicInteger());
            concurrent = concurrents.get(key);
        }
        return concurrent;
    }

}

