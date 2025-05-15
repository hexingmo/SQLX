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
import io.github.sqlx.RoutingContext;
import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.exception.SqlRouteException;
import io.github.sqlx.integration.springboot.RouteAttribute;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.util.CollectionUtils;
import io.github.sqlx.util.RandomUtils;
import io.github.sqlx.util.SqlUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Route to the specified data source forcefully,
 * and randomly select between multiple data sources
 * if more than one are specified.
 *
 * @author He Xing Mo
 * @see RoutingContext#force(RouteAttribute)
 * @since 1.0
 */
public class ForceRouteRule extends AbstractRouteRule {

    private final SqlXConfiguration configuration;

    public ForceRouteRule(Integer priority, SqlXConfiguration configuration) {
        super(priority);
        this.configuration = configuration;
    }

    @Override
    public NodeAttribute routing(SqlAttribute sqlAttribute) {

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        if (ra == null) {
            return null;
        }

        if (SqlUtils.isAnnotationSql(sqlAttribute)) {
            return null;
        }

        List<String> nodes = ra.getNodes();
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        // Filter write nodes are required.
        if (StringUtils.isNotEmpty(ra.getCluster()) && sqlAttribute.isWrite()) {
            Set<String> writableNodes = configuration.getCluster(ra.getCluster()).getWritableNodes();
            List<String> availableNodes = nodes.stream()
                    .filter(writableNodes::contains).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(availableNodes)) {
                throw new SqlRouteException("There are no available write nodes. Cluster: " + ra.getCluster() + " writable nodes: " + writableNodes);
            }
            nodes = availableNodes;
        }

        if (nodes.size() == 1) {
            return configuration.getNodeAttribute((String) nodes.toArray()[0]);
        }

        int index = RandomUtils.nextInt(0, nodes.size());
        String node = nodes.get(index);
        return configuration.getNodeAttribute(node);
    }

}
