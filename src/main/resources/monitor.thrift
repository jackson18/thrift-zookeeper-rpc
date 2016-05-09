namespace java com.qijiabin.demo.monitor.thrift

struct BizMethodInfo{
   1: string name;
   2: byte argsNum;
   3: list<string> argsType;
}

struct BizMethodInvokeInfo{
   1: i64 totalCount;
   2: i64 successCount;
   3: i64 failureCount;
   4: i64 successAverageTime;
   5: i64 successMinTime;
   6: i64 successMaxTime;
}


service MonitorService {
  string getName()

  string getVersion()

  list<BizMethodInfo> getServiceBizMethods()

  map<string,BizMethodInvokeInfo> getBizMethodsInvokeInfo()

  BizMethodInvokeInfo getBizMethodInvokeInfo(1:string methodName)

}
