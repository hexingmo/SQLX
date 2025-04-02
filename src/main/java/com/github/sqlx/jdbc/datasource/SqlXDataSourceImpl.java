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

import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.rule.RoutingKey;
import com.github.sqlx.rule.RouteRule;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class SqlXDataSourceImpl extends AbstractSqlXDataSource {

    private final RouteRule rule;


    public SqlXDataSourceImpl(DatasourceManager datasourceManager, EventListener eventListener , RouteRule rule) {
        super(datasourceManager , eventListener);
        this.rule = rule;
    }

    @Override
    public RoutedDataSource getDataSource(String sql) {
        RouteInfo routeInfo = rule.route(new RoutingKey().setSql(sql));
        DataSource dataSource = getDataSourceWithName(routeInfo.getHitNodeAttr().getName());
        return new RoutedDataSource(dataSource , routeInfo);
    }


    /**
     * Retrieves a data source that is suitable for obtaining database metadata.
     * This method is typically used when metadata information is required,
     * such as during the initialization phase or when setting up connections.
     *
     * The method first checks if all data sources managed by the datasourceManager
     * are of the same database product. If they are, it returns the first data source
     * from the list. If not, it attempts to retrieve the default data source. If no
     * default data source is set, it falls back to the first data source in the list.
     *
     * @return a RoutedDataSource that can be used to access database metadata.
     */
    @Override
    public RoutedDataSource getDataSourceForDatabaseMetaData() {
        DataSource dataSource;
        if (datasourceManager.isSameDatabaseProduct()) {
            dataSource = datasourceManager.getDataSourceList().get(0);
        } else {
            Optional<DataSourceWrapper> optional = datasourceManager.getDefaultDataSource();
            dataSource = optional.orElseGet(() -> datasourceManager.getDataSourceList().get(0));
        }
        RouteInfo routeInfo = new RouteInfo();
        return new RoutedDataSource(dataSource , routeInfo);
    }
}
