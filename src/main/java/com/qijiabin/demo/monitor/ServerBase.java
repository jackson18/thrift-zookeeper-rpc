package com.qijiabin.demo.monitor;

import java.util.HashMap;
import java.util.Map;

/**
 * ========================================================
 * 日 期：2016年5月8日 上午10:55:57
 * 作 者：jackson
 * 版 本：1.0.0
 * 类说明：服务器启动基类，用于服务参数控制
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public abstract class ServerBase {
	
	private final Map<String, String> options = new HashMap<String, String>();
	
	public  void setOption(String key, String value){
		this.options.put(key, value);
	}
	
	public Map<String, String> getOptions(){
		return this.options;
	}
	
}