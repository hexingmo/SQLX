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

import com.github.sqlx.jdbc.ConnectionInfo;
import com.github.sqlx.jdbc.PreparedStatementInfo;
import com.github.sqlx.jdbc.ResultSetInfo;
import com.github.sqlx.jdbc.StatementInfo;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collections;
import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class CompositeEventListener implements EventListener {

    private final List<EventListener> eventListeners;

    public CompositeEventListener(List<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    public void addListener(EventListener listener) {
        eventListeners.add(listener);
    }

    public List<EventListener> getEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }

    @Override
    public void onBeforeRouting(RouteInfo routeInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeRouting(routeInfo);
        }
    }

    @Override
    public void onAfterRouting(RouteInfo routeInfo, Exception e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterRouting(routeInfo, e);
        }
    }

    @Override
    public void onBeforeGetConnection(ConnectionInfo connectionInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeGetConnection(connectionInfo);
        }
    }

    @Override
    public void onAfterGetConnection(ConnectionInfo connectionInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterGetConnection(connectionInfo , e);
        }
    }

    @Override
    public void onBeforeConnectionClose(ConnectionInfo connectionInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeConnectionClose(connectionInfo);
        }
    }

    @Override
    public void onAfterConnectionClose(ConnectionInfo connectionInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterConnectionClose(connectionInfo , e);
        }
    }

    @Override
    public void onBeforeSetAutoCommit(ConnectionInfo connectionInfo, boolean autoCommit, boolean oldAutoCommit) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeSetAutoCommit(connectionInfo , autoCommit , oldAutoCommit);
        }
    }

    @Override
    public void onAfterSetAutoCommit(ConnectionInfo connectionInfo, boolean autoCommit, boolean oldAutoCommit, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterSetAutoCommit(connectionInfo , autoCommit , oldAutoCommit , e);
        }
    }

    @Override
    public void onBeforeRollback(ConnectionInfo connectionInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeRollback(connectionInfo);
        }
    }

    @Override
    public void onAfterRollback(ConnectionInfo connectionInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterRollback(connectionInfo , e);
        }
    }

    @Override
    public void onBeforeSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeSavepointRollback(connectionInfo , savepoint);
        }
    }

    @Override
    public void onAfterSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterSavepointRollback(connectionInfo , savepoint , e);
        }
    }

    @Override
    public void onBeforeCommit(ConnectionInfo connectionInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeCommit(connectionInfo);
        }
    }

    @Override
    public void onAfterCommit(ConnectionInfo connectionInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterCommit(connectionInfo , e);
        }
    }

    @Override
    public void onBeforeCreateStatement(StatementInfo statementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeCreateStatement(statementInfo);
        }
    }

    @Override
    public void onAfterCreateStatement(StatementInfo statementInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterCreateStatement(statementInfo , e);
        }
    }

    @Override
    public void onBeforeCloseStatement(StatementInfo statementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeCloseStatement(statementInfo);
        }
    }

    @Override
    public void onAfterCloseStatement(StatementInfo statementInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterCloseStatement(statementInfo , e);
        }
    }

    @Override
    public void onBeforeExecuteQuery(StatementInfo statementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeExecuteQuery(statementInfo);
        }
    }

    @Override
    public void onAfterExecuteQuery(StatementInfo statementInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterExecuteQuery(statementInfo , e);
        }
    }

    @Override
    public void onBeforeExecuteUpdate(StatementInfo statementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeExecuteUpdate(statementInfo);
        }
    }

    @Override
    public void onAfterExecuteUpdate(StatementInfo statementInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterExecuteUpdate(statementInfo , e);
        }
    }

    @Override
    public void onAfterGetResultSet(StatementInfo currentStatementInfo, long timeElapsedNanos, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterGetResultSet(currentStatementInfo , timeElapsedNanos , e);
        }
    }

    @Override
    public void onBeforePrepareStatement(PreparedStatementInfo preparedStatementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforePrepareStatement(preparedStatementInfo);
        }
    }

    @Override
    public void onAfterPrepareStatement(PreparedStatementInfo preparedStatementInfo, Exception e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterPrepareStatement(preparedStatementInfo , e);
        }
    }

    @Override
    public void onBeforeExecuteQuery(PreparedStatementInfo preparedStatementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeExecuteQuery(preparedStatementInfo);
        }
    }

    @Override
    public void onAfterExecuteQuery(PreparedStatementInfo preparedStatementInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterExecuteQuery(preparedStatementInfo , e);
        }
    }

    @Override
    public void onBeforeExecuteUpdate(PreparedStatementInfo preparedStatementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeExecuteUpdate(preparedStatementInfo);
        }
    }

    @Override
    public void onAfterExecuteUpdate(PreparedStatementInfo preparedStatementInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterExecuteUpdate(preparedStatementInfo , e);
        }
    }

    @Override
    public void onBeforeAddBatch(PreparedStatementInfo preparedStatementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeAddBatch(preparedStatementInfo);
        }
    }

    @Override
    public void onAfterAddBatch(PreparedStatementInfo preparedStatementInfo, long timeElapsedNanos, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterAddBatch(preparedStatementInfo , timeElapsedNanos , e);
        }
    }

    @Override
    public void onBeforeClearBatch(PreparedStatementInfo preparedStatementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeClearBatch(preparedStatementInfo);
        }
    }

    @Override
    public void onAfterClearBatch(PreparedStatementInfo preparedStatementInfo, long timeElapsedNanos, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterClearBatch(preparedStatementInfo , timeElapsedNanos , e);
        }
    }

    @Override
    public void onBeforeExecuteBatch(PreparedStatementInfo preparedStatementInfo, long beforeTimeNs) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeExecuteBatch(preparedStatementInfo , beforeTimeNs);
        }
    }

    @Override
    public void onAfterExecuteBatch(PreparedStatementInfo preparedStatementInfo, long afterTimeNs, long timeElapsedNanos, long[] counts, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterExecuteBatch(preparedStatementInfo , afterTimeNs , timeElapsedNanos , counts,e);
        }
    }

    @Override
    public void onAfterPreparedStatementSet(PreparedStatementInfo statementInformation, int parameterIndex, Object value, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterPreparedStatementSet(statementInformation , parameterIndex , value , e);
        }
    }

    @Override
    public void onBeforeStatementClose(PreparedStatementInfo preparedStatementInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeStatementClose(preparedStatementInfo);
        }
    }

    @Override
    public void onAfterStatementClose(PreparedStatementInfo preparedStatementInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterStatementClose(preparedStatementInfo , e);
        }
    }

    @Override
    public void onBeforeResultSetNext(ResultSetInfo resultSetInfo) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onBeforeResultSetNext(resultSetInfo);
        }
    }

    @Override
    public void onAfterResultSetNext(ResultSetInfo resultSetInfo, long timeElapsedNanos, boolean next, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterResultSetNext(resultSetInfo , timeElapsedNanos , next, e);
        }
    }

    @Override
    public void onAfterResultSetClose(ResultSetInfo resultSetInfo, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterResultSetClose(resultSetInfo, e);
        }
    }

    @Override
    public void onAfterResultSetGet(ResultSetInfo resultSetInfo, String columnLabel, Object value, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterResultSetGet(resultSetInfo, columnLabel , value , e);
        }
    }

    @Override
    public void onAfterResultSetGet(ResultSetInfo resultSetInfo, int columnIndex, Object value, SQLException e) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onAfterResultSetGet(resultSetInfo, columnIndex , value , e);
        }
    }
}
