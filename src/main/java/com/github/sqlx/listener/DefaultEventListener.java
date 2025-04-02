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
import com.github.sqlx.jdbc.PreparedStatementInfo;
import com.github.sqlx.jdbc.ResultSetInfo;
import com.github.sqlx.jdbc.StatementInfo;

import java.sql.SQLException;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class DefaultEventListener implements EventListener {

    @Override
    public void onAfterExecuteQuery(StatementInfo statementInfo, SQLException e) {
        statementInfo.incrementTimeElapsed(statementInfo.getTimeElapsedExecuteNs());
    }

    @Override
    public void onAfterExecuteUpdate(StatementInfo statementInfo, SQLException e) {
        statementInfo.incrementTimeElapsed(statementInfo.getTimeElapsedExecuteNs());
        statementInfo.incrementUpdatedRows(statementInfo.getUpdatedRows());
    }

    @Override
    public void onAfterExecute(StatementInfo statementInfo, SQLException e) {
        statementInfo.incrementTimeElapsed(statementInfo.getTimeElapsedExecuteNs());
        statementInfo.incrementUpdatedRows(statementInfo.getUpdatedRows());
    }

    @Override
    public void onAfterPreparedStatementSet(PreparedStatementInfo preparedStatementInfo, int parameterIndex, Object value, SQLException e) {
        preparedStatementInfo.setParameterValue(parameterIndex, value);
    }

    @Override
    public void onAfterExecute(PreparedStatementInfo statementInfo, SQLException e) {
        statementInfo.incrementTimeElapsed(statementInfo.getTimeElapsedExecuteNs());
        statementInfo.incrementUpdatedRows(statementInfo.getUpdatedRows());
    }

    @Override
    public void onAfterExecuteUpdate(PreparedStatementInfo statementInfo, SQLException e) {
        statementInfo.incrementTimeElapsed(statementInfo.getTimeElapsedExecuteNs());
        statementInfo.incrementUpdatedRows(statementInfo.getUpdatedRows());
    }

    @Override
    public void onAfterExecuteQuery(PreparedStatementInfo statementInfo, SQLException e) {
        statementInfo.incrementTimeElapsed(statementInfo.getTimeElapsedExecuteNs());
    }

    @Override
    public void onAfterExecuteBatch(PreparedStatementInfo statementInfo, long afterTimeNs, long timeElapsedNanos, long[] counts, SQLException e) {
        statementInfo.incrementTimeElapsed(timeElapsedNanos);
    }

    @Override
    public void onAfterCallableStatementSet(CallableStatementInfo statementInformation, String parameterName, Object value, SQLException e) {
        statementInformation.setParameterValue(parameterName, value);
    }

    @Override
    public void onAfterGetResultSet(StatementInfo statementInfo, long timeElapsedNanos, SQLException e) {
        statementInfo.incrementTimeElapsed(timeElapsedNanos);
    }

    @Override
    public void onAfterResultSetNext(ResultSetInfo resultSetInfo, long timeElapsedNanos, boolean hasNext, SQLException e) {
        resultSetInfo.getStatementInfo().incrementTimeElapsed(timeElapsedNanos);
    }
}
