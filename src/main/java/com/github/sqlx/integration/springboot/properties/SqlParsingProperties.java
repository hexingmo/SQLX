package com.github.sqlx.integration.springboot.properties;

import com.github.sqlx.config.SqlParsingFailBehavior;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sql parsing configuration properties.
 *
 * @author jing yun
 * @since 1.0
 */
@ConfigurationProperties(prefix = "sqlx.sql-parsing")
@Data
public class SqlParsingProperties {

    /**
     * SQL parser class name.
     */
    private String sqlParserClass;

    /**
     * Behavior when SQL parsing fails.
     */
    private SqlParsingFailBehavior sqlParsingFailBehavior = SqlParsingFailBehavior.WARNING;

}
