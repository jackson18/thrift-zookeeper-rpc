package com.qijiabin.demo.monitor.bean;

import java.util.Date;

/**
 * ========================================================
 * 日 期：2016年5月4日 上午11:51:42
 * 作 者：qijiabin
 * 版 本：1.0.0
 * 类说明：
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class Statistic {

	private int id;
	private String service;
	private String method;
	private long time;
	private int concurrent;
	private Date createTime;
	private int isError;
	
	public Statistic() {
	}
	
	public Statistic(String service, String method, long time, int concurrent, Date createTime, int isError) {
		super();
		this.service = service;
		this.method = method;
		this.time = time;
		this.concurrent = concurrent;
		this.createTime = createTime;
		this.isError = isError;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getConcurrent() {
		return concurrent;
	}
	public void setConcurrent(int concurrent) {
		this.concurrent = concurrent;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getIsError() {
		return isError;
	}
	public void setIsError(int isError) {
		this.isError = isError;
	}
	
}
