package com.qijiabin.demo.monitor.statistic;
/**
 * ========================================================
 * 日 期：2016年4月18日 下午2:52:06
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：监听器服务接口
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public interface Monitor {

	String CONSUMER = "consumer";
	
	String[] TYPES = {"success", "concurrent"};
	
	String SUCCESS = "success";
	
	String CONCURRENT = "concurrent";
	
	String STATISTICS_DIRECTORY = "/opt/monitor/statistics";
	
	String CHARTS_DIRECTORY = "/opt/monitor/charts";
	
}