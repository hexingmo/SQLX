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

package com.github.sqlx.jdbc.datasource;

import javax.sql.DataSource;

/**
 * The data source that provides the ability to route multiple data sources,
 * each with a unique name, supports routing based on data source name
 * and SQL statements.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public interface SqlXDataSource extends DataSource {

    /**
     * Route based on SQL, queries are routed to read data sources
     * and write statements are routed to write data sources.
     * In transaction scenarios, all SQL executions are routed
     * to write data sources;
     * Other JDBC API calls unrelated to SQL execution will be routed
     * to write data sources for execution.
     *
     * @param sql sql
     * @return RoutedDataSource
     */
    RoutedDataSource getDataSource(String sql);

    /**
     * Retrieves a data source that is suitable for obtaining database metadata.
     * This method is typically used when metadata information is required,
     * such as during the initialization phase or when setting up connections.
     *
     * @return a RoutedDataSource that can be used to access database metadata.
     */
    RoutedDataSource getDataSourceForDatabaseMetaData();

}
