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

package io.github.sqlx.sql;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
@Accessors(chain = true)
public class DefaultSqlAttribute implements SqlAttribute {

    private String sql;

    private String nativeSql;

    private SqlType sqlType;

    private boolean isWrite;

    private boolean isRead;

    private Set<String> databases;

    private Set<String> simpleTables;

    private Set<Table> tables;

    private Set<String> simpleFromTables;

    private Set<Table> fromTables;

    private Set<String> simpleJoinTables;

    private Set<Table> joinTables;

    private Set<String> simpleSubTables;

    private Set<Table> subTables;

    private Set<String> simpleInsertTables;

    private Set<Table> insertTables;

    private Set<String> simpleUpdateTables;

    private Set<Table> updateTables;

    private Set<String> simpleDeleteTables;

    private Set<Table> deleteTables;

    private Set<Table> readTables;

    private Set<Table> writeTables;


    public boolean isWrite() {
        return isWrite;
    }

    public boolean isRead() {
        return isRead;
    }

    @Override
    public void setDefaultDatabase(String database) {
        if (this.databases != null) {
            this.databases.add(database);
        }
        setDefaultDatabase(tables , database);
        setDefaultDatabase(readTables , database);
        setDefaultDatabase(writeTables , database);
        setDefaultDatabase(fromTables , database);
        setDefaultDatabase(joinTables , database);
        setDefaultDatabase(subTables , database);
        setDefaultDatabase(insertTables , database);
        setDefaultDatabase(updateTables , database);
        setDefaultDatabase(deleteTables , database);
    }



    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public String getNativeSql() {
        return nativeSql;
    }

    @Override
    public SqlType getSqlType() {
        return sqlType;
    }

    @Override
    public Set<String> getDatabases() {
        return databases;
    }

    @Override
    public Set<Table> getTables() {
        return tables;
    }

    @Override
    public Set<Table> getFromTables() {
        return fromTables;
    }

    @Override
    public Set<Table> getJoinTables() {
        return joinTables;
    }

    @Override
    public Set<Table> getSubTables() {
        return subTables;
    }

    @Override
    public Set<Table> getInsertTables() {
        return insertTables;
    }

    @Override
    public Set<Table> getUpdateTables() {
        return updateTables;
    }

    @Override
    public Set<Table> getDeleteTables() {
        return deleteTables;
    }

    @Override
    public Set<String> getSimpleTables() {
        return simpleTables;
    }

    @Override
    public Set<String> getSimpleFromTables() {
        return simpleFromTables;
    }

    @Override
    public Set<String> getSimpleJoinTables() {
        return simpleJoinTables;
    }

    @Override
    public Set<String> getSimpleSubTables() {
        return simpleSubTables;
    }

    @Override
    public Set<String> getSimpleInsertTables() {
        return simpleInsertTables;
    }

    @Override
    public Set<String> getSimpleUpdateTables() {
        return simpleUpdateTables;
    }

    @Override
    public Set<String> getSimpleDeleteTables() {
        return simpleDeleteTables;
    }

    @Override
    public Set<Table> getReadTables() {
        return readTables;
    }

    @Override
    public Set<Table> getWriteTables() {
        return writeTables;
    }

    private void setDefaultDatabase(Set<Table> tables , String database) {
        if (tables != null) {
            Set<Table> newTables = new HashSet<>();
            for (Table table : tables) {
                table.setFullTableName(database + "." + table.getTable());
                table.setDatabase(database);
                newTables.add(table);
            }
            tables.clear();
            tables.addAll(newTables);
        }
    }
}
