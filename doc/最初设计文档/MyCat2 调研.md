
# MyCat2 调研

- 既定目标
- MyCat2 特性
- 应用场景
- MyCat2 高可用
- 安装 MyCat
- 启动 MyCat
    - 修改配置文件
    - 启动 MyCat
- 连接 MyCat
- 一主一从读写分离配置
    - 配置数据源
        - 数据源配置说明
    - 配置集群
        - 集群配置说明
    - 配置 schema
        - 创建 oms_dev 逻辑库
        - 创建 longpean 逻辑库
- 验证跨库 join 查询，子查询
- 验证读写分离效果
- 强制读主库
- proxy 事务
- 主键生成
- MyCat SQL 兼容性
- MyCat 监控
- 数据水平拆分
    - 逻辑库，逻辑表
    - 分片算法
    - 验证数据分表
        - 创建物理表
        - 配置数据源
        - 创建逻辑库
        - 配置分表规则
        - 测试写入数据
        - 测试数据写入与查询
- 数据迁移

## 既定目标

- 目标
    - 实现读写分离
    - 预期对数据做水平拆分
- 我们需要做什么
    - 开发，测试，生产环境 MySQL 都需要是至少一主一备,主备需要可以单独访问
    - 增加部署 MyCat 集群的机器
    - 开发，运维同学需要掌握 MyCat 使用&运维
    - 开发同学在应用中编写 sql 时要考虑 mycat 是否支（[mycat sql 兼容性](https://www.yuque.com/ccazhw/ml3nkf/442fb5725c71e519d5aa984ec96f9108 "mycat sql 兼容性")）
    - 处理引入 MyCat ，数据拆分所引入的新问题
- 我们能得到什么
    - 读写分离后单台 MySQL 实例的 QPS 会降低,读写压力分流减少了 MySQL 压力，提高系统吞吐能力
    - 数据分片后单库或表数据量减少更快的读，写响应速度

## MyCat2 特性

- 优化查询计划
  定制Calcite分布式查询引擎、编译SQL到关系代数表达式、规则优化引擎和代价优化引擎、生成物理执行计划、支持逻辑视图

- SQL支持语法
  任意跨库跨表join查询、支持跨库跨表非关联子查询、支持跨库跨表关联子查询、支持跨库跨表Window语法、支持全局二级索引、有限支持存储过程、支持可视化配置

- 高性能
  支持并行拉取结果集、支持自动调动后端结果集、支持多种路由注释、优化器注释

- 优化
  对请求的sql进行参数化、缓存物理执行计划、相同参数化sql的请求、将免去一些分析优化过程

- 支持原生协议
  前端协议MySQL网络通信协议、MySQL原生网络协议异步非阻塞、JDBC接口支持多种数据库、生成物理执行计划

- 支持定制任意多字段路由
  提供分片算法接口、优化器简化过滤条件、分片信息与关系表达式结合生成执行sql

项目地址：https://github.com/MyCATApache/Mycat2
项目官网：http://mycatone.top/

## 应用场景

1. 高并发访问：Mycat2的高性能和支持快速数据处理的特点适用于面向客户端的高并发访问的场景。
2. 数据库水平扩容：Mycat2的分库分表策略和自动化分片技术适用于需要对数据库进行适度的水平扩容的场景。
3. 读写分离：Mycat2的读写分离特性适用于需要进行大量读操作的场景，可以将写操作集中到主节点，将读操作负载均衡到从节点上。

## MyCat2 高可用

1. 主备切换：MyCat2 支持主备切换机制，可以通过配置主备节点，当主节点宕机时，自动切换到备节点上，从而保证了MyCat2的高可用。

2. Keepalived：Mycat2 也可以与 Keepalived 一起使用，通过将 Keepalived 与 MyCat2 集成，实现MyCat2的高可用。Keepalived 是一个实现 IP 热备的软件，可以在多个服务器之间进行切换，提高系统的可用性。

3. 热备机制：Mycat2 支持热备机制，可以通过备份数据进行恢复，实现对整个MyCat2系统的快速恢复。

4. 自动扩展：Mycat2 支持自动扩展机制，可以根据系统负载动态地增加集群节点数量，从而提高系统可用性和伸缩性。

5. 监控管理：Mycat2提供完善的监控管理平台，可以实时监控MyCat2的性能指标、运行状态等，并提供报警机制和日志记录等，从而保证了MyCat2自身的高可用性。


## 安装 MyCat

1. 下载安装程序包： http://dl.mycat.org.cn/2.0/install-template/mycat2-install-template-1.21.zip

2. 下载 jar 包: http://dl.mycat.org.cn/2.0/1.21-release/mycat2-1.21-release-jar-with-dependencies.jar

3. 把下载好的 jar 包放到 lib 目录下

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/bc4a6fd5ba8549e08f78905c6a94803f.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


MyCat 配置文件目录

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/2ce1cb3b4f6f4f538741a57968bf3283.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


`server.json` 是对 Mycat Server 的配置. ip , port , mycatId 之类的。

`users` 路径下存放的是 Mycat Server 用户的配置. 也就是哪些账号可以访问 MyCat Server , 和 MySQL 的账号类似. 默认会存在一个 `root.user.json` 配置文件. `ip` 配置了值会限定访问 IP. `transactionType` 是事务类型, 默认为 `proxy` (只支持本地事务) , 还支持 `XA` 事务.

`datasources` 路径下存放了数据源配置文件, 可以配置多个数据源,名称需要唯一。默认会存在一个 `prototypeDs.datasource.json` 原型数据源配置。

`clusters` 路径下存放了集群配置。默认会有一个 `prototype.cluster.json` 配置文件。 集群就是多个数据源所组成了一个数据源集群。在集群中可以做故障切换,负载均衡等。比数据源的可用性更高。集群中可以配置多个 `masters` 主节点，和多个 `replicas` 从节点。其中配置的值是 `datasource` 的名称。
这里再看上面的图就可以理解为什么 `targetName` 可以是 `cluster` 或 `datasource` 了，他们的作用都是用来访问物理库。

`schemas` 路径下存放了逻辑表和物理表的映射关系。

`sequences` 路径下存放的是对生成序列号规则的配置.

## 启动 MyCat

#### 修改配置文件

> 配置文件在 `conf` 路径下.

1. 修改 `conf/datasources` 路径下的 `prototypeDs.datasource.json` (原型库配置) 文件

> 在 Mycat2 中，原型库（Prototype）是一个基础库，它的作用是为其他独立的逻辑库提供一个原型或模板，方便其他库进行数据分片和数据路由。原型库是一个空库，不存储任何数据，只有表结构和分片规则。其他的逻辑库或者数据节点可以通过引用原型库，继承原型库的表结构和分片规则，从而实现数据的水平分片、读写分离等功能。

> 原型库的主要作用就是帮助 Mycat2 更好地实现数据分片和数据路由功能。通过使用原型库，Mycat2 可以实现对数据进行水平分片，并将数据分布到不同的数据节点上，从而实现对大型数据的存储和管理。此外，使用原型库还可以实现读写分离和负载均衡，让数据库的读写负载更加均衡，提高数据库的性能和稳定性。原型库还为开发者提供了更加灵活的数据模型设计和管理方式，方便数据库应用程序的开发和维护。

```
{
	"dbType":"mysql",
	"idleTimeout":60000,
	"initSqls":[],
	"initSqlsGetConnection":true,
	"instanceType":"READ_WRITE",
	"maxCon":1000,
	"maxConnectTimeout":3000,
	"maxRetryCount":5,
	"minCon":1,
	"name":"prototypeDs",
	"password":"Lp123@Rds456",
	"type":"JDBC",
	"url":"jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/?useUnicode=true&useSSL=false",
	"user":"dev",
	"weight":0
}
```

2. 修改 `conf/users` 路径下的 `root.user.json` 文件

> 注意这个文件配置的是连接到 mycat 的用户名密码，不是 mysql 的.
> 这里我用的是 root ，root

```
{
	"dialect":"mysql",
	"ip":null,
	"password":"root",
	"transactionType":"proxy",
	"username":"root"
}
```

#### 启动 MyCat

windows 环境
1. 进入 `bin` 路径下, 命令行中启用 powershell.

```
.\mycat install

.\mycat start
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/cf31c8ff12fc454b9da5ccab02724e9b.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/0b48b4af044644ccb39666d2c8ed5848.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


## 连接 MyCat

> 使用任意的数据库连接工具.如果没有修改 `conf/server.json` 文件中的 port 配置项，默认端口是 8066.

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/d004614607fe4f53b0db34a4ba769a06.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


连接上 mycat 后就可以向操作 mysql 数据库一样执行 sql 指令了.

```
show databases ;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/e7e94169a0e84caa84ae861dc5b4c4c2.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


创建库、表
```
CREATE DATABASE `test` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
use `test`;

CREATE TABLE employee (
	id INT PRIMARY KEY,
	name VARCHAR(100) NOT NULL,
	department_id INT NOT NULL
);

show tables ;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/635727a5a59b42d5b5a523bdd857ed94.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)



往 employee 表中写入数据
```
INSERT INTO employee (id, name, department_id) 
VALUES (1, 'John Doe', 1),
(2, 'Jane Doe', 1),
(3, 'Bob Smith', 2),
(4, 'Alice Jones', 2);
```
这时候会得到一个报错信息 `[42000][1142] SELECT command denied to user 'dev'@'219.144.252.152' for table 'employee'` 因为我们刚才在 mycat 上创建的只是逻辑库和逻辑表，在物理数据库上并没有真正的创建库和表.


## 一主一从读写分离配置

> 基于实现读写分离的目标，以及检验跨库查询的特性对 mycat 进行配置.准备一个写库和一个读库.

这里为了方便演示读库，写库我们都使用开发环境，逻辑上我们可以把他们看作是主从同步关系的两个不同实例:

写库：jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888
读库：jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/65a5cbf10174493dafb708e4c97ce222.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


#### 配置数据源

> 在 `conf/datasources` 路径下增加 `oms_dev0.datasource.json` , `oms_dev1.datasource.json` 文件. oms_dev0 可读写，oms_dev1 只读.

##### 数据源配置说明
> `type` : 数据源类型
`NATIVE` ,只使用NATIVE协议(即Mycat自研的连接MySQL的协议)
`JDBC` ,默认,只使用JDBC驱动连接
`NATIVE_JDBC` ,该数据源同一个配置同时可以使用NATIVE,JDBC

> `name` : 数据源名称

> `url` : JDBC连接URL
对应实际MySQL连接地址，可以无需指定MySQL schema。如果URL中配置了MySQL schema，相应的MySQL schema必须在物理库上先创建好，否则无法启动mycat。

> `password` : MySQL用户密码

> `instanceType` : 配置实例只读还是读写
`READ` 只读节点
`READ_WRITE` 读写节点
`WRITE` 只写节点

> `queryTimeout` : jdbc查询超时时间，单位：ms
默认30mills，根据实际业务需要调整超时时间

> `maxConnectTimeout` : 定时检查闲置连接，单位：ms

> `initSqlsGetConnection` : 每次获取jdbc连接是否都执行initSqls
true|false,默认:false

> JDBC禁用SSL属性有助提高性能

oms_dev0.datasource.json :

```
{
	"dbType":"mysql",
	"idleTimeout":60000,
	"initSqls":[],
	"initSqlsGetConnection":true,
	"instanceType":"READ_WRITE",
	"logAbandoned":true,
	"maxCon":1000,
	"maxConnectTimeout":3000,
	"maxRetryCount":5,
	"minCon":1,
	"name":"oms_dev",
	"password":"Lp123@Rds456",
	"queryTimeout":0,
	"removeAbandoned":false,
	"removeAbandonedTimeoutSecond":180,
	"type":"JDBC",
	"url":"jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/?useUnicode=true&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=GMT%2B8",
	"user":"dev",
	"weight":0
}
```

oms_dev1.datasource.json :
```
{
	"dbType":"mysql",
	"idleTimeout":60000,
	"initSqls":[],
	"initSqlsGetConnection":true,
	"instanceType":"READ",
	"logAbandoned":true,
	"maxCon":1000,
	"maxConnectTimeout":3000,
	"maxRetryCount":5,
	"minCon":1,
	"name":"oms_dev1",
	"password":"Lp123",
	"queryTimeout":0,
	"removeAbandoned":false,
	"removeAbandonedTimeoutSecond":180,
	"type":"JDBC",
	"url":"jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/?useUnicode=true&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=GMT%2B8",
	"user":"dev",
	"weight":0
}
```

再增加 `longpean0.datasource.json` `longpean1.datasource.json` 检验跨库查询.

longpean0.datasource.json :
```
{
	"dbType":"mysql",
	"idleTimeout":60000,
	"initSqls":[],
	"initSqlsGetConnection":true,
	"instanceType":"READ_WRITE",
	"logAbandoned":true,
	"maxCon":1000,
	"maxConnectTimeout":3000,
	"maxRetryCount":5,
	"minCon":1,
	"name":"longpean_0",
	"password":"Lp123@Rds456",
	"queryTimeout":0,
	"removeAbandoned":false,
	"removeAbandonedTimeoutSecond":180,
	"type":"JDBC",
	"url":"jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false",
	"user":"dev",
	"weight":0
}
```

longpean1.datasource.json :
```
{
	"dbType":"mysql",
	"idleTimeout":60000,
	"initSqls":[],
	"initSqlsGetConnection":true,
	"instanceType":"READ",
	"logAbandoned":true,
	"maxCon":1000,
	"maxConnectTimeout":3000,
	"maxRetryCount":5,
	"minCon":1,
	"name":"longpean_1",
	"password":"Lp123@Rds456",
	"queryTimeout":0,
	"removeAbandoned":false,
	"removeAbandonedTimeoutSecond":180,
	"type":"JDBC",
	"url":"jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false",
	"user":"dev",
	"weight":0
}
```

#### 配置集群

> 多个数据节点组成的逻辑节点.在mycat2里,它是把对多个数据源地址视为一个数据源地址(名称),并提供自动故障恢复,转移,即实现高可用,负载均衡的组件.

> 在集群配置中我们指定了是主从模式, 一个 master 数据源 一个 replica 数据源.

##### 集群配置说明
> `clusterType` : 集群类型
`SINGLE_NODE` : 单一节点
`MASTER_SLAVE` : 普通主从
GARELA_CLUSTER:garela cluster/PXC集群

> `readBalanceType`: 查询负载均衡策略
`BALANCE_ALL`(默认值) : 获取集群中所有数据源
`BALANCE_ALL_READ` : 获取集群中允许读的数据源
`BALANCE_READ_WRITE` : 获取集群中允许读写的数据源,但允许读的数据源优先
`BALANCE_NONE` : 获取集群中允许写数据源,即主节点中选择

> `switchType`: 主从切换设置
`NOT_SWITCH` : 不进行主从切换
`SWITCH` : 进行主从切换

oms_dev.cluster.json :
```
{
	"clusterType":"MASTER_SLAVE",
	"heartbeat":{
		"heartbeatTimeout":1000,
		"maxRetryCount":3,
		"minSwitchTimeInterval":300,
		"slaveThreshold":0
	},
	"masters":[
		"oms_dev0"
	],
  	"replicas":[
		"oms_dev1"
	],
	"maxCon":200,
	"name":"oms_dev",
	"readBalanceType":"BALANCE_ALL",
	"switchType":"SWITCH"
}

```

longpean_dev.cluster.json :
```
{
	"clusterType":"MASTER_SLAVE",
	"heartbeat":{
		"heartbeatTimeout":1000,
		"maxRetryCount":3,
		"minSwitchTimeInterval":300,
		"slaveThreshold":0
	},
	"masters":[
		"longpean_dev0"
	],
  	"replicas":[
		"longpean_dev1"
	],
	"maxCon":200,
	"name":"longpean_dev",
	"readBalanceType":"BALANCE_ALL",
	"switchType":"SWITCH"
}
```

## 配置 schema

> 在 `conf/schemas` 路径下存放 schema 配置文件.

重新启动 mycat
```
 .\mycat restart
```
##### schema 配置项说明

> `schemaName` schema 名称

> `targetName` 访问的集群或数据源名称

> `customTables` 定义自定义表映射规则，可以将自定义的SQL语句映射到物理表上。此配置一般用于一些特殊的查询需求，例如多表关联查询、视图查询、存储过程等，在MyCAT中通过自定义表映射规则，可以将这些特殊需求转化为标准的SQL语句，从而实现对数据的查询和操作。

> `globalTables` 定义全局表规则，全局表是一种特殊的表，可以在不同的逻辑库之间进行数据共享，即可以在一个逻辑库中插入、更新、删除数据，并在另一个逻辑库中查询到这些数据。通过定义 globalTables ，可以将多个逻辑库中的表定义为全局表，从而实现数据共享的功能。

> `normalProcedures` 定义普通存储过程规则。普通存储过程是一种可以在MyCAT服务端上执行的SQL脚本，通过预编译和缓存操作可以提高SQL执行的效率。通过定义 normalProcedures ，可以将多个普通存储过程定义为一个逻辑库中的公共存储过程，从而方便SQL语句的调用和执行。

> `normalTables` 定义普通表规则。普通表是一种与MySQL实例中的物理表相对应的虚拟表，在MyCAT服务端上进行路由和转发操作。

> `shardingTables` 定义需要分片的表。


##### 创建 oms_dev 逻辑库

创建逻辑库：
```
CREATE DATABASE `oms_dev` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

修改 schema 配置：
```
#指定 oms 逻辑库默认的 targetName 为 prototype , mycat会自动加载已经有的物理表或者视图作为单表.
 
/*+ mycat:createSchema{
  "customTables":{},
  "globalTables":{},
  "normalTables":{},
  "schemaName":"oms_dev",
  "shardingTables":{},
  "targetName":"prototype"
} */;
```
在 `conf/schemas` 路径下生成了 `oms_dev.schema.json` 文件：

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/4e05564e2b4944f683a284a06727583b.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


```
{
	"customTables":{},
	"globalTables":{},
	"normalProcedures":{},
	"normalTables":{},
	"schemaName":"oms_dev",
	"shardingTables":{},
	"targetName":"prototype",
	"views":{}
}
```

在连接工具中查看：
```
use oms_dev ;
show tables ;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/0cafd53dcfa24987be103cd8cbc3cffa.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)

执行查询语句：
```
select * from t_oms_order_info limit 100;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/e8369ca9d4554689b26a020fe69a08c1.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)



