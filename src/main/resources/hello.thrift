include "monitor.thrift"
namespace java com.qijiabin.demo.thrift

service HelloWorldService extends monitor.MonitorService {
	string sayHello(1:string username)
}