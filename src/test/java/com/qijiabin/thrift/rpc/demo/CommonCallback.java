package com.qijiabin.thrift.rpc.demo;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.thrift.async.TAsyncClient;

/**
 * ========================================================
 * 日 期：2016年4月19日 下午1:49:42
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class CommonCallback {

	protected GenericObjectPool<TAsyncClient> pool;
	protected TAsyncClient client;
	
	public GenericObjectPool<TAsyncClient> getPool() {
		return pool;
	}

	public void setPool(GenericObjectPool<TAsyncClient> pool) {
		this.pool = pool;
	}

	public TAsyncClient getClient() {
		return client;
	}

	public void setClient(TAsyncClient client) {
		this.client = client;
	}
	
	protected void giveBackResrouce() {
		if (pool != null && client != null) {
			pool.returnObject(client);
		}
	}
	
}