##### 创建 longpean 逻辑库

> longpean 库中表太多，这里我们就使用手动编写配置文件的方式只创建两个个逻辑表作为演示.

longpean.schema.json :
```
{
	"customTables":{},
	"globalTables":{},
	"normalProcedures":{},
	"normalTables":{
		"t_py_current_stock":{
			"createTableSQL": null ,
			"locality":{
				"schemaName":"dev",
				"tableName":"t_py_current_stock",
				"targetName":"longpean_dev"
			}
		},
		"t_user_baseinfo":{
			"createTableSQL": null ,
			"locality":{
				"schemaName":"dev",
				"tableName":"t_user_baseinfo",
				"targetName":"longpean_dev"
			}
		}
	},
	"schemaName":"longpean",
	"shardingTables":{},
	"views":{}
}
```

重启 mycat：
```
 .\mycat restart
```
执行查询语句：
```
show databases ;
use longpean ;
show tables ;
select * from t_py_current_stock limit 100 ;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/49830620ab684f00bd8924f162d527ec.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


## 验证跨库 join 查询，子查询
执行跨库 子查询 、 join 查询：
```
select ( select name from longpean.t_user_baseinfo where id = t_oms_order_info.created_by ) created_user_name , t_oms_order_info.*
from t_oms_order_info
left join t_oms_order_details detail
    on t_oms_order_info.id = detail.order_id
    left join longpean.t_py_current_stock stock
