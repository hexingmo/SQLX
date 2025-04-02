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

package com.github.sqlx.listener;

import com.github.sqlx.jdbc.CallableStatementInfo;
import com.github.sqlx.jdbc.ConnectionInfo;
import com.github.sqlx.jdbc.PreparedStatementInfo;
import com.github.sqlx.jdbc.ResultSetInfo;
import com.github.sqlx.jdbc.StatementInfo;

import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public interface EventListener {

    default void onBeforeRouting(RouteInfo routeInfo) {
    }

    default void onAfterRouting(RouteInfo routeInfo, Exception e) {
    }

    default void onBeforeGetConnection(ConnectionInfo connectionInfo) {

    }

    default void onAfterGetConnection(ConnectionInfo connectionInfo, SQLException e) {

    }

    default void onBeforeConnectionClose(ConnectionInfo connectionInfo) {

    }


    default void onAfterConnectionClose(ConnectionInfo connectionInfo, SQLException e) {
    }

    default void onBeforeSetAutoCommit(ConnectionInfo connectionInfo, boolean autoCommit, boolean oldAutoCommit) {

    }

    default void onAfterSetAutoCommit(ConnectionInfo connectionInfo, boolean autoCommit, boolean oldAutoCommit, SQLException e) {

    }

    default void onBeforeRollback(ConnectionInfo connectionInfo) {

    }

    default void onAfterRollback(ConnectionInfo connectionInfo, SQLException e) {

    }

    default void onBeforeSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint) {

    }

    default void onAfterSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint, SQLException e) {

    }


    default void onBeforeCommit(ConnectionInfo connectionInfo) {

    }

    default void onAfterCommit(ConnectionInfo connectionInfo, SQLException e) {

    }

    default void onBeforeCreateStatement(StatementInfo statementInfo) {

    }

    default void onAfterCreateStatement(StatementInfo statementInfo, SQLException e) {

    }

    default void onBeforeCloseStatement(StatementInfo statementInfo) {

    }

    default void onAfterCloseStatement(StatementInfo statementInfo, SQLException e) {

    }

    default void onBeforeExecuteQuery(StatementInfo statementInfo) {

    }

    default void onAfterExecuteQuery(StatementInfo statementInfo, SQLException e) {

    }

    default void onBeforeExecute(StatementInfo statementInfo) {

    }

    default void onAfterExecute(StatementInfo statementInfo, SQLException e) {

    }

    default void onBeforeExecuteUpdate(StatementInfo statementInfo) {

    }

    default void onAfterExecuteUpdate(StatementInfo statementInfo, SQLException e) {

    }


    default void onAfterGetResultSet(StatementInfo currentStatementInfo, long timeElapsedNanos, SQLException e) {

    }

    default void onBeforePrepareStatement(PreparedStatementInfo preparedStatementInfo) {

    }

    default void onAfterPrepareStatement(PreparedStatementInfo preparedStatementInfo, Exception e) {

    }

    default void onBeforeExecuteQuery(PreparedStatementInfo preparedStatementInfo) {

    }

    default void onAfterExecuteQuery(PreparedStatementInfo preparedStatementInfo, SQLException e) {

    }

    default void onBeforeExecuteUpdate(PreparedStatementInfo preparedStatementInfo) {

    }

    default void onAfterExecuteUpdate(PreparedStatementInfo preparedStatementInfo, SQLException e) {

    }

    default void onBeforeExecute(PreparedStatementInfo preparedStatementInfo) {

    }

    default void onAfterExecute(PreparedStatementInfo preparedStatementInfo, SQLException e) {

    }

    default void onBeforeAddBatch(PreparedStatementInfo preparedStatementInfo) {

    }

    default void onAfterAddBatch(PreparedStatementInfo preparedStatementInfo, long timeElapsedNanos, SQLException e) {

    }

    default void onBeforeClearBatch(PreparedStatementInfo preparedStatementInfo) {

    }

    default void onAfterClearBatch(PreparedStatementInfo preparedStatementInfo, long timeElapsedNanos, SQLException e) {

    }

    default void onBeforeExecuteBatch(PreparedStatementInfo preparedStatementInfo, long beforeTimeNs) {

    }

    default void onAfterExecuteBatch(PreparedStatementInfo preparedStatementInfo, long afterTimeNs, long timeElapsedNanos, long[] counts, SQLException e) {

    }

    default void onAfterPreparedStatementSet(PreparedStatementInfo statementInformation, int parameterIndex, Object value, SQLException e) {
    }

    default void onBeforeStatementClose(PreparedStatementInfo preparedStatementInfo) {

    }

    default void onAfterStatementClose(PreparedStatementInfo preparedStatementInfo, SQLException e) {

    }

    default void onBeforeResultSetNext(ResultSetInfo resultSetInfo) {

    }

    default void onAfterResultSetNext(ResultSetInfo resultSetInfo, long timeElapsedNanos, boolean next, SQLException e) {

    }

    default void onAfterResultSetClose(ResultSetInfo resultSetInfo, SQLException e) {

    }

    default void onAfterResultSetGet(ResultSetInfo resultSetInfo, String columnLabel, Object value, SQLException e) {
    }

    default void onAfterResultSetGet(ResultSetInfo resultSetInfo, int columnIndex, Object value, SQLException e) {
    }

    default void onBeforeCallableStatement(CallableStatementInfo statementInfo) {

    }

    default void onAfterCallStatement(CallableStatementInfo statementInfo, SQLException e) {

    }

    default void onBeforeStatementClose(CallableStatementInfo statementInfo) {

    }

    default void onAfterStatementClose(CallableStatementInfo statementInfo, SQLException e) {

    }

    default void onAfterCallableStatementSet(CallableStatementInfo callableStatementInfo, String parameterName, Object x, SQLException e) {

    }
}
