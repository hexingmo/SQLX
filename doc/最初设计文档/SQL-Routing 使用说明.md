
# SQL-Routing 使用说明

## 设计目标

> 代码仓库地址： https://e.gitee.com/longpean/repos/longpean/sql-routing/sources

1. 尽可能低的减少侵入,实现低成本的集成和剥离

完全的零侵入几乎无法实现,为了满足一些特定场景的需求例如强制路由到某个指定的数据节点,用户需要手动对路由过程进行控制.虽然我们无法完全避免侵入性，但是也应该尽量降低侵入性,或限定侵入的范围,最好避免让这种侵入扩散到代码中的任何地方.

2. 简单易用

3. 方便扩展

## 实现方式

`SQL-Routing` 的实现方式核心是 `SQL` 和路由，路由的依据就是所执行的 `SQL` 语句。例如 `SELECT` 语句会被路由到从库, `INSERT` 语句会被路由到主库.

### 示意图

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/411a4b976457488a9a69b1aaccff7c44.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


### SQL 解析示意图

```
SELECT customers.customer_name, orders.order_id, order_details.product_name, order_details.unit_price, order_details.quantity, (order_details.unit_price * order_details.quantity) AS total_price
FROM customers
INNER JOIN orders
ON customers.customer_id = orders.customer_id
INNER JOIN order_details
ON orders.order_id = order_details.order_id
WHERE customers.country = 'USA'
AND orders.order_date BETWEEN '2022-01-01' AND '2022-12-31'
AND order_details.quantity > 10
ORDER BY customers.customer_name ASC, orders.order_date DESC;
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/255c7364e15a49a7a03c426d41572feb.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


### 内置 RoutingRule

提供了一个内置的 `EmbeddedRoutingGroup` , 其中包含多个 `RoutingRule` :

- `TxRoutingRule` 事务路由规则
- `NullSqlAttributeRoutingRule` 无法解析的 SQL 会直接路由到主库
- `RoutingNameSqlHintRoutingRule` 注解 SQL 指定了数据目标节点名称
- `RoutingTypeSqlHintRoutingRule` 注解 SQL 指定了数据目标节点类型
- `ForceTargetRoutingRule` 通过 `RoutingContext` 强制指定了数据目标节点名称
- `DatabaseRoutingRule` 基于数据库的路由规则
- `TableRoutingRule` 基于表的路由规则
- `ReadWriteSplittingRoutingRule` 基于SQL类型的读写分离路由规则

## 如何使用

#### 安装

maven 安装：
```
<dependency>
	<groupId>com.lp</groupId>
	<artifactId>SQL-Routing</artifactId>
	<version>1.0.0-Beta</version>
</dependency>
```

#### 配置

`sql.routing.dataSources` 配置项下可以配置多个数据源
- `name` 数据源名称必须唯一
- `type` 数据源类型，分为 READ (只读) ， WRITE (只写) ， READ_WRITE （读写）
- `weight` 权重，权重按数字从小到大，数字越大权重越高
- `dataSourceClass` 使用的数据源全类名
- `driverClassName` 使用的数据库驱动全类名
- `props` 是使用的数据源的具体参数配置项

`sql.routing.masters` 配置项下可以配置多个主节点数据源的名称
`sql.routing.replicas` 配置项下可以配置多个从节点数据源的名称

`sql.routing.readLoadBalanceType` 配置项是读节点负载均衡类型
- `WEIGHT_RANDOM_BALANCE_ALL` 所有可读节点加权随机
- `WEIGHT_RANDOM_BALANCE_ONLY_READ` 只读节点加权随机
- `WEIGHT_RANDOM_BALANCE_READ_WRITE` 只读节点和读写节点加权随机
- `WEIGHT_ROUND_ROBIN_BALANCE_ALL` 所有可读节点加权轮询
- `WEIGHT_ROUND_ROBIN_BALANCE_ONLY_READ` 只读节点加权轮询
- `WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE` 只读节点和读写节点加权轮询

`SqlType` SQL 类型：

- `INSERT`
- `UPDATE`
- `DELETE`
- `SELECT`
- `OTHER` (除了以上类型外的其他所有 SQL, OTHER 类型 SQL 直接路由主库)


```
############################# SQL Routing Configuration #############################

sql.routing.dataSources[0].name=master
sql.routing.dataSources[0].type=READ_WRITE
sql.routing.dataSources[0].weight=999
sql.routing.dataSources[0].dataSourceClass=com.alibaba.druid.pool.DruidDataSource
sql.routing.dataSources[0].driverClassName=com.mysql.cj.jdbc.Driver
sql.routing.dataSources[0].props.jdbcUrl=jdbc:mysql://dev-lp888.mysql.rds.aliyuncs.com:1888/oms_dev?useUnicode=true&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=GMT%2B8
sql.routing.dataSources[0].props.username=dev
sql.routing.dataSources[0].props.password=Lp123@Rds456
sql.routing.dataSources[0].props.initialSize=30
sql.routing.dataSources[0].props.minIdle=15
sql.routing.dataSources[0].props.maxActive=100
sql.routing.dataSources[0].props.maxWait=60000
sql.routing.dataSources[0].props.timeBetweenEvictionRunsMillis=60000
sql.routing.dataSources[0].props.minEvictableIdleTimeMillis=300000
sql.routing.dataSources[0].props.validationQuery=SELECT 1 FROM DUAL
sql.routing.dataSources[0].props.testWhileIdle=true
sql.routing.dataSources[0].props.testOnBorrow=false
sql.routing.dataSources[0].props.testOnReturn=false
sql.routing.dataSources[0].props.poolPreparedStatements=true
sql.routing.dataSources[0].props.maxPoolPreparedStatementPerConnectionSize=20
sql.routing.dataSources[0].props.connectionProperties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=3000

