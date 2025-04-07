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
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.rule.DefaultDataSourceRouteRule;
import com.github.sqlx.rule.SingleDatasourceRouteRule;
import com.github.sqlx.rule.TransactionRouteRule;
import com.github.sqlx.sql.parser.SqlParser;
import com.github.sqlx.rule.ForceTargetRouteRule;
import com.github.sqlx.rule.NullSqlAttributeRouteRule;
import com.github.sqlx.rule.ReadWriteSplittingRouteRule;
import com.github.sqlx.rule.RoutingNameSqlHintRouteRule;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class DefaultRoutingGroupBuilder {

    private SqlXConfiguration sqlXConfiguration;

    private SqlParser sqlParser;

    private Transaction transaction;

    private LoadBalance readLoadBalance;

    private LoadBalance writeLoadBalance;

    private DatasourceManager datasourceManager;

    public static DefaultRoutingGroupBuilder builder() {
        return new DefaultRoutingGroupBuilder();
    }

    public DefaultRoutingGroupBuilder sqlXConfiguration(SqlXConfiguration sqlXConfiguration) {
        this.sqlXConfiguration = sqlXConfiguration;
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

    public DefaultRoutingGroupBuilder readLoadBalance(LoadBalance readLoadBalance) {
        this.readLoadBalance = readLoadBalance;
        return this;
    }

    public DefaultRoutingGroupBuilder writeLoadBalance(LoadBalance writeLoadBalance) {
        this.writeLoadBalance = writeLoadBalance;
        return this;
    }

    public DefaultRoutingGroupBuilder datasourceManager(DatasourceManager datasourceManager) {
        this.datasourceManager = datasourceManager;
        return this;
    }

    public DefaultRouteGroup build() {
        DefaultRouteGroup routingGroup = new DefaultRouteGroup(sqlParser);
        routingGroup.install(new SingleDatasourceRouteRule(-10 , sqlParser , readLoadBalance , writeLoadBalance , datasourceManager));
        routingGroup.install(new TransactionRouteRule(0 , sqlParser , readLoadBalance , writeLoadBalance , transaction));
        routingGroup.install(new ForceTargetRouteRule(10 , sqlParser , sqlXConfiguration));
        routingGroup.install(new RoutingNameSqlHintRouteRule(30 , sqlParser , sqlXConfiguration));
        routingGroup.install(new ReadWriteSplittingRouteRule(40 , sqlParser ,  readLoadBalance , writeLoadBalance));
        routingGroup.install(new NullSqlAttributeRouteRule(50 , sqlParser ,  readLoadBalance , writeLoadBalance));
        routingGroup.install(new DefaultDataSourceRouteRule(60 , sqlParser , datasourceManager));
        return routingGroup;
    }
}
