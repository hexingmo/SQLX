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

import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public interface SqlAttribute {

    /**
     *
     * @return the SQL statement
     */
    String getSql();

    String getNativeSql();

    /**
     * Returns a SqlType enumeration instance
     * @return SqlType enumeration instance
     * @see SqlType
     */
    SqlType getSqlType();

    /**
     * Determine whether the SQL statement is a write statement or a read statement
     * @return Returns true if it is a write statement, otherwise returns false
     */
    boolean isWrite();

    /**
     * Determine whether the SQL statement is a write statement or a read statement
     * @return Returns true if it is a read statement, otherwise returns false
     */
    boolean isRead();

    /**
     * set default database name
     * @param database database name
     */
    void setDefaultDatabase(String database);

    /**
     * If the database name is specified in the sql, the name of the database being accessed can be obtained.
     * For example, the database name obtained by "dbname.table1" is "dbname".
     * @return Database name collection
     */
    Set<String> getDatabases();

    /**
     * get all table names accessed in sql.
     * @return table name collection
     */
    Set<String> getSimpleTables();

    /**
     * get all table accessed in sql.
     * @return table collection
     */
    Set<Table> getTables();

    /**
     * get the name of the table accessed in sql, excluding subqueries and join query tables.
     * @return table name collection
     */
    Set<String> getSimpleFromTables();

    /**
     * Retrieves the set of tables involved in the SQL query's FROM clause.
     *
     * This method returns a set of Table objects that are referenced in the FROM clause
     * of an SQL query, which is useful for query analysis and processing.
     *
     * @return Set<Table> A set of tables involved in the SQL query's FROM clause
     */
    Set<Table> getFromTables();


    /**
     * get the table name of the join query in sql.
     * @return table name collection
     */
    Set<String> getSimpleJoinTables();

    /**
     * Get the set of join tables.
     * This method retrieves a set of tables that are joined with the current table based on certain conditions.
     *
     * @return Set<Table> A set of joined tables
     */
    Set<Table> getJoinTables();


    /**
     * get the table name of the subquery in sql.
     * @return table name collection
     */
    Set<String> getSimpleSubTables();

    /**
     * Get the set of sub tables.
     * This method retrieves a set of tables that are considered sub-tables of the current table.
     *
     * @return Set<Table> A set of sub-tables
     */
    Set<Table> getSubTables();

    /**
     * get the table name of the insert in sql.
     * @return table name collection
     */
    Set<String> getSimpleInsertTables();

    /**
     * Get the set of insert tables.
     * This method retrieves a set of tables that are designated for insert operations.
     *
     * @return Set<Table> A set of tables for insert operations
     */
    Set<Table> getInsertTables();

    /**
     * get the table name of the update in sql.
     * @return table name collection
     */
    Set<String> getSimpleUpdateTables();

    /**
     * Get the set of update tables.
     * This method retrieves a set of tables that are designated for update operations.
     *
     * @return Set<Table> A set of tables for update operations
     */
    Set<Table> getUpdateTables();


    /**
     * get the table name of the delete in sql.
     * @return table name collection
     */
    Set<String> getSimpleDeleteTables();

    /**
     * Get the set of delete tables.
     * This method retrieves a set of tables that are designated for delete operations.
     *
     * @return Set<Table> A set of tables for delete operations
     */
    Set<Table> getDeleteTables();

    /**
     * Get the set of read tables.
     * This method retrieves a set of tables that are designated for read operations.
     *
     * @return Set<Table> A set of tables for read operations
     */
    Set<Table> getReadTables();


    /**
     * Get the set of write tables.
     * This method retrieves a set of tables that are designated for write operations.
     *
     * @return Set<Table> A set of tables for write operations
     */
    Set<Table> getWriteTables();

}
