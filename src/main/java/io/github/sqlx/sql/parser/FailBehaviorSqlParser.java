/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.github.sqlx.sql.parser;

import io.github.sqlx.config.SqlParsingFailBehavior;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * A SQL parser that delegates parsing to another parser and handles parsing failures
 * according to a specified fail behavior.
 * <p>
 * This class wraps another {@link SqlParser} and applies a fail behavior strategy
 * when the delegate parser throws an exception. The fail behavior determines how
 * to handle the exception and what {@link SqlAttribute} to return.
 * </p>
 * <p>
 * This parser is useful in scenarios where you want to ensure that a parsing attempt
 * always results in a valid {@link SqlAttribute}, even if the original parsing fails.
 * </p>
 * 
 * @see SqlParser
 * @see SqlParsingFailBehavior
 * @see SqlAttribute
 * 
 * @since 1.0
 */
@Slf4j
public class FailBehaviorSqlParser implements SqlParser {

    private final SqlParser delegate;
    private final SqlParsingFailBehavior failBehavior;

    /**
     * Constructs a new FailBehaviorSqlParser with the specified delegate parser
     * and fail behavior.
     *
     * @param delegate the delegate parser to use for parsing SQL statements
     * @param failBehavior the fail behavior to apply when parsing fails
     */
    public FailBehaviorSqlParser(SqlParser delegate, SqlParsingFailBehavior failBehavior) {
        this.delegate = delegate;
        this.failBehavior = failBehavior;
    }

    /**
     * Parses the given SQL statement using the delegate parser. If the delegate
     * parser throws an exception, the fail behavior is applied to handle the
     * exception and determine the resulting {@link SqlAttribute}.
     *
     * @param sql the SQL statement to parse
     * @return the resulting {@link SqlAttribute} after parsing or applying the fail behavior
     */
    @Override
    public SqlAttribute parse(String sql) {
        sql = StringUtils.replaceEach(sql, new String[]{StringUtils.LF, StringUtils.CR}, new String[]{" ", " "});

        SqlAttribute sqlAttr;
        try {
            sqlAttr = delegate.parse(sql.trim());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing failed, applying fail behavior: {}", failBehavior, e);
            }
            sqlAttr = failBehavior.action(sql, e);
        }
        return sqlAttr;
    }
}