on detail.sku = stock.sku
    and detail.store_id = stock.StoreID
where t_oms_order_info.shop_id = 135602
and t_oms_order_info.platform_code = 'shopee'
limit 100;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/2725bf358d4343c4a8aba9b9b0a0f772.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


连接 mycat 查询耗时 20 s 328 ms

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/9be5a280f9e9412daa522ff950a5f5b4.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


在物理库中执行该 sql 观察耗时 19 s 236 ms

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/59384ab4ac3b4e54b5260fe4f5289222.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)



## 验证读写分离效果

1. 修改 `oms_dev1` 数据源连接 `oms_test` 库.

> 使用注解配置方式无需重启 mycat.
```
/*+ mycat:createDataSource{
	"dbType":"mysql",
	"idleTimeout":60000,
	"initSqls":[],
	"initSqlsGetConnection":true,
	"instanceType":"READ",
	"maxCon":1000,
	"maxConnectTimeout":3000,
	"maxRetryCount":5,
	"minCon":1,
	"name":"oms_dev1",
	"password":"htest123@longpean",
	"type":"JDBC",
	"url":"jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/oms_test?useUnicode=true&serverTimezone=Asia/Shanghai&allowMultiQueries=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false",
	"user":"htest",
	"weight":0
} */;
```

修改数据源后查询结果确认:
```
/*+ mycat:showDataSources{} */
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/db4204b9d4e94924b3a7582aa579d0da.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


在 `oms_test` 物理库执行 insert 语句：
```
insert into t_face_sheet_log (order_id , logics_way_id , success , err_msg , created_time)
values (666666666 , 666666666 , 0 , 'mycat insert test' , now()) ;
```
![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/a7773f54c940408b8de5c4fddd048669.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


因为我们在创建 `oms_dev` 逻辑库以及其中的逻辑表时,指定的 targetName 为 `prototype` , 这意味着去访问 `oms_dev` 逻辑库时使用的是默认的 prototype cluster (对应的配置文件为 prototype.cluster.json). 所以我们要修改 prototype cluster 配置。

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/c938419010a84feaa6310eae67ea5c78.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


```
/*! mycat:createCluster{
	"clusterType":"MASTER_SLAVE",
	"heartbeat":{
		"heartbeatTimeout":1000,
		"maxRetry":3,
		"minSwitchTimeInterval":300,
		"slaveThreshold":0
	},
	"masters":[
		"prototypeDs"
	],
	"replicas":[
		"oms_dev0",
		"oms_dev1"
	],
	"maxCon":200,
	"name":"prototype",
	"readBalanceType":"BALANCE_ALL",
	"switchType":"SWITCH"
} */;
```

修改完成后查询验证：
```
/*+ mycat:showClusters{} */
```
![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/662520a5c49a46ec8a6af3022ff90958.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)

执行查询语句验证读写分离,因为我们是随机策略会在 `oms_dev0` 和 `oms_dev1` 两个数据源之间做随机的负载均衡,所以只要有时能读到有时读不到就说明读写分离是生效的.
```
select * from t_face_sheet_log where order_id = 666666666 ;
```

由于 mycat 会在 sql 上加上物理库名，所以我们的 sql 被路由到 `oms_dev1` 数据源执行后会得到一个报错.这就证明它已经路由到了读库执行.
```
select * from oms_dev.t_face_sheet_log where order_id = 666666666 ;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/4149bb00592e48918504acae3ff6209a.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


