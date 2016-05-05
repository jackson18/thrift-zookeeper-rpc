package com.qijiabin.demo.monitor;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import com.qijiabin.demo.monitor.bean.Statistic;

/**
 * ========================================================
 * 日 期：2016年4月18日 下午2:16:24
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：数据搜集服务接口
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public interface MonitorService extends Monitor{
	
	/**
	 * 监控数据采集
	 * 记录监控来源主机，应用，接口，方法信息
	 * 记录调用的成功次数，失败次数，成功调用总耗时，平均时间将用总耗时除以成功次数
	 * @param statistics
	 */
    public void collect(Statistic statistic);
    
    /**
     * 监控数据采集
	 * 记录监控来源主机，应用，接口，方法信息
	 * 记录调用的成功次数，失败次数，成功调用总耗时，平均时间将用总耗时除以成功次数
     * @param clazz
     * @param method
     * @param start
     * @param error
     */
    public void collect(Class<?> clazz, Method method, long start, boolean error);
    
    /**
	 * 获取并发计数器
	 * @param clazz
	 * @param method
	 * @return
	 */
    public AtomicInteger getConcurrent(Class<?> clazz, Method method);

}