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

import com.github.sqlx.RoutingContext;
import com.github.sqlx.NodeAttribute;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.integration.springboot.RouteAttribute;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;
import com.github.sqlx.util.CollectionUtils;
import com.github.sqlx.util.RandomUtils;

import java.util.List;

/**
 * Route to the specified data source forcefully,
 * and randomly select between multiple data sources
 * if more than one are specified.
 *
 * @author He Xing Mo
 * @since 1.0
 *
 * @see RoutingContext#force(RouteAttribute) ()
 */
public class ForceTargetRouteRule extends AbstractRouteRule {

    private final SqlXConfiguration configuration;

    public ForceTargetRouteRule(Integer priority, SqlParser sqlParser, LoadBalance readLoadBalance, LoadBalance writeLoadBalance, SqlXConfiguration configuration) {
        super(priority, sqlParser, readLoadBalance, writeLoadBalance);
        this.configuration = configuration;
    }

    @Override
    public NodeAttribute routing(SqlAttribute sqlAttribute) {

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        if (ra == null) {
            return null;
        }

        List<String> nodes = ra.getNodes();
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }

        if (nodes.size() == 1) {
            return configuration.getNodeAttribute((String) nodes.toArray()[0]);
        }

        int index = RandomUtils.nextInt(0, nodes.size());
        String node = nodes.get(index);
        return configuration.getNodeAttribute(node);
    }

}
