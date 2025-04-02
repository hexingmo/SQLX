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
import com.github.sqlx.loadbalance.WeightRandomLoadBalance;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private final SqlXConfiguration routingConf;

    public ForceTargetRouteRule(Integer priority, SqlParser sqlParser, LoadBalance<NodeAttribute> readLoadBalance, LoadBalance<NodeAttribute> writeLoadBalance, SqlXConfiguration routingConf) {
        super(priority, sqlParser, readLoadBalance, writeLoadBalance);
        this.routingConf = routingConf;
    }

    @Override
    public NodeAttribute routing(SqlAttribute sqlAttribute) {

        if (sqlAttribute == null) {
            return null;
        }
        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        if (ra == null) {
            return null;
        }
        List<String> nodes = ra.getNodes();
        if (nodes != null && nodes.size() == 1) {
            return routingConf.getRoutingNodeAttribute((String) nodes.toArray()[0]);
        }
        if (nodes != null && !nodes.isEmpty()) {
            List<NodeAttribute> writableNodeAttrs = new ArrayList<>();
            List<NodeAttribute> readableNodeAttrs = new ArrayList<>();

            for (String forceDataSource : nodes) {
                NodeAttribute nodeAttr = routingConf.getRoutingNodeAttribute(forceDataSource);
                if (nodeAttr != null && nodeAttr.getNodeType().canWrite()) {
                    writableNodeAttrs.add(nodeAttr);
                }

                if (nodeAttr != null && nodeAttr.getNodeType().canRead()) {
                    readableNodeAttrs.add(nodeAttr);
                }
            }

            if (sqlAttribute.isWrite() && !writableNodeAttrs.isEmpty()) {
                if (writableNodeAttrs.size() == 1) {
                    return writableNodeAttrs.get(0);
                }
                WeightRandomLoadBalance loadBalance = new WeightRandomLoadBalance(writableNodeAttrs);
                NodeAttribute attribute = loadBalance.choose();
                if (Objects.nonNull(attribute)) {
                    return attribute;
                }
            }

            if (sqlAttribute.isRead() && !readableNodeAttrs.isEmpty()) {
                if (readableNodeAttrs.size() == 1) {
                    return readableNodeAttrs.get(0);
                }
                WeightRandomLoadBalance loadBalance = new WeightRandomLoadBalance(readableNodeAttrs);
                NodeAttribute attribute = loadBalance.choose();
                if (Objects.nonNull(attribute)) {
                    return attribute;
                }
            }
        }

        return null;
    }

}
