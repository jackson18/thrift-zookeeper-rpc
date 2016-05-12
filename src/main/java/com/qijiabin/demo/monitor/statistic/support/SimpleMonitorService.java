package com.qijiabin.demo.monitor.statistic.support;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qijiabin.demo.monitor.statistic.MonitorService;


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
	private final BlockingQueue<Statistics> queue = new LinkedBlockingQueue<Statistics>(100000);
	// 定时任务执行器
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	
	public void start() {
        Thread writeThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
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
        
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    new SimpleVisualService().draw(); // 绘制图表
                } catch (Throwable t) { // 防御性容错
                    logger.error("Unexpected error occur at draw stat chart, cause: " + t.getMessage(), t);
                }
            }
        }, 1, 300, TimeUnit.SECONDS);
	}
	
	/**
	 * 将统计数据写入文件
	 * @throws Exception
	 */
	private void write() throws Exception {
		Statistics statistics = queue.take();
		Date now = new Date();
        String day = new SimpleDateFormat("yyyyMMdd").format(now);
        SimpleDateFormat format = new SimpleDateFormat("HHmm");
        
        for (String type : TYPES) {
        	String filename = STATISTICS_DIRECTORY 
        			+ "/" + day 
        			+ "/" + statistics.getClazz().getName()
        			+ "/" + statistics.getMethod().getName()
        			+ "/" + CONSUMER + "." + type;
        	File file = new File(filename);
        	File dir = file.getParentFile();
        	if (dir != null && ! dir.exists()) {
        		dir.mkdirs();
        	}
        	FileWriter writer = new FileWriter(file, true);
        	try {
        		if (type.equals(SUCCESS)) {
        			writer.write(format.format(now) + " " + statistics.getTime() + "\n");
        		} else if (type.equals(CONCURRENT)){
        			writer.write(format.format(now) + " " + statistics.getConcurrent() + "\n");
        		}
        		writer.flush();
        	} finally {
        		writer.close();
        	}
		}
	}
	
	/**
	 * 信息采集
	 */
	@Override
	public void collect(Statistics statistics) {
		queue.offer(statistics);
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
		if (time > 0) {
			int concurrent = getConcurrent(clazz, method).get(); // 当前并发数
			if(logger.isDebugEnabled()){
				logger.debug("此次请求耗时：{}毫秒,并发数为：{},method：{},是否出错：{}", time, concurrent, method.getName(), error);
			}
			Statistics statistics = new Statistics(clazz, method, concurrent, time, error);
			collect(statistics);
		}
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