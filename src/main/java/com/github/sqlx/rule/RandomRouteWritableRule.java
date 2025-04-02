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
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;

import java.util.List;
import java.util.Random;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class RandomRouteWritableRule extends AbstractRouteRule {

    private final Random random = new Random();

    private final SqlXConfiguration routingConf;

    public RandomRouteWritableRule(Integer priority, SqlParser sqlParser, LoadBalance<NodeAttribute> readLoadBalance, LoadBalance<NodeAttribute> writeLoadBalance, SqlXConfiguration routingConf) {
        super(priority, sqlParser, readLoadBalance, writeLoadBalance);
        this.routingConf = routingConf;
    }

    @Override
    public NodeAttribute routing(SqlAttribute sqlAttribute) {
        List<NodeAttribute> nodeAttributes = routingConf.getWritableRoutingNodeAttributes();
        int index = random.nextInt(nodeAttributes.size());
        return nodeAttributes.get(index);
    }
}
