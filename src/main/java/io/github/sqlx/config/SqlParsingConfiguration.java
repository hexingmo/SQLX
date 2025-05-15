package io.github.sqlx.config;

import io.github.sqlx.sql.parser.SqlParser;
import lombok.Data;

/**
 * @author jing yun
 * @since 1.0
 */
@Data
public class SqlParsingConfiguration {

    /**
     * Behavior when SQL parsing fails.
     */
    private SqlParsingFailBehavior sqlParsingFailBehavior = SqlParsingFailBehavior.WARNING;

    /**
     * sql parser
     */
    private SqlParser sqlParser;
}
