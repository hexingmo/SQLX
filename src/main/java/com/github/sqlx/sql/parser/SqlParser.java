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

package com.github.sqlx.sql.parser;

import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.DefaultSqlAttribute;

/**
 * SQL is an important basis for determining the target data source in the routing process of data sources.
 * The purpose of the SQL parser is to parse the SQL and obtain the judgment basis information for selecting
 * the final data source in the subsequent routing process.
 *
 * @author He Xing Mo
 * @since 1.0
 *
 * @see JSqlParser
 * @see DefaultSqlAttribute
 */
public interface SqlParser {

    /**
     * Parse a given SQL statement
     * @param sql sql statement
     * @return {@link SqlAttribute}
     */
    SqlAttribute parse(String sql);
}
