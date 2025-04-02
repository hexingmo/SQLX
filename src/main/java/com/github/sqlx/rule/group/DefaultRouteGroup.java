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

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.NodeState;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.rule.PriorityRouteRule;
import com.github.sqlx.rule.RoutingKey;
import com.github.sqlx.rule.RouteRule;
import com.github.sqlx.rule.SqlAttributeRouteRule;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class DefaultRouteGroup extends AbstractComparableRouteGroup<SqlAttributeRouteRule> {

    private final SqlParser sqlParser;

    public DefaultRouteGroup(SqlParser sqlParser) {
        super(Comparator.comparingInt(PriorityRouteRule::priority));
        this.sqlParser = sqlParser;
    }


    @Override
    public RouteInfo route(RoutingKey key) {

        RouteInfo routeInfo = new RouteInfo();
        NodeAttribute target = null;
        SqlAttribute sqlAttribute = null;
        if (Objects.nonNull(key) && Objects.nonNull(key.getSql())) {
            sqlAttribute = sqlParser.parse(key.getSql());
        }

        SqlAttributeRouteRule rule = null;
        for (SqlAttributeRouteRule routingRule : routingRules) {
            target = routingRule.routing(sqlAttribute);
            if (Objects.isNull(target)) {
                continue;
            }
            if (Objects.nonNull(target.getNodeState()) && !target.getNodeState().isAvailable()) {
                log.warn("Node with state '{}' is unavailable, ignoring routing rule '{}'.", target.getNodeState(), routingRule.getClass().getSimpleName());
                continue;
            }
            rule = routingRule;
            break;
        }

        routeInfo.setHitRule(rule);
        routeInfo.setSqlAttribute(sqlAttribute);
        routeInfo.setHitNodeAttr(target);
        return routeInfo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append(" Routing Rules : ");
        if (routingRules == null) {
            sb.append("no match");
        } else if (routingRules.isEmpty()) {
            sb.append("[] empty (bypassed by RoutingRule='none') ");
        } else {
            sb.append("[\n");
            for (RouteRule r : routingRules) {
                sb.append("  ").append(r.getClass().getSimpleName()).append("\n");
            }
            sb.append("]");
        }

        return sb.toString();
    }

}
