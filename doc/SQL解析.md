# SQL 解析

`SQLX` 提供了强大的 SQL 解析功能，支持多种 SQL 解析器，以满足不同的应用需求。通过 SQL 解析，SQLX 可以智能地将 SQL 语句路由到合适的数据源，从而实现读写分离、负载均衡等功能。

## 配置 SQL 解析器

在 `application.yml` 或 `application.properties` 中，您可以通过配置 `sqlx.config.sql-parser-class` 来指定使用的 SQL 解析器类。如果没有指定，SQLX 将默认使用 `com.github.sqlx.sql.parser.JSqlParser`。

```yaml
sqlx:
  config:
    enabled: true
    sql-parser-class: com.github.sqlx.sql.parser.JSqlParser
```

没有指定时默认使用 `com.github.sqlx.sql.parser.JSqlParser`.

## 自定义 SQL 解析器

您也可以实现自定义的 SQL 解析器。自定义解析器需要实现 `com.github.sqlx.sql.parser.SqlParser` 接口。

### 自定义SQL解析器示例

```java
package com.example.sqlparser;

import com.github.sqlx.sql.parser.SqlParser;

public class CustomSqlParser implements SqlParser {
    @Override
    public SqlAttribute parse(String sql) {
        // 自定义解析逻辑
        return new DefaultSqlAttribute();
    }
}
```

当您自定义 SQL 解析器时请确保它正确的实现, sqlx 的诸多功能依赖于 SQL 解析器。

## SQL 解析失败行为

当解析 SQL 失败时，您可以通过 `sqlx.config.sql-parsing-fail-behavior` 配置来指定失败时的行为。可选的行为包括：

- **IGNORE**: 忽略异常，并将 SQL 类型设置为 `OTHER` 类型，把 SQL 视作一条写语句。
- **WARNING**: 输出警告日志，并将 SQL 类型设置为 `OTHER` 类型，把 SQL 视作一条写语句。
- **FAILING**: 抛出 `com.github.sqlx.exception.SqlParseException` 异常，并终止程序运行。

```yaml
sqlx:
  config:
    sql-parsing-fail-behavior: warning
```