## 强制读主库

连接 mycat 执行 sql, 这时数据肯定被写入了我们的 `oms_dev` 物理库.
```
insert into t_face_sheet_log (order_id , logics_way_id , success , err_msg , created_time)
values (99999999 , 99999999 , 0 , 'mycat insert test' , now()) ;
```

sql 强制从主库读：
官方文档: https://www.yuque.com/ccazhw/tuacvk/qr1mw4 ， 按照官方文档示例进行测试发现不起作用，可能是哪里配置的有问题.
```
/*#mycat:db_type=master*/ select * from t_face_sheet_log where order_id = 99999999 ;

/*!mycat:db_type=master*/ select * from t_face_sheet_log where order_id = 99999999 ;

/**mycat:db_type=master*/ select * from t_face_sheet_log where order_id = 99999999 ;
```

> 作为客户端能与 mycat 交互的唯一方式就是指令和 sql , 这意味着我们不得不在 sql 代码层面进行侵入。类似 /*#mycat:db_type=master*/ 这种注解查询.

## proxy 事务

- proxy 本地事务,在涉及大于1个数据库的事务,commit阶段失败会导致不一致,但是兼容性最好

- xa 事务,需要确认存储节点集群类型是否支持XA

> 不涉及跨库事务请把事务类型改成proxy,不要使用XA.
Mycat2事务基于Vertx的异步SQL接口构建,但是其实现是自研的mysql协议实现.

