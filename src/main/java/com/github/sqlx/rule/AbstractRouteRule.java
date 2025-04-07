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

package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;

import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractRouteRule implements SqlAttributeRouteRule {


    protected final Integer priority;

    protected final SqlParser sqlParser;

    protected LoadBalance readLoadBalance;

    protected LoadBalance writeLoadBalance;

    protected AbstractRouteRule(Integer priority , SqlParser sqlParser , LoadBalance readLoadBalance , LoadBalance writeLoadBalance) {
        this(priority , sqlParser);
        this.readLoadBalance = readLoadBalance;
        this.writeLoadBalance = writeLoadBalance;
    }

    protected AbstractRouteRule(Integer priority, SqlParser sqlParser) {
        this.priority = priority;
        this.sqlParser = sqlParser;
    }

    @Override
    public RouteInfo route(RoutingKey key) {
        SqlAttribute sqlAttribute = null;
        if (Objects.nonNull(key) && Objects.nonNull(key.getSql())) {
            sqlAttribute = sqlParser.parse(key.getSql());
        }
        NodeAttribute nodeAttr = routing(sqlAttribute);
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setSqlAttribute(sqlAttribute);
        routeInfo.setHitNodeAttr(nodeAttr);
        return routeInfo;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    protected NodeAttribute chooseWriteNode() {
        return writeLoadBalance.choose();
    }

    protected NodeAttribute chooseReadNode() {
        return readLoadBalance.choose();
    }

}
