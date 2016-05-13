package com.qijiabin.demo.monitor.statistic.support;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * ========================================================
 * 日 期：2016年5月11日 下午2:15:51
 * 作 者：qijiabin
 * 版 本：1.0.0
 * 类说明：http远程调用类，可以用来发送统计数据
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class RemoteHttpWrite {
	
	private static final String HTTP_GET_URL = "http://localhost:8080/thrift-zookeeper-rpc-web/statistic/add";
	
	public static void write(Statistic entity) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		StringBuilder sb = new StringBuilder();
		sb.append("?service=").append(entity.getClass().getName())
		.append("&method=").append(entity.getMethod().getName())
		.append("&time=").append(entity.getTakeTime())
		.append("&concurrent=").append(entity.getConcurrent())
		.append("&createTime=").append(sdf.format(new Date()))
		.append("&isError=").append(entity.getIsError() ? 1 : 0);
		String loginUrl = HTTP_GET_URL + sb.toString();
		// 使用http请求写入数据库
		requestGet(loginUrl);
	}

	/**
	 * http get请求
	 * @param urlWithParams
	 */
	private static void requestGet(String urlWithParams) {
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
	
}
