package com.qijiabin.demo.monitor.statistic.support;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
public class SimpleMonitorService extends MonitorService{

	private static final Logger logger = LoggerFactory.getLogger(SimpleMonitorService.class);
	private final BlockingQueue<Statistic> queue = new LinkedBlockingQueue<Statistic>(100000);
	// 定时任务执行器
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("HHmm");

	
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
		Statistic entity = queue.take();
		Date now = new Date();
        String day = sdf1.format(now);
        
        for (String type : TYPES) {
        	String filename = STATISTICS_DIRECTORY 
        			+ "/" + day 
        			+ "/" + entity.getServiceName()
        			+ "/" + entity.getMethod().getName()
        			+ "/" + PROVIDER + "." + type;
        	File file = new File(filename);
        	File dir = file.getParentFile();
        	if (dir != null && ! dir.exists()) {
        		dir.mkdirs();
        	}
        	FileWriter writer = new FileWriter(file, true);
        	try {
        		if (SUCCESS.equals(type)) {
        			writer.write(sdf2.format(now) + " " + entity.getTakeTime() + "\n");
        		} else if (ERROR.equals(type) && entity.getIsError()) {
        			writer.write(sdf2.format(now) + " " + 1 + "\n");
        		} else if (CONCURRENT.equals(type)){
        			writer.write(sdf2.format(now) + " " + entity.getConcurrent() + "\n");
        		}
        		writer.flush();
        	} finally {
        		writer.close();
        	}
		}
	}
	
	/**
	 * 统计信息采集
	 * @param entity
	 */
	public void collect(Statistic entity) {
		queue.offer(entity);
	}
    
	/**
	 * 统计信息采集
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void collect(String serviceName, String serviceVersion, Class clazz, Method method, Object[] args,
			int concurrent, long takeTime, boolean isError) {
		Statistic entity = new Statistic(serviceName, method, concurrent, takeTime, isError);
		collect(entity);
	}

}