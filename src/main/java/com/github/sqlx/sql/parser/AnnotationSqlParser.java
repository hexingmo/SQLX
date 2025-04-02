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

package com.github.sqlx.sql.parser;

import com.github.sqlx.sql.AnnotationSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;

import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class AnnotationSqlParser implements SqlParser {


    private final SqlParser delegate;

    private final AnnotationSqlHintParser sqlHintParser;


    public AnnotationSqlParser(SqlParser delegate, AnnotationSqlHintParser sqlHintParser) {
        this.delegate = delegate;
        this.sqlHintParser = sqlHintParser;
    }

    @Override
    public SqlAttribute parse(String sql) {

        SqlAttribute sqlAttribute = null;
        SqlHint sqlHint = sqlHintParser.parse(sql);
        if (Objects.nonNull(sqlHint)) {
            SqlAttribute attribute = delegate.parse(sqlHint.getNativeSql());
            if (Objects.nonNull(attribute)) {
                sqlAttribute = new AnnotationSqlAttribute(attribute , sqlHint);
            }
        } else {
            sqlAttribute = delegate.parse(sql);
        }
        return sqlAttribute;
    }
}
