package com.qijiabin.demo.zookeeper;

/**
 * ========================================================
 * 日 期：2016年4月12日 上午11:28:40
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：解析thrift-server端IP地址，用于注册服务
 * 1) 可以从一个物理机器或者虚机的特殊文件中解析
 * 2) 可以获取指定网卡序号的Ip
 * ========================================================
 * 修订日期     修订人    描述
 */
public interface ThriftServerIpResolve {
	
public String getServerIp() throws Exception;
	
	public void reset();
	
	//当IP变更时,将会调用reset方法
	public static interface IpRestCalllBack{
		public void rest(String newIp);
	}
}
