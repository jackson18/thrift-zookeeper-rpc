package com.qijiabin.demo.monitor.dao;

import java.io.IOException;
import java.io.Reader;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 * ========================================================
 * 日 期：2016年5月4日 下午2:37:36
 * 作 者：qijiabin
 * 版 本：1.0.0
 * 类说明：
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class SqlMapClientManager {
	
	private static SqlMapClient sqlMapper;

	static {
		try {
			Reader reader = Resources.getResourceAsReader("com/qijiabin/demo/monitor/dao/SqlMapConfig.xml");
			sqlMapper = SqlMapClientBuilder.buildSqlMapClient(reader);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static final SqlMapClient getClient() {
		return sqlMapper;
	}
	
}
