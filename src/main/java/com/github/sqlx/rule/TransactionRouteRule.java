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
import com.github.sqlx.RoutingContext;
import com.github.sqlx.config.ClusterConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.exception.SqlRouteException;
import com.github.sqlx.integration.springboot.RouteAttribute;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.util.CollectionUtils;
import com.github.sqlx.util.RandomUtils;

import java.util.List;
import java.util.Set;

/**
 * When transactions exist, the routing rules are as follows:
 * If a read-only transaction exists and the current SQL being
 * executed is a read statement, it will be routed to the read
 * data source. If the transaction is not read-only and there
 * are write statements in the transaction, all SQL statements
 * in the transaction will be routed to the write data source
 * for execution.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class TransactionRouteRule extends AbstractRouteRule {

    private final Transaction transaction;

    private final SqlXConfiguration configuration;


    public TransactionRouteRule(Integer priority, SqlXConfiguration configuration, Transaction transaction) {
        super(priority);
        this.configuration = configuration;
        this.transaction = transaction;
    }

    @Override
    public NodeAttribute routing(SqlAttribute attribute) {

        if (!transaction.isActive()) {
            return null;
        }
        NodeAttribute node = transaction.getCurrentNode();
        if (node != null) {
            transaction.addSql(attribute);
            return node;
        }

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        if (ra == null) {
            throw new SqlRouteException("Transaction is active, but no routing attribute is found.");
        }
        node = getNode(ra);
        if (node != null) {
            transaction.registerNode(node , attribute);
        }
        return node;
    }


    private NodeAttribute getNode(RouteAttribute ra) {

        List<String> nodes = ra.getNodes();
        if (CollectionUtils.isNotEmpty(nodes)) {
            int index = RandomUtils.nextInt(0, nodes.size());
            return configuration.getNodeAttribute(nodes.get(index));
        }
        ClusterConfiguration cluster = configuration.getCluster(ra.getCluster());
        if (cluster != null) {
            Set<NodeAttribute> writableNodes = cluster.getWritableRoutingNodeAttributes();
            if (CollectionUtils.isEmpty(writableNodes)) {
                throw new SqlRouteException("Transaction is active,But no writable nodes found in cluster: " + ra.getCluster());
            }
            int index = RandomUtils.nextInt(0, writableNodes.size());
            return (NodeAttribute) writableNodes.toArray()[index];
        }
        return null;
    }
}
