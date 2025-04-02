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

import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.sql.DefaultSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.SqlAttributeBuilder;
import com.github.sqlx.sql.SqlType;
import com.github.sqlx.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Parse SQL statements using JSqlParser.
 * <p><a href="https://github.com/JSQLParser/JSqlParser">JSqlParser</a><p/>
 *
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class JSqlParser extends AbstractSqlParser {

    private static final Set<Class<? extends Statement>> READ_STATEMENTS = Collections.synchronizedSet(new HashSet<>());

    static {
        // register read statement
        registerReadStatement(Select.class);
        registerReadStatement(ExplainStatement.class);
        registerReadStatement(ShowColumnsStatement.class);
        registerReadStatement(ShowStatement.class);
        registerReadStatement(ShowIndexStatement.class);
        registerReadStatement(ShowTablesStatement.class);
    }

    public JSqlParser(SqlXConfiguration routingConf) {
        super(routingConf);
    }

    private static void registerReadStatement(Class<? extends Statement> type) {
        READ_STATEMENTS.add(type);
    }

    @Override
    protected SqlAttribute internalParse(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        SqlAttributeVisitor visitor = new SqlAttributeVisitor();
        return visitor.build(statement).setSql(sql);
    }

    private static class SqlAttributeVisitor extends TablesNamesFinder implements SqlAttributeBuilder<Statement> {

        private final DefaultSqlAttribute statement = new DefaultSqlAttribute();

        private final Set<String> databases = new HashSet<>();

        private final Set<String> tables = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> tableSet = new HashSet<>();

        private final Set<String> subTables = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> subTableSet = new HashSet<>();

        private final Set<String> joinTables = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> joinTableSet = new HashSet<>();

        private final Set<String> fromTables = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> fromTableSet = new HashSet<>();

        private final Set<String> insertTables = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> insertTableSet = new HashSet<>();

        private final Set<String> updateTables = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> updateTableSet = new HashSet<>();

        private final Set<String> deleteTables = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> deleteTableSet = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> readTableSet = new HashSet<>();

        private final Set<com.github.sqlx.sql.Table> writeTableSet = new HashSet<>();


        private SqlType statementType = SqlType.OTHER;

        private Model prevVisited;

        @Override
        public DefaultSqlAttribute build(Statement obj) {
            getTableList(obj);
            if (fromTables.size() > subTables.size() && fromTables.containsAll(subTables)) {
                fromTables.removeAll(subTables);
            }
            if (fromTables.size() > joinTables.size() && fromTables.containsAll(joinTables)) {
                fromTables.removeAll(joinTables);
            }
            if (!CollectionUtils.isEqualCollection(fromTables , insertTables)) {
                fromTables.removeAll(insertTables);
            }
            if (!CollectionUtils.isEqualCollection(fromTables , updateTables)) {
                fromTables.removeAll(updateTables);
            }
            if (!CollectionUtils.isEqualCollection(fromTables , deleteTables)) {
                fromTables.removeAll(deleteTables);
            }

            databases.removeIf(Objects::isNull);
            tables.removeIf(Objects::isNull);
            fromTables.removeIf(Objects::isNull);
            joinTables.removeIf(Objects::isNull);
            subTables.removeIf(Objects::isNull);
            prevVisited = null;
            for (com.github.sqlx.sql.Table table : tableSet) {
                if (insertTableSet.contains(table) || updateTableSet.contains(table) || deleteTableSet.contains(table)) {
                    writeTableSet.add(table);
                }
            }

            for (com.github.sqlx.sql.Table table : tableSet) {
                if (!writeTableSet.contains(table)) {
                    readTableSet.add(table);
                }
            }

            return statement.setDatabases(databases)
                    .setSimpleFromTables(fromTables).setFromTables(fromTableSet)
                    .setSimpleTables(tables).setTables(tableSet)
                    .setSimpleJoinTables(joinTables).setJoinTables(joinTableSet)
                    .setSimpleSubTables(subTables).setSubTables(subTableSet)
                    .setSimpleInsertTables(insertTables).setInsertTables(insertTableSet)
                    .setSimpleUpdateTables(updateTables).setUpdateTables(updateTableSet)
                    .setSimpleDeleteTables(deleteTables).setDeleteTables(deleteTableSet)
                    .setReadTables(readTableSet).setWriteTables(writeTableSet)
                    .setWrite(isWrite(obj)).setRead(isRead(obj)).setSqlType(statementType);
        }

        @Override
        public void visit(Insert insert) {
            prevVisited = insert;
            super.visit(insert);
            Table table = insert.getTable();
            String database = table.getSchemaName();
            String tableName = table.getName();
            insertTables.add(tableName);
            com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), database, tableName);
            insertTableSet.add(tbe);

            databases.add(table.getSchemaName());
            statementType = SqlType.INSERT;
        }

        @Override
        public void visit(Update update) {
            prevVisited = update;
            super.visit(update);
            Table table = update.getTable();
            String database = table.getSchemaName();
            String tableName = table.getName();
            updateTables.add(tableName);
            com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), database, tableName);
            updateTableSet.add(tbe);
            databases.add(table.getSchemaName());

            // parse set fragment sub select
            List<UpdateSet> sets = update.getUpdateSets();
            if (Objects.nonNull(sets) && !sets.isEmpty()) {
                for (UpdateSet set : sets) {
                    for (Expression expression : set.getExpressions()) {
                        expression.accept(this);
                    }
                }
            }
            statementType = SqlType.UPDATE;
        }

        @Override
        public void visit(Delete delete) {
            super.visit(delete);
            Table table = delete.getTable();

            String database = table.getSchemaName();
            String tableName = table.getName();
            com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), database, tableName);
            databases.add(table.getSchemaName());
            deleteTables.add(tableName);
            deleteTableSet.add(tbe);
            statementType = SqlType.DELETE;
        }

        @Override
        public void visit(SubJoin subjoin) {
            super.visit(subjoin);

            List<Join> joinList = subjoin.getJoinList();
            if (Objects.nonNull(joinList)) {
                for (Join join : joinList) {
                    FromItem rightItem = join.getRightItem();
                    if (rightItem instanceof Table) {
                        Table table = (Table) rightItem;
                        String database = table.getSchemaName();
                        String tableName = table.getName();
                        joinTables.add(tableName);
                        com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), database, tableName);
                        joinTableSet.add(tbe);
                    }
                }
            }
        }

        @Override
        public void visit(Table table) {
            super.visit(table);
            String database = table.getSchemaName();
            if (Objects.nonNull(database)) {
                databases.add(database);
            }
            tables.add(table.getName());
            com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), database, table.getName());
            tableSet.add(tbe);
            fromTables.add(table.getName());
            fromTableSet.add(tbe);
        }

        @Override
        public void visit(SubSelect subSelect) {
            super.visit(subSelect);
            SelectBody selectBody = subSelect.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                PlainSelect select = (PlainSelect) selectBody;
                if (select.getFromItem() instanceof Table) {
                    Table table = (Table) select.getFromItem();
                    String tableName = table.getName();
                    subTables.add(tableName);
                    com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), tableName, table.getName());
                    subTableSet.add(tbe);
                }
            }
        }

        @Override
        public void visit(PlainSelect plainSelect) {
            if (Objects.equals(prevVisited , plainSelect)) {
                return;
            }
            prevVisited = plainSelect;
            super.visit(plainSelect);

            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                String database = table.getSchemaName();
                String tableName = table.getName();
                fromTables.add(tableName);
                com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), database, tableName);
                fromTableSet.add(tbe);
            }

            List<Join> joins = plainSelect.getJoins();
            if (Objects.nonNull(joins)) {
                for (Join join : joins) {
                    FromItem rightItem = join.getRightItem();
                    if (rightItem instanceof Table) {
                        Table table = (Table) rightItem;
                        String database = table.getSchemaName();
                        String tableName = table.getName();
                        joinTables.add(tableName);
                        com.github.sqlx.sql.Table tbe = new com.github.sqlx.sql.Table(table.getFullyQualifiedName(), database, tableName);
                        joinTableSet.add(tbe);
                    }
                }
            }
            statementType = SqlType.SELECT;
        }

        private boolean isRead(Statement statement) {
            return READ_STATEMENTS.contains(statement.getClass());
        }

        private boolean isWrite(Statement statement) {
            return !isRead(statement);
        }
    }
}
