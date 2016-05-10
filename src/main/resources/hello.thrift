namespace java com.qijiabin.demo.thrift

service HelloWorldService {
	string sayHello(1:string username)
}