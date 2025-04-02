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

package com.github.sqlx.sql;

import com.github.sqlx.sql.parser.SqlHint;
import com.github.sqlx.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */

public class AnnotationSqlAttribute implements SqlAttribute {

    private final SqlAttribute delegate;

    private final SqlHint sqlHint;

    public AnnotationSqlAttribute(SqlAttribute delegate, SqlHint sqlHint) {
        this.delegate = delegate;
        this.sqlHint = sqlHint;
    }

    public SqlHint getSqlHint() {
        return sqlHint;
    }

    @Override
    public void setDefaultDatabase(String database) {
        if (delegate != null) {
            delegate.setDefaultDatabase(database);
        }
    }

    @Override
    public String getSql() {
        return delegate != null ? delegate.getSql() : null;
    }

    @Override
    public String getNativeSql() {
        String nativeSql = delegate != null ? delegate.getNativeSql() : "";
        if (StringUtils.isBlank(nativeSql) && sqlHint != null) {
            nativeSql = sqlHint.getNativeSql();
        }
        return nativeSql;
    }

    @Override
    public SqlType getSqlType() {
        return delegate != null ? delegate.getSqlType() : null;
    }

    @Override
    public boolean isWrite() {
        return delegate == null || delegate.isWrite();
    }

    @Override
    public boolean isRead() {
        return delegate == null || delegate.isRead();
    }

    @Override
    public Set<String> getDatabases() {
        return delegate != null ? delegate.getDatabases() : new HashSet<>();
    }

    @Override
    public Set<String> getSimpleTables() {
        return delegate != null ? delegate.getSimpleTables() : new HashSet<>();
    }

    @Override
    public Set<String> getSimpleFromTables() {
        return delegate != null ? delegate.getSimpleFromTables() : new HashSet<>();
    }

    @Override
    public Set<String> getSimpleJoinTables() {
        return delegate != null ? delegate.getSimpleJoinTables() : new HashSet<>();
    }

    @Override
    public Set<String> getSimpleSubTables() {
        return delegate != null ? delegate.getSimpleSubTables() : new HashSet<>();
    }

    @Override
    public Set<String> getSimpleInsertTables() {
        return delegate != null ? delegate.getSimpleInsertTables() : new HashSet<>();
    }

    @Override
    public Set<String> getSimpleUpdateTables() {
        return delegate != null ? delegate.getSimpleUpdateTables() : new HashSet<>();
    }

    @Override
    public Set<String> getSimpleDeleteTables() {
        return delegate != null ? delegate.getSimpleDeleteTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getTables() {
        return delegate != null ? delegate.getTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getFromTables() {
        return delegate != null ? delegate.getFromTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getJoinTables() {
        return delegate != null ? delegate.getJoinTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getSubTables() {
        return delegate != null ? delegate.getSubTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getInsertTables() {
        return delegate != null ? delegate.getInsertTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getUpdateTables() {
        return delegate != null ? delegate.getUpdateTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getDeleteTables() {
        return delegate != null ? delegate.getDeleteTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getReadTables() {
        return delegate != null ? delegate.getReadTables() : new HashSet<>();
    }

    @Override
    public Set<Table> getWriteTables() {
        return delegate != null ? delegate.getWriteTables() : new HashSet<>();
    }
}
