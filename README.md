# sqlx-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-green.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

## Introduction
See the [中文文档](./README-zh.md) for Chinese readme.

`sqlx-spring-boot-starter` is a lightweight, non-intrusive, pluggable, monitorable, dynamically manageable, ORM technology-independent multi-data source integration framework. It is mainly used for intelligently routing SQL to the appropriate data source, supporting read-write separation, cluster mode, and non-cluster mode, thereby improving application performance.

## Features
- **Lightweight**: Easy to use by simply adding a jar and configuration
- **Non-intrusive**: No need to introduce any SQLX API dependencies in the code, all features can be configured
- **Pluggable**: Decide whether SQLX is effective through configuration without any code changes
- **Intelligent Routing**: SQL parsing-based intelligent routing to route SQL statements to the appropriate data source
- **Multi-Data Source Support**: Supports cluster mode, non-cluster mode, and independent database mixed mode
- **Auto Failover**: Monitors data source status and automatically switches to other nodes when a node fails
- **Transaction Support**: Automatically detects transactions, ensuring SQL within a transaction is routed to the same data source
- **Method Nesting Support**: Supports multi-layer method nesting and controls whether SQL routing propagates downward
- **Annotation SQL Support**: Control SQL statement routing to a specific data source through annotation
- **Automatic Failover**: Monitors data source status and automatically switches to other nodes when a node fails
- **ORM Technology Independent**: Supports any ORM framework, such as Hibernate, MyBatis, JPA, etc.
- **Monitoring Features**: Monitors abnormal SQL, slow SQL, and large transactions to provide optimization basis
- **Dynamic Management**: Supports dynamic addition, deletion, and modification of data sources and cluster configurations through configuration
- **Event Listening**: Provides JDBC event listening mechanism for users to customize extended listeners to handle different JDBC events

## Quick Start

### Installation
Add SQLX to your Maven project:

```xml
<dependency>
    <groupId>com.github.sqlx</groupId>
    <artifactId>sqlx-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### Configuration
Add SQLX configuration in `application.yml` or `application.properties`:

```yaml
sqlx:
  enabled: true # Whether to enable SQLX
  sql-parsing-fail-behavior: warning # Behavior when SQL parsing fails, supports WARNING (warning), FAILING (error), IGNORE (ignore)
  metrics:
    enabled: true # Whether to enable monitoring functionality
    username: admin # Monitoring username
    password: adminpw # Monitoring password
    collect-scope: SLOW # Monitoring collection scope, supports SLOW (only collects slow SQL and slow transactions), ALL (collects all)
    enable-routing-metrics: true
    enable-sql-metrics: true
    enable-transaction-metrics: true
    slow-sql-millis: 300 # Slow SQL threshold, unit in milliseconds
    slow-transaction-millis: 3000 # Slow transaction threshold, unit in milliseconds
    #      collect-mode: ASYNC # Collection mode, supports SYNC (synchronous), ASYNC (asynchronous)
    #      collect-core-pool-size: 10
    #      collect-max-pool-size: 30
    #      collect-keep-alive-millis: 10000
    #      collect-queue-capacity: 3000
    file-directory: /usr/local/sqlx-metrics # Directory for saving collected data files
    data-retention-duration: 2h # Data retention duration, supports s, m, h, d, w, M, y
  data-sources: # Data source configuration
    - name: write_0 # Data source name must be unique
      weight: 99 # Data source weight, used for load balancing
      defaulted: true
      data-source-class: com.zaxxer.hikari.HikariDataSource
      init-method: init # Initialization method
      destroy-method: close # Destruction method
      init-sql-script: classpath:init.sql # Initialization SQL script
      props: # Data source parameter configuration, parameter names should match the fields of the data source
        driverClassName: org.h2.Driver
        jdbcUrl: jdbc:h2:mem:~/test1;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;
        username: sa
        password: pwd
        minIdle: 5
        maxPoolSize: 30
        connectionTimeout: 30000
        isAutoCommit: false
        isReadOnly: false

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
        isReadOnly: true

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
  clusters: # Cluster configuration
    - name: cluster_0 # Cluster name must be unique
      defaulted: true # Specifies whether it is the default cluster when there are multiple clusters
      writable-nodes:
        - write_0
      readable-nodes:
        - read_0
        - read_1
    - name: cluster_1
      writable-nodes:
        - write_0
      readable-nodes:
        - read_1

  pointcuts: # SQL routing interception configuration, SQL within this method will be routed to the specified cluster and nodes
    - expression: "execution(* com.foo.ServiceA.fun1())" # Interception expression, supports Spring AOP interception expressions
      cluster: cluster_0 # Cluster name
      nodes: # Node names
        - write_0
        - read_0
      propagation: true # Whether to accept propagation from upper-level methods
    - expression: "execution(* com.foo.ServiceA.fun2())"
      cluster: cluster_0
      nodes:
        - write_0
        - read_0
        - read_1
      propagation: true
```

## Contribution Guide
We welcome any form of contribution, including but not limited to:

1. Submitting issues and suggestions
2. Improving documentation
3. Fixing bugs, writing unit tests, adding new features
4. Sharing usage experiences

## License
SQLX is licensed under the [Apache License 2.0](LICENSE).

## Contact
For questions or suggestions, please submit Issues or Pull Requests.  
You can also email: [dawn.hexingmo@gmail.com](mailto:dawn.hexingmo@gmail.com)   


