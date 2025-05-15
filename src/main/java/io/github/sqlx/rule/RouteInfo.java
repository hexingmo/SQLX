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
package io.github.sqlx.rule;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.cluster.Cluster;
import io.github.sqlx.rule.group.RouteGroup;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.util.UUIDUtils;
import lombok.Data;

import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Data
public class RouteInfo {

    private final String routeId;

    private String sql;

    private Cluster cluster;

    private List<RouteGroup<?>> routingGroups;

    private RouteGroup<?> hitRoutingGroup;

    private RouteRule hitRule;

    private SqlAttribute sqlAttribute;

    private NodeAttribute hitNodeAttr;

    private Boolean isTransactionActive;

    private String transactionName;

    private String transactionId;

    private long beforeNanoTime;

    private long afterNanoTime;

    private long beforeTimeMillis;

    private long afterTimeMillis;

    public RouteInfo() {
        this.routeId = UUIDUtils.getSimpleUUID();
    }

    public long getTimeElapsedNanos() {
        return afterNanoTime - beforeNanoTime;
    }
}
