# sqlx-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-green.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 简介

`sqlx-spring-boot-starter` 是一个轻量级、零侵入、可插拔、可监控、支持动态管理、ORM 技术无关的多数据源集成框架。它主要用于智能路由 SQL 到合适的数据源，支持读写分离、集群模式和非集群模式，从而提高应用性能。

## 特性

- **轻量级**：只需引入 jar 包并增加配置即可使用
- **零侵入**：无需在代码中引入任何 SQLX API 的依赖，所有功能都可通过配置实现
- **可插拔**：通过配置决定 SQLX 是否生效，无需对代码做任何更改
- **智能路由**：基于 SQL 解析的智能路由功能，将 SQL 语句路由到合适的数据源
- **多数据源支持**：支持集群模式、非集群模式和独立数据库混合模式
- **自动故障切换**：监测数据源状态，某个节点故障时自动切换到其他节点
- **事务支持**：自动检测事务,事务内的 SQL 都会被路由到同一数据源执行
- **支持方法嵌套**: 支持多层方法嵌套,以及控制SQL路由是否向下传播
- **支持注解SQL**：通过注解 SQL 的方式来控制 SQL 语句路由到那个数据源执行
- **自动故障转移**：监测数据源状态,某个节点故障时自动切换到其他节点
- **ORM 技术无关**：支持任意 ORM 框架，如 Hibernate、MyBatis、JPA 等
- **监控功能**：对异常 SQL、慢 SQL、大事务进行监控，帮助用户提供优化依据
- **动态管理**：支持通过配置动态增删改数据源和集群配置
- **事件监听**：提供 JDBC 事件监听机制，方便用户自定义扩展监听器处理不同的 JDBC 事件

## 快速开始

### 安装

将 SQLX 添加到你的 Maven 项目中：

```xml
<dependency>
    <groupId>com.github.hexingmo</groupId>
    <artifactId>sqlx-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### 配置

```yaml
sqlx:
  enabled: true # 是否启用SQLX
  sql-parsing-fail-behavior: warning # SQL解析失败时的行为，支持 WARNING (警告)、FAILING (报错)、IGNORE (忽略)
  metrics:
    enabled: true # 是否启用监控功能
    username: admin # 监控用户名
    password: adminpw # 监控密码
    collect-scope: SLOW # 监控采集范围, 支持 SLOW (只采集慢SQL和慢事务)、ALL (采集所有)
    enable-routing-metrics: true
    enable-sql-metrics: true
    enable-transaction-metrics: true
    slow-sql-millis: 300 # 慢 SQL 阈值，单位为毫秒
    slow-transaction-millis: 3000 # 慢事务阈值，单位为毫秒
    #      collect-mode: ASYNC # 采集方式，支持 SYNC (同步)、ASYNC (异步)
    #      collect-core-pool-size: 10
    #      collect-max-pool-size: 30
    #      collect-keep-alive-millis: 10000
    #      collect-queue-capacity: 3000
    file-directory: /usr/local/sqlx-metrics # 保存采集数据的文件目录
    data-retention-duration: 2h # 数据保留时长，支持 s、m、h、d、w、M、y
  data-sources: # 数据源配置
    - name: write_0 # 数据源名称需唯一
      weight: 99 # 数据源权重，用于负载均衡
      defaulted: true
      data-source-class: com.zaxxer.hikari.HikariDataSource
      init-method: init # 初始化方法
      destroy-method: close # 销毁方法
      init-sql-script: classpath:init.sql # 初始化 SQL 脚本
      props: # 数据源参数配置,参数名称和所使用的数据源字段名保持一致即可
        driverClassName: org.h2.Driver
        jdbcUrl: jdbc:h2:mem:~/test1;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;
        username: sa
        password: pwd
        minIdle: 5
        maxPoolSize: 30
        connectionTimeout: 30000
        isAutoCommit: false
        isReadOnly: fals  
    - name: read_0
      weight: 6
      data-source-class: com.zaxxer.hikari.HikariDataSource
      init-method: init
      destroy-method: close
      init-sql-script: classpath:init.sql
      props:
        driverClassName: org.h2.Driver
        jdbcUrl: jdbc:h2:mem:~/test2;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;
        username: sa
        password: pwd
        minIdle: 10
        maxPoolSize: 30
        connectionTimeout: 40000
        isAutoCommit: false
        isReadOnly: tru  
    - name: read_1
      weight: 10
      data-source-class: com.zaxxer.hikari.HikariDataSource
      init-method: init
      destroy-method: close
      init-sql-script: classpath:init.sql
      props:
        driverClassName: org.h2.Driver
        jdbcUrl: jdbc:h2:mem:~/test3;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;
        username: sa
        password: pwd
        minIdle: 15
        maxPoolSize: 30
        connectionTimeout: 60000
        isAutoCommit: false
        isReadOnly: true
  clusters: # 集群配置
    - name: cluster_0 # 集群名称需唯一
      defaulted: true # 当存在多个集群时指定是否是默认集群
      writable-nodes:
        - write_0
      readable-nodes:
        - read_0
        - read_1
        - write_0
    - name: cluster_1
      writable-nodes:
        - write_0
      readable-nodes:
        - read_  
  pointcuts: # SQL 路由切入点配置,该方法内的 SQL 会被路由到指定的集群和节点
    - expression: "execution(* com.foo.ServiceA.fun1())" # 切入点表达式，支持 Spring AOP 的切入点表达式
      cluster: cluster_0 # 集群名称
      nodes: # 节点名称
        - write_0
        - read_0
      propagation: true # 是否接受上层方法传播
    - expression: "execution(* com.foo.ServiceA.fun2())"
      cluster: cluster_0
      nodes:
        - write_0
        - read_0
        - read_1
      propagation: true
```

## 贡献指南
1. 提交问题和建议
2. 改进文档
3. 修复 Bug,编写单元测试,添加新功能
4. 分享使用经验

## 许可证
SQLX 使用 [Apache License 2.0](LICENSE) 开源协议。

## 联系方式
如有问题或建议，请提交 Issues 或 Pull Requests。   
也可发送邮件到：[dawn.hexingmo@gmail.com](mailto:dawn.hexingmo@gmail.com)

