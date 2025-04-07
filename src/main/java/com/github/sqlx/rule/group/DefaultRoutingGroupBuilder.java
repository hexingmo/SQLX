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
package com.github.sqlx.rule.group;


import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.jdbc.datasource.DatasourceManager;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.rule.DefaultDataSourceRouteRule;
import com.github.sqlx.rule.SingleDatasourceRouteRule;
import com.github.sqlx.rule.TransactionRouteRule;
import com.github.sqlx.sql.parser.SqlParser;
import com.github.sqlx.rule.ForceRouteRule;
import com.github.sqlx.rule.RoutingNameSqlHintRouteRule;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class DefaultRoutingGroupBuilder {

    private SqlXConfiguration configuration;

    private SqlParser sqlParser;

    private Transaction transaction;

    private DatasourceManager datasourceManager;

    public static DefaultRoutingGroupBuilder builder() {
        return new DefaultRoutingGroupBuilder();
    }

    public DefaultRoutingGroupBuilder sqlXConfiguration(SqlXConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public DefaultRoutingGroupBuilder sqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }

    public DefaultRoutingGroupBuilder transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public DefaultRoutingGroupBuilder datasourceManager(DatasourceManager datasourceManager) {
        this.datasourceManager = datasourceManager;
        return this;
    }

    public DefaultRouteGroup build() {
        DefaultRouteGroup routingGroup = new DefaultRouteGroup(sqlParser);
        routingGroup.install(new SingleDatasourceRouteRule(0 , sqlParser , datasourceManager));
        routingGroup.install(new TransactionRouteRule(10 , sqlParser , configuration , transaction));
        routingGroup.install(new ForceRouteRule(20 , sqlParser , configuration));
        routingGroup.install(new RoutingNameSqlHintRouteRule(30 , sqlParser , configuration));
        routingGroup.install(new DefaultDataSourceRouteRule(40 , sqlParser , datasourceManager));
        return routingGroup;
    }
}
