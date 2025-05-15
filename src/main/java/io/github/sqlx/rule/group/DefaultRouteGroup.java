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

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.rule.RouteInfo;
import io.github.sqlx.rule.PriorityRouteRule;
import io.github.sqlx.rule.RoutingKey;
import io.github.sqlx.rule.RouteRule;
import io.github.sqlx.rule.SqlAttributeRouteRule;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.parser.SqlParser;
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
    public final RouteInfo route(RoutingKey key) {
        RouteInfo routeInfo = new RouteInfo();
        SqlAttribute sqlAttribute = null;
        if (Objects.nonNull(key) && Objects.nonNull(key.getSql())) {
            sqlAttribute = sqlParser.parse(key.getSql());
        }

        NodeAttribute target = null;
        SqlAttributeRouteRule rule = null;

        for (SqlAttributeRouteRule routingRule : routingRules) {
            NodeAttribute currentTarget = routingRule.routing(sqlAttribute);
            if (Objects.nonNull(currentTarget)) {
                if (Objects.isNull(currentTarget.getNodeState()) || currentTarget.getNodeState().isAvailable()) {
                    target = currentTarget;
                    rule = routingRule;
                    break;
                } else {
                    log.warn("Node with state '{}' is unavailable, ignoring routing rule '{}'.", currentTarget.getNodeState(), routingRule.getClass().getSimpleName());
                }
            }
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