sql.routing.dataSources[1].name=slave_0
sql.routing.dataSources[1].type=READ
sql.routing.dataSources[1].weight=777
sql.routing.dataSources[1].dataSourceClass=com.alibaba.druid.pool.DruidDataSource
sql.routing.dataSources[1].driverClassName=com.mysql.cj.jdbc.Driver
sql.routing.dataSources[1].props.jdbcUrl=jdbc:mysql://rr-bp1h657r886c78464vo.mysql.rds.aliyuncs.com:3316/oms_dev?useUnicode=true&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=GMT%2B8
sql.routing.dataSources[1].props.username=dev
sql.routing.dataSources[1].props.password=Lp123@Rds456
sql.routing.dataSources[1].props.initialSize=30
sql.routing.dataSources[1].props.minIdle=15
sql.routing.dataSources[1].props.maxActive=100
sql.routing.dataSources[1].props.maxWait=60000
sql.routing.dataSources[1].props.timeBetweenEvictionRunsMillis=60000
sql.routing.dataSources[1].props.minEvictableIdleTimeMillis=300000
sql.routing.dataSources[1].props.validationQuery=SELECT 1 FROM DUAL
sql.routing.dataSources[1].props.testWhileIdle=true
sql.routing.dataSources[1].props.testOnBorrow=false
sql.routing.dataSources[1].props.testOnReturn=false
sql.routing.dataSources[1].props.poolPreparedStatements=true
sql.routing.dataSources[1].props.maxPoolPreparedStatementPerConnectionSize=20
sql.routing.dataSources[1].props.connectionProperties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=3000
sql.routing.dataSources[1].props.defaultReadOnly=true


sql.routing.masters[0]=master
sql.routing.replicas[0]=slave_0
sql.routing.readLoadBalanceType=WEIGHT_RANDOM_BALANCE_ONLY_READ

# database routing rule
sql.routing.rules.databases[0].name=dev
sql.routing.rules.databases[0].nodes.slave_0.sqlTypes[0]=SELECT


# table routing rule
sql.routing.rules.tables.t_face_sheet_rewrite_log.master.allowAllSqlTypes=true
sql.routing.rules.tables.t_face_sheet_rewrite_log.master.sqlTypes[0]=SELECT
sql.routing.rules.tables.t_face_sheet_rewrite_log.master.sqlTypes[1]=UPDATE
sql.routing.rules.tables.t_face_sheet_rewrite_log.master.sqlTypes[2]=DELETE
sql.routing.rules.tables.t_face_sheet_rewrite_log.master.sqlTypes[3]=OTHER

sql.routing.rules.tables.t_face_sheet_rewrite_log.slave_0.sqlTypes[0]=SELECT
```

#### 启动

在 spring boot 主类上使用 `@EnableSQLRouting` 即可.

```
@SpringBootApplication
@EnableAsync
@MapperScan("com.lp.*.dao")
@EnableSQLRouting
public class OmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmsApplication.class, args);
    }

}
```

#### 编写 SQL

##### 数据库的路由规则

通常情况下编写 SQL 不受任何影响, 如果要使用基于数据库的路由规则就需要在编写 SQL 的时候在表名称前指定库名称，否则 SQL 解析无法知道库名称.例如：

```
select * from dev.t_py_current_stock limit 100
```

##### 注解 SQL

> 将侵入性控制在 SQL 层面.

路由到指定类型的数据节点注解 SQL
```
/*!routingType=WRITE;*/ select * from t_oms_order_info where id = ?
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/a99e9fb1c3114cc789c6086fb26a349e.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


路由到指定名称的数据节点注解 SQL
```
/*!routingTargetName=slave_0;*/ select * from t_oms_order_info where id = ?
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/359800b50f984cefabc7221cbf534deb.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


为了实现平滑迁移，注解 SQL 的实现思路是,提供多种中间件的 注解 SQL 语法支持。 假设我们后期可能会选择使用 `Mycat` 数据中间件,那么现在我们编写 SQL 的时候就可以使用 `Mycat` 的注解 SQL 语法.这样就可以做到在剥离 `SQL-Routing` 的时候我们几乎不需要对代码做任何的改动. (目前这块还没实现,数据中间件太多了,只实现了 `SQL-Routing` 内部的一套注解 SQL 语法)。

## 注解路由

使用 `@SqlRouting` 注解注释在方法上来控制整个方法内 sql 执行的路由。

> 如果方法是事务方法时 `@SqlRouting` 无效，依然会执行事务路由规则。

```
    @SqlRouting("write_0")
    @Transactional(rollbackFor = Throwable.class)
    public String fun0() {

        String sql1 = "select * from employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql1 , new Object[] {1} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(1L);
        assertThat(result).extractingByKey("NAME").isEqualTo("John Doe");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);

        String sql2 = "insert into employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql2, ps -> {
            ps.setLong(1 , 5L);
            ps.setString(2 , "Peng He");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);
        return "ok";
    }
