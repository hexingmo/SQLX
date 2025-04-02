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
package com.github.sqlx.config;

import com.github.sqlx.exception.SqlParseException;
import com.github.sqlx.sql.DefaultSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.SqlType;
import lombok.extern.slf4j.Slf4j;

/**
 * An enumeration that defines behaviors for handling SQL parsing failures.
 * This class provides three strategies to deal with situations where SQL parsing fails:
 * - IGNORE: Do nothing and treat the SQL as a write operation.
 * - WARNING: Log a warning message and treat the SQL as a write operation.
 * - FAILING: Throw a {@link SqlParseException} when parsing fails.
 * Each behavior implements an abstract method `action` which is called when a parsing error occurs.
 * The method receives the problematic SQL statement and the associated exception, then handles it according to the defined strategy.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public enum SqlParsingFailBehavior {

    /**
     * Do nothing when SQL parsing fails (Default behavior).
     * Treats the SQL as a write operation without any additional action.
     */
    IGNORE {
        @Override
        public SqlAttribute action(String sql, Exception ex) {
            return createOtherSqlTypeIsWriteSqlAttribute(sql);
        }
    } ,

    /**
     * Outputs a warning log when SQL parsing fails.
     * Logs the problematic SQL and the error message, then treats the SQL as a write operation.
     */
    WARNING {
        @Override
        public SqlAttribute action(String sql, Exception ex) {
            log.warn("sql parse error, sql: {}, error: {}", sql, ex.getMessage());
            return createOtherSqlTypeIsWriteSqlAttribute(sql);
        }
    },

    /**
     * Throws a {@link SqlParseException} when SQL parsing fails.
     * This behavior stops execution and propagates the exception to the caller.
     */
    FAILING {
        @Override
        public SqlAttribute action(String sql, Exception ex) {
            throw new SqlParseException(String.format("SQL Parse Error, SQL [%s]" , sql) , ex);
        }
    };

    /**
     * Abstract method that defines the action to take when SQL parsing fails.
     * Each enum constant must implement this method to define its specific behavior.
     *
     * @param sql the SQL statement that caused the parsing failure
     * @param ex the exception thrown during SQL parsing
     * @return a {@link SqlAttribute} object representing the fallback SQL attributes
     */
    public abstract SqlAttribute action(String sql, Exception ex);

    /**
     * Helper method to create a default SQL attribute object when parsing fails.
     * Sets the SQL type to "OTHER" and marks it as a write operation.
     *
     * @param sql the original SQL statement
     * @return a {@link DefaultSqlAttribute} object with fallback settings
     */
    private static SqlAttribute createOtherSqlTypeIsWriteSqlAttribute(String sql) {
        DefaultSqlAttribute dsa = new DefaultSqlAttribute();
        dsa.setSql(sql).setNativeSql(sql).setSqlType(SqlType.OTHER).setWrite(true).setRead(false);
        return dsa;
    }
}
