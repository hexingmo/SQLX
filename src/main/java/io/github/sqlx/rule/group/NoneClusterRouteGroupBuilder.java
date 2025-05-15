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
package io.github.sqlx.rule.group;


import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.jdbc.datasource.DatasourceManager;
import io.github.sqlx.jdbc.transaction.Transaction;
import io.github.sqlx.rule.DefaultDataSourceRouteRule;
import io.github.sqlx.rule.SingleDatasourceRouteRule;
import io.github.sqlx.rule.TransactionRouteRule;
import io.github.sqlx.sql.parser.SqlParser;
import io.github.sqlx.rule.ForceRouteRule;
import io.github.sqlx.rule.DataSourceNameSqlHintRouteRule;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class NoneClusterRouteGroupBuilder {

    private SqlXConfiguration configuration;

    private SqlParser sqlParser;

    private Transaction transaction;

    private DatasourceManager datasourceManager;

    public static NoneClusterRouteGroupBuilder builder() {
        return new NoneClusterRouteGroupBuilder();
    }

    public NoneClusterRouteGroupBuilder sqlXConfiguration(SqlXConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public NoneClusterRouteGroupBuilder sqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }

    public NoneClusterRouteGroupBuilder transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public NoneClusterRouteGroupBuilder datasourceManager(DatasourceManager datasourceManager) {
        this.datasourceManager = datasourceManager;
        return this;
    }

    public DefaultRouteGroup build() {
        DefaultRouteGroup routingGroup = new DefaultRouteGroup(sqlParser);
        routingGroup.install(new TransactionRouteRule(0 , configuration , transaction));
        routingGroup.install(new SingleDatasourceRouteRule(10 , datasourceManager));
        routingGroup.install(new DataSourceNameSqlHintRouteRule(20 , configuration));
        routingGroup.install(new ForceRouteRule(30 , configuration));
        routingGroup.install(new DefaultDataSourceRouteRule(40 , datasourceManager));
        return routingGroup;
    }
}