> 暂时Mycat2缺XA(在事务涉及多个连接的情况下)死锁检查,实现该功能需要改动MySQL的源码,所以业务代码添加全局锁或者梳理事务代码来避免XA死锁,或者直接只使用单库分表.mycat在事务内只使用一个连接,这样不涉及XA事务.或者配合使用mycat内置的锁函数,强制使事务串行。

在事务中写入和查询都会走主库.
```
set autocommit = 0;
begin END;
insert into t_face_sheet_log (order_id , logics_way_id , success , err_msg , created_time)
values (987456123 , 987456123 , 0 , 'mycat tx test' , now()) ;

select * from t_face_sheet_log where order_id = 987456123 ;
select * from t_face_sheet_log where order_id = 99999999 ;

commit END;

select * from t_face_sheet_log where order_id = 99999999 ;
```

在事务提交后查询 order_id = 99999999 一段时间内一直可以查到，这是因为 mycat2 支持会话粘滞，Mycat2从2022-5-8在1.21和1.22上支持会话粘滞,实现上是数据源名字粘滞，会话粘滞特性会把上次写入的数据源记录下来,如果下一次是查询,就会使用上一个数据源查询。

会话粘滞功能默认开启,可以通过下面配置开启关闭
```
//server.json
{
  "server":{
    "stickySession": true,
    "stickySessionTime":-1
  }
}
```