```

![](https://devx-blog-images.oss-cn-beijing.aliyuncs.com/images/20250108/af41e109e06c407fbe35fc034ea19eae.png?x-oss-process=image/auto-orient,1/interlace,1/quality,q_50/format,jpg)


## 关于扩展

可以自定义 `SqlParser` , `RoutingGroup` 实现, 通过 spring bean 的方式提供即可.用户自定义的 `RoutingGroup` 会排在 `EmbeddedRoutingGroup` 之前，务必小心自定义的 `RoutingGroup` 会使得现有功能失效例如事务路由规则.

## 第三方库兼容

#### mybatis-plus

在和 `mybatis-plus` 集成使用时建议加上 `mybatis-plus` 数据库类型配置项:

```
mybatis-plus.global-config.db-config.db-type=mysql
```

#### pagehelper

在和 `pagehelper` 集成使用时需要指定配置方言。

```
pagehelper.dialect=com.lp.sql.routing.integration.pagehelper.MySqlDialect
```

> SQL-Routing 是在 SQL 执行时才会去确定使用那个具体的数据源，在没有明确的 SQL 时无法提供一个具体的物理 `Connection` 也就无法通过 `DatabaseMetaData` 获取到数据库的 `URL`, 而 `pagehelper` 和 `mybatis-plus` 都通过获取数据库 `URL` 的方式判断数据库类型，所以无法满足.

## 局限性

#### 数据库路由规则,表路由规则

数据库路由规则和表路由规则不适合处理复杂的 SQL , 因为 SQL 中可以访问多个表.

比如这个 SQL 中访问了 3 个表 `customers` , `orders` , `order_details` .库和表的路由规则目前是只要当前 SQL 中访问了这个表就认为匹配到了. 并没有根据表出现的位置进行更精确的匹配（比如限定表是在 join 位置还是在子查询中，是在 from 子句中还是在 join 子句中等等比较）,比较麻烦虽然这一点也可以做到,但是为了避免过度设计就先没做。

```
SELECT customers.customer_name, orders.order_id, order_details.product_name, order_details.unit_price, order_details.quantity, (order_details.unit_price * order_details.quantity) AS total_price
FROM customers
INNER JOIN orders
ON customers.customer_id = orders.customer_id
INNER JOIN order_details
ON orders.order_id = order_details.order_id
WHERE customers.country = 'USA'
AND orders.order_date BETWEEN '2022-01-01' AND '2022-12-31'
AND order_details.quantity > 10
ORDER BY customers.customer_name ASC, orders.order_date DESC;
```

> 也可以通过数据库路由规则,表路由规则的方式来实现无侵入的强制读取主库效果。但是这样做粒度会粗放，意味着访问该表符合配置规则的 SQL 都会路由到主库执行.这种方式不如注解 SQL 的方式灵活，但优势就是无侵入性。

#### RoutingContext

`RoutingContext` 在当前线程内传播, 在事务场景下 `RoutingContext` 会随着事务的开始而创建，事务完成（无论提交或回滚后）清除。在非事务场景下 `RoutingContext` 的作用范围是 `Connection` 或 `Statement` 的打开和关闭, `Connection` 或 `Statement` 关闭时都将清除 `RoutingContext`。

`RoutingContext` 提供了方法 `forceWrite` 表示在该方法调用后的第一次 SQL 执行会强制路由写库，`forceRead` 表示在该方法调用后的第一次 SQL 执行会强制路由读库，`force(String ... dataSourceNames)` 可以指定数据源名称,表示在该方法调用后的第一次 SQL 执行会强制路由到所指定的数据源名称，注意这三个方法之间是互斥的。注意这些机制不适用于事务场景，在事务场景下始终使用的都是同一个 `Connection` , 在由 Spring 管理的事务场景下, 在开始执行事务方法前就已经确定了之后要执行 SQL 语句所使用的 `Connection` ， 该 `Connection` 将在当前线程中传播,无法跨线程传播.随着事务的提交或回滚该 `Connection` 将被释放。

虽然 `RoutingContext` 提供了一些 `force` 方法，但是应该尽量避免使用 `RoutingContext`. 在代码中使用了 `RoutingContext` 会引入侵入，尤其是在业务代码中,应该尽可能的避免侵入,或者限定侵入的范围,比如在一些强制路由的场景使用注解 SQL 的方式，将侵入限定在 SQL 语句层面.而使用了 `RoutingContext` 后将变得不可控制。
