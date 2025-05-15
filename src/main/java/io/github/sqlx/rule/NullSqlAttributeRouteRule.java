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
import io.github.sqlx.loadbalance.LoadBalance;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.parser.SqlParser;

import java.util.Objects;

/**
 * When the SQL cannot be parsed by the SqlParser,
 * data source routing based on the SQL cannot be performed.
 * It will be routed to the write data source by default.
 *
 * @author He Xing Mo
 * @since 1.0
 *
 * @see SqlParser
 */
public class NullSqlAttributeRouteRule extends AbstractRouteRule {

    public NullSqlAttributeRouteRule(Integer priority, LoadBalance readLoadBalance, LoadBalance writeLoadBalance) {
        super(priority, readLoadBalance, writeLoadBalance);
    }

    @Override
    public NodeAttribute routing(SqlAttribute attribute) {
        return Objects.isNull(attribute) ? chooseWriteNode() : null;
    }

}