> stickySessionTime
默认值:-1
可选值:整数范围,单位毫秒
当-1的时候,每次指定主数据源的时候,下一次选择数据源一定会选择主数据源
当大于等于0的是,每次指定主数据源的时候,下一次选择数据源,在时间内,会选择主数据源,时间外负载均衡.

## 主键生成

- mycat的自增序列
> 如果不需要使用mycat的自增序列,而使用mysql本身的自增主键的功能,需要在配置中更改对应的建表sql,不设置AUTO_INCREMENT关键字,这样,mycat就不认为这个表有自增主键的功能,就不会使用mycat的全局序列号.
这样,对应的插入sql在mysql处理,由mysql的自增主键功能补全自增值.

- mycat 雪花算法生成

在 `conf/sequences` 路径下配置, `{数据库名字}_{表名字}.sequence.json` :
```
{
	"clazz":"io.mycat.plug.sequence.SequenceMySQLGenerator",
	"name":"db1_travelrecord"
}
```

也可以通过注解方式配置:
```
/*+ mycat:setSequence{"name":"数据库名字_表名字","time":true} */;
```

## MyCat SQL 兼容性

使用 mycat 我们就会在 sql 的编写上受到一些限制，因为 mycat 可能不支持，比如 sql 语法 insert ... select , 或者是某个 sql 中的函数. 详细信息参考: [MyCat SQL 兼容性](https://www.yuque.com/ccazhw/ml3nkf/442fb5725c71e519d5aa984ec96f9108)

## MyCat 监控

支持
- zabbix监控
- Prometheus & Grafana监控
- Mycat UI 监控

## 数据水平拆分

单表数据量太大的时查询就变得非常的缓慢，一般如果我们预期某个表在未来的一到两年时间单表数据会达到500万，甚至更大那我们就需要对这样的表进行水平拆分了。

#### 逻辑库，逻辑表

在 mycat 中的库和表都是逻辑的，这些逻辑的库和表会对应多个物理的库和表，mycat 就是为逻辑库表和物理库表之间建立起映射关系。

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/1a036d5ab6864706a825e71dc97bf59f.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


下图表示了这种映射关系，sharding-table 是逻辑表，对应了两个物理中的表.


![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/80baefa1e35c4d5dac252029b4153c18.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


mycat 支持分库和分表，对于 mycat 来说物理库表被抽象为 `partion` (分区)，具体是分库还是分表由分片规则决定.

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/9a8aaed41d7346539ab89bc91f4236e0.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


分库同时分表

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/3326d0789f2249bea65fd508987a993b.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


#### 分片算法

- HASH型分片算法
> HASH型分片算法默认要求集群名字以c为前缀,数字为后缀,c0就是分片表第一个节点,c1就是第二个节点.该命名规则允许用户手动改变.

| 分片算法 | 描述 | 分库 | 分表 | 数值类型 |
| :----: | :----: | :----: | :----: | :----: |
| MOD_HASH | 取模哈希 | 是 | 是 | 数值，字符串 |
| UNI_HASH | 取模哈希 | 是 | 是 | 数值，字符串 |
| RIGHT_SHIFT | 右移哈希 | 是 | 是 | 数值 |
| RANGE_HASH | 两字段其一取模 | 是 | 是 | 数值，字符串 |
| YYYYMM | 按年月哈希 | 是 | 是 | DATE，DATETIME |
| YYYYDD | 按年日哈希 | 是 | 是 | DATE，DATETIME |
| YYYYWEEK | 按年周哈希 | 是 | 是 | DATE，DATETIME |
| ~~HASH~~ | ~~取模哈希~~ | ~~是~~ | ~~是~~ | ~~数值，字符串，如果不是，则转换成字符串~~ |
| MM | 按月哈希 | 否 | 是 | DATE，DATETIME |
| DD | 按日期哈希 | 否 | 是 | DATE，DATETIME |
| MMDD | 按月日哈希 | 是 | 是 | DATE，DATETIME |
| WEEK | 按周哈希 | 否 | 是 | DATE，DATETIME |
| STR_HASH | 字符串哈希 | 是 | 是 | 字符串 |

**所有分片算法都可以用于分表，但是涉及单独按周，月的HASH算法不能用于分库**

#### 验证数据分表

##### 创建物理表
```
CREATE DATABASE `mycat_test` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
use mycat_test;

CREATE TABLE employee_0
(
    emp_id         bigint(20)  NOT NULL,
    emp_name       varchar(50) NOT NULL,
    emp_department varchar(20) NOT NULL,
    emp_salary     decimal(10, 2),
    PRIMARY KEY (emp_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE employee_1
(
    emp_id         bigint(20)  NOT NULL,
    emp_name       varchar(50) NOT NULL,
    emp_department varchar(20) NOT NULL,
    emp_salary     decimal(10, 2),
    PRIMARY KEY (emp_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE department_0
(
    dep_id     bigint(20)  NOT NULL,
    dep_name   varchar(50) NOT NULL,
    dep_region varchar(20) NOT NULL,
    PRIMARY KEY (dep_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE department_1
(
    dep_id     bigint(20)  NOT NULL,
    dep_name   varchar(50) NOT NULL,
    dep_region varchar(20) NOT NULL,
    PRIMARY KEY (dep_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE region_0
(
    reg_id   bigint(20)  NOT NULL,
    reg_name varchar(50) NOT NULL,
    PRIMARY KEY (reg_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE region_1
(
    reg_id   bigint(20)  NOT NULL,
    reg_name varchar(50) NOT NULL,
    PRIMARY KEY (reg_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```


##### 配置数据源
`mycat_test.datasource.json`
```
{
	"dbType":"mysql",
	"idleTimeout":60000,
	"initSqls":[],
	"initSqlsGetConnection":true,
	"instanceType":"READ_WRITE",
	"maxCon":1000,
	"maxConnectTimeout":3000,
	"maxRetryCount":5,
	"minCon":1,
	"name":"mycat_testDS",
	"password":"htest123@longpean",
	"type":"JDBC",
	"url":"jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/?useUnicode=true&useSSL=false",
	"user":"htest",
	"weight":0
}
```

##### 创建逻辑库
```
/*+ mycat:createSchema{
	"customTables":{},
	"globalTables":{},
	"normalTables":{},
	"schemaName":"mycat_test",
	"shardingTables":{},
	"targetName":"mycat_testDS"
} */;
```

##### 配置分表规则

为 employee 表创建分片规则
```
/*+ mycat:createTable{
   "schemaName":"mycat_test",
   "shardingTable":{

"createTableSQL":"CREATE TABLE employee (emp_id bigint(20) NOT NULL, emp_name varchar(50) NOT NULL, emp_department varchar(20) NOT NULL, emp_salary decimal(10, 2), PRIMARY KEY (emp_id)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4",

"function":{
               "properties":{
                   "dbNum":1,
                   "mappingFormat":"mycat_testDS/mycat_test/employee_${tableIndex}",
                   "tableNum":2,
                   "tableMethod":"mod_hash(emp_id)",
                   "storeNum":1,
                   "dbMethod":"mod_hash(emp_id)"
               }
           }
   },
   "tableName":"employee"
} */;
```

为 employee 表创建分片规则
```
/*+ mycat:createTable{
   "schemaName":"mycat_test",
   "shardingTable":{

"createTableSQL":"CREATE TABLE department (dep_id bigint(20) NOT NULL, dep_name varchar(50) NOT NULL, dep_region varchar(20) NOT NULL, PRIMARY KEY (dep_id)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4",

"function":{
               "properties":{
                   "dbNum":1,
                   "mappingFormat":"mycat_testDS/mycat_test/department_${tableIndex}",
                   "tableNum":2,
                   "tableMethod":"mod_hash(dep_id)",
                   "storeNum":1,
                   "dbMethod":"mod_hash(dep_id)"
               }
           }
   },
   "tableName":"department"
} */;
```

为 region 表创建分片规则
```
/*+ mycat:createTable{
   "schemaName":"mycat_test",
   "shardingTable":{

"createTableSQL":"CREATE TABLE region (reg_id bigint(20) NOT NULL, reg_name varchar(50) NOT NULL, PRIMARY KEY (reg_id)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4",

"function":{
               "properties":{
                   "dbNum":1,
                   "mappingFormat":"mycat_testDS/mycat_test/region_${tableIndex}",
                   "tableNum":2,
                   "tableMethod":"mod_hash(reg_id)",
                   "storeNum":1,
                   "dbMethod":"mod_hash(reg_id)"
               }
           }
   },
   "tableName":"region"
} */;
```

`conf\schemas\mycat_test.schema.json` 配置文件这时是这样：

```
{
	"customTables":{},
	"globalTables":{},
	"normalProcedures":{},
	"normalTables":{},
	"schemaName":"mycat_test",
	"shardingTables":{
		"employee":{
			"createTableSQL":"CREATE TABLE employee (emp_id bigint(20) NOT NULL, emp_name varchar(50) NOT NULL, emp_department varchar(20) NOT NULL, emp_salary decimal(10, 2), PRIMARY KEY (emp_id)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4",
			"function":{
				"properties":{
					"dbNum":1,
					"mappingFormat":"mycat_testDS/mycat_test/employee_${tableIndex}",
					"tableNum":2,
					"tableMethod":"mod_hash(emp_id)",
					"storeNum":1,
					"dbMethod":"mod_hash(emp_id)"
				},
				"ranges":{}
			},
			"partition":{
				
			},
			"shardingIndexTables":{}
		},
		"department":{
			"createTableSQL":"CREATE TABLE department (dep_id bigint(20) NOT NULL, dep_name varchar(50) NOT NULL, dep_region varchar(20) NOT NULL, PRIMARY KEY (dep_id)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4",
			"function":{
				"properties":{
					"dbNum":1,
					"mappingFormat":"mycat_testDS/mycat_test/department_${tableIndex}",
					"tableNum":2,
					"tableMethod":"mod_hash(dep_id)",
					"storeNum":1,
					"dbMethod":"mod_hash(dep_id)"
				},
				"ranges":{}
			},
			"partition":{
				
			},
			"shardingIndexTables":{}
		},
		"region":{
			"createTableSQL":"CREATE TABLE region (reg_id bigint(20) NOT NULL, reg_name varchar(50) NOT NULL, PRIMARY KEY (reg_id)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4",
			"function":{
				"properties":{
					"dbNum":1,
					"mappingFormat":"mycat_testDS/mycat_test/region_${tableIndex}",
					"tableNum":2,
					"tableMethod":"mod_hash(reg_id)",
					"storeNum":1,
					"dbMethod":"mod_hash(reg_id)"
				},
				"ranges":{}
			},
			"partition":{
				
			},
			"shardingIndexTables":{}
		}
	},
	"targetName":"mycat_testDS",
	"views":{}
}
```

##### 测试数据写入与查询

> 在 mycat 连接端执行.

```
INSERT INTO employee (emp_id, emp_name, emp_department, emp_salary) VALUES (1, 'John Smith', 'Finance', 5000.00);
INSERT INTO employee (emp_id, emp_name, emp_department, emp_salary) VALUES (2, 'Mary Johnson', 'Human Resources', 6000.00);
INSERT INTO employee (emp_id, emp_name, emp_department, emp_salary) VALUES (3, 'Robert Brown', 'Marketing', 7000.00);
INSERT INTO employee (emp_id, emp_name, emp_department, emp_salary) VALUES (4, 'Karen Davis', 'Sales', 8000.00);
```
执行查询验证：
```
select * from employee ;
```
![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/ae946606755e464ea65f20c0e1842678.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


在物理库上执行查询验证：
```
select * from employee_0;
```
![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/f6020c568311469bbc79c09185bf34e7.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


```
select * from employee_1;
```
![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/b800e680a23b4e2383bb427532b0a44f.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


写入一批数据测试关联查询：
```
INSERT INTO employee (emp_id, emp_name, emp_department, emp_salary)
VALUES (1662004435575422977, 'John Smith', 'Sales', 5000.00),
       (1662004435575422978, 'Jane Doe', 'Marketing', 6000.00),
       (1662004435575422979, 'Tom Wilson', 'Operations', 7000.00),
       (1662004435575422980, 'Bob Johnson', 'Operations', 5500.00),
       (1662004435575422981, 'Mike Lee', 'Marketing', 6500.00);

INSERT INTO department (dep_id, dep_name, dep_region)
VALUES (1662004435575422982, 'Sales', 'North America'),
       (1662004435575422983, 'Marketing', 'Europe'),
       (1662004435575422987, 'Operations', 'Asia');

INSERT INTO region (reg_id, reg_name)
VALUES (1662004435575422991, 'North America'),
       (1662004435575422994, 'Europe'),
       (1662004435575422996, 'Asia');
```

执行 join 查询语句：
```
SELECT e.emp_name, e.emp_salary, d.dep_name, r.reg_name
FROM employee e
JOIN department d ON e.emp_department = d.dep_name
JOIN region r ON d.dep_region = r.reg_name;
```
![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/7edf06b74ed64a98b5565fdca5c1e35b.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


## 数据迁移

当我们决定要对数据进行水平拆分后，就涉及到将原有物理库中的数据迁移到新的物理库结构上，例如 `employee` 表目前是单库单表，为了对 `employee` 表数据进行水平拆分，我们建立了两个数据库，每个数据库中分别有 10 个 `employee_n` 表（n 从 0 到 9）.
这是后我们就需要根据我们规划的分片策略将原 `employee` 表中的数据迁移到这两个数据库中多个 `employee_n` 表中。

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/d4791af6a9e640e68542c97d2ad4d71f.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)
