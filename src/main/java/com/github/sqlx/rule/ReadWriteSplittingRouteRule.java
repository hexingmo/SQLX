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
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.sql.SqlAttribute;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Write statements will be routed to the write data source,
 * and read statements will be routed to the read data source,
 * except when in a transaction.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class ReadWriteSplittingRouteRule extends AbstractRouteRule {


    public ReadWriteSplittingRouteRule(Integer priority, LoadBalance readLoadBalance, LoadBalance writeLoadBalance) {
        super(priority, readLoadBalance, writeLoadBalance);
    }

    @Override
    public NodeAttribute routing(SqlAttribute sqlAttribute) {
        if (Objects.isNull(sqlAttribute)) {
            return null;
        }
        if (sqlAttribute.isWrite()) {
            return chooseWriteNode();
        }

        NodeAttribute nodeAttribute = chooseReadNode();
        if (Objects.isNull(nodeAttribute)) {
            log.warn("No available readable nodes attempted to obtain writable nodes SQL:[{}]" , sqlAttribute.getSql());
            nodeAttribute = chooseWriteNode();
        }
        return nodeAttribute;
    }
}
