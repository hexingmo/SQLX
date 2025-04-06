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
import com.github.sqlx.sql.AnnotationSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.NodeNameSqlHintConverter;
import com.github.sqlx.sql.parser.SqlHint;
import com.github.sqlx.sql.parser.SqlHintConverter;
import com.github.sqlx.sql.parser.SqlParser;

import java.util.Objects;

/**
 * Annotation Hint routingTargetName Routing Rule
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class RoutingNameSqlHintRouteRule extends AbstractRouteRule {

    private static final SqlHintConverter<String> SQL_HINT_CONVERTER = new NodeNameSqlHintConverter();

    private final SqlXConfiguration configuration;

    public RoutingNameSqlHintRouteRule(Integer priority, SqlParser sqlParser, LoadBalance readLoadBalance, LoadBalance writeLoadBalance, SqlXConfiguration configuration) {
        super(priority, sqlParser, readLoadBalance, writeLoadBalance);
        this.configuration = configuration;
    }

    @Override
    public NodeAttribute routing(SqlAttribute attribute) {

        if (Objects.isNull(attribute)) {
            return null;
        }

        if (!(attribute instanceof AnnotationSqlAttribute)) {
            return null;
        }

        SqlHint sqlHint = ((AnnotationSqlAttribute) attribute).getSqlHint();
        if (sqlHint == null) {
            return null;
        }

        String nodeName = SQL_HINT_CONVERTER.convert(sqlHint);
        return configuration.getNodeAttribute(nodeName);
    }
}
