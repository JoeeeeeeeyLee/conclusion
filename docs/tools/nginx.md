# 白名单

## 1. 需求

+ 监听443端口，确保在IP列表中的IP可以进行访问，其他的IP不可以访问。
+ 校验
  + IP地址唯一校验
  + IP地址长度校验：最大15字符。
  + IP地址格式校验：X.X.X.X
  + 备注的最大长度为64字符
+ 移除或批量移除IP
+ 修改IP或者备注
+ 系统管理员预设一个IP，这个IP不可以被删除。

### 2.实现

可以利用Nginx，不停的编辑```nginx.conf```，校验的时候校验参数就可以，这里面总结一下，有两个增加功能，分别是增加备注和增加IP，在第二项校验中，具体实现

createIP

+ 唯一校验，可以在实现createIP方法的时候，利用HashMap来确定唯一性，这个Map应该设置成类变量
+ 长度校验，当通过唯一性校验，在createIP方法直接判断输入的IP字符串的长度
+ 格式校验，通过前两个校验后，从头开始遍历，查看是否满足格式要求

createRemark

+ 长度校验，可以在添加备注字符串的时候，直接判断

removeTheIP

+ 首先需要验证这个移除的IP是不是预设的那个IP，如果是的话则返回失败，并提醒不能删除系统预置IP，预置IP应该设置为一个常量
+ 如果不是则根据map找到这个IP，然后删掉

removeIPs

+ 首先挨个检查这些要删除的IP是否是系统预置IP，如果不是直接删掉
+ 否则，跳过删除步骤，进行下一个IP的判断

updateIP

+ 这个其实可以在这个方法中先直接调用removeTheIP，然后再直接调用createIP（这里要明确一个问题，这个系统预置IP是不是可以被更改）

updateRemark

+ 将原先的值覆盖，变为修改后的备注

InitIP

+ 设置系统预置IP，放到map中

### 3. nginx.conf

白名单或者黑名单有两种实现，一种是在外部实现一个`*.conf`文件，然后在`nginx.conf`文件中`include *.conf`，当然这需要`nginx.conf`和`*.conf`文件在同一级目录上

另一种是直接在nginx.conf文件中的对应的location块中直接进行书写

```Java
//比如我们现在要禁止一个ip但是我们允许其他所有的ip访问
//在location块下，我们使用
{
  deny 125.20.247.58; //这后面必须要有;
  allow all;
}
//使用nginx -t查看配置文件是否有错误
nginx -t
//使用nginx -s reload重新加载配置文件
nginx -s reload
```

```java
//禁止ip的顺序，和允许访问的顺序，是有关联的
//比如你这样写,此时代表所有的ip都不能访问，这是因为率先匹配到第一个原则，会直接忽略掉第二个原则
{
    deny all;
    allow 125.20.247.58;
}
```

Github上有一个库，可以直接实现调用，根据库写的demo

```java
public static void main(String[] args) {
  try {
    nginxparasesample.paraseNginx("/usr/local/etc/nginx/nginx.conf");
  }catch (Exception e){
    e.printStackTrace();
  }
}

public static void paraseNginx(String filePath) throws Exception {
  //读取配置文件
  NgxConfig ngxConfig=NgxConfig.read(filePath);
  //解析配置文件中的worker_processes
  NgxParam workerProcesses = ngxConfig.findParam("worker_processes");
  //获取worker_processes的值,getValue的值是String类型
  System.out.println("worker_processes:"+workerProcesses.getValue());

  //解析http块下的所有server块中的location块
  List<NgxEntry> locationServers=ngxConfig.findAll(NgxConfig.BLOCK,"http","server","location");
  Set<String> noRepeatLocationServers=new HashSet<>();
  for (NgxEntry entry:locationServers) {
    NgxParam locationParam=((NgxBlock)entry).findParam("deny");
    if (locationParam==null){
      continue;
    }
    //这个地方查询出来的结果永远是配置文件中的那一行是什么，就显示什么，所以需要进行处理，
    //比如这个地方就是deny  all;     注意这里是有分号的，原汁原味的显示
    //所以我们需要将这个进行处理，只要后面的ip，当然这里面的ip是一个特殊值代表全部ip
    String[] denyArray=locationParam.toString().split(" ");
    String tmp=denyArray[1];
    String[] temp=tmp.split(";");
    noRepeatLocationServers.add(temp[0]);
  }
  for (String noRepeatLocationServer:noRepeatLocationServers){
    System.out.println(noRepeatLocationServer);
  }
  System.out.println("已结束");
}
```

4.nginx.conf中的upstream与proxy_pass

Nginx可以实现正向代理和反向代理，正向代理的对象是客户端，反向代理的对象是服务端。做正向代理时，客户端发起请求其访问目标应该是真实服务器，而反向代理时，客户端发起请求其访问目标应该是代理服务器，代理服务器将真实服务器的数据发给客户端。反向代理通常是作为负载均衡来分发流量给后端的应用程序服务器，以此来提高性能

实现负载均衡需要用到`ngx_http_upstream_module和proxy_pass`模块，`upstream`模块只能定义在http块中，该模块定义了需要反向代理的服务器池，然后进行负载均衡，最终再由`proxy_pass`块进行反向代理。
