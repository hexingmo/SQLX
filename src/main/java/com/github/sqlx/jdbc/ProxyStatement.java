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

package com.github.sqlx.jdbc;


import com.github.sqlx.jdbc.datasource.SqlXDataSource;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.sql.SqlAttribute;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class ProxyStatement extends AbstractStatementAdapter {

    private final List<StatementInfo> statementInfoList = new ArrayList<>();

    private final SqlXDataSource dataSource;

    protected final EventListener eventListener;

    private Statement currentStatement;

    private StatementInfo currentStatementInfo;

    private boolean closed = false;

    private Integer maxFieldSize;

    private Integer maxRows;

    private Integer queryTimeout;

    private int fetchDirection = ResultSet.FETCH_FORWARD;

    private Integer fetchSize;

    private int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;

    private int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

    private int resultSetHoldability = ResultSet.CLOSE_CURSORS_AT_COMMIT;

    private Boolean poolable;

    private Boolean escapeProcessing;

    private String cursorName;


    public ProxyStatement(SqlXDataSource dataSource, Integer resultSetType, Integer resultSetConcurrency, Integer resultSetHoldability, EventListener eventListener) {
        this(dataSource, eventListener);
        if (resultSetType != null) {
            this.resultSetType = resultSetType;
        }

        if (resultSetConcurrency != null) {
            this.resultSetConcurrency = resultSetConcurrency;
        }

        if (resultSetHoldability != null) {
            this.resultSetHoldability = resultSetHoldability;
        }
    }




    public ProxyStatement(SqlXDataSource dataSource, EventListener eventListener) {
        this.dataSource = dataSource;
        this.eventListener = eventListener;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            eventListener.onBeforeExecuteQuery(statementInfo);
            ResultSet rs = statement.executeQuery(statementInfo.getNativeSql());
            ResultSetInfo resultSetInfo = new ResultSetInfo();
            resultSetInfo.setResultSet(rs);
            resultSetInfo.setStatementInfo(statementInfo);
            return new ResultSetWrapper(rs , resultSetInfo ,eventListener);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.addException(e);
            }
            eventListener.onAfterExecuteQuery(statementInfo , e);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        int rows = 0;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            eventListener.onBeforeExecuteUpdate(statementInfo);
            rows = statement.executeUpdate(statementInfo.getNativeSql());
            return rows;
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.setUpdatedRows(rows);
                statementInfo.addException(e);
            }
            eventListener.onAfterExecuteUpdate(statementInfo , e);
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            if (eventListener != null) {
                eventListener.onBeforeExecuteUpdate(statementInfo);
            }
            return statement.executeUpdate(statementInfo.getNativeSql() , autoGeneratedKeys);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.addException(e);
            }
            if (eventListener != null) {
                eventListener.onAfterExecuteUpdate(statementInfo , e);
            }
        }
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            eventListener.onBeforeExecuteUpdate(statementInfo);
            return statement.executeUpdate(statementInfo.getNativeSql() , columnIndexes);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.addException(e);
            }
            eventListener.onAfterExecuteUpdate(statementInfo , e);
        }
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            if (eventListener != null) {
                eventListener.onBeforeExecuteQuery(statementInfo);
            }
            return statement.executeUpdate(statementInfo.getNativeSql() , columnNames);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.addException(e);
            }
            eventListener.onAfterExecuteQuery(statementInfo , e);
        }
    }


    @Override
    public boolean execute(String sql) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            if (eventListener != null) {
                eventListener.onBeforeExecute(statementInfo);
            }
            return statement.execute(statementInfo.getNativeSql());
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.addException(e);
            }
            eventListener.onAfterExecute(statementInfo , e);
        }
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            if (eventListener != null) {
                eventListener.onBeforeExecute(statementInfo);
            }
            return statement.execute(statementInfo.getNativeSql() , autoGeneratedKeys);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.addException(e);
            }
            eventListener.onAfterExecute(statementInfo , e);
        }
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            statementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            if (eventListener != null) {
                eventListener.onBeforeExecute(statementInfo);
            }
            return statement.execute(statementInfo.getNativeSql() , columnIndexes);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
                statementInfo.addException(e);
            }
            eventListener.onAfterExecute(statementInfo , e);
        }
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        SQLException e = null;
        StatementInfo statementInfo = null;
        try {
            statementInfo = acquireStatement(sql);
            Statement statement = statementInfo.getStatement();
            statementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            if (eventListener != null) {
                eventListener.onBeforeExecute(statementInfo);
            }
            return statement.execute(statementInfo.getNativeSql() , columnNames);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            if (statementInfo != null) {
                statementInfo.setAfterTimeToExecuteNs(System.nanoTime());
                statementInfo.addException(e);
            }
            eventListener.onAfterExecute(statementInfo , e);
        }
    }

    @Override
    public synchronized void close() throws SQLException {
        if (closed) {
            return;
        }

        for (StatementInfo statementInfo : statementInfoList) {
            closeStatement(statementInfo);
            Connection connection = statementInfo.getConnectionInfo().getConnection();
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        this.closed = true;
    }

    @Override
    public synchronized int getMaxFieldSize() throws SQLException {
        return maxFieldSize;
    }

    @Override
    public synchronized void setMaxFieldSize(int max) throws SQLException {
        this.maxFieldSize = max;
        if (Objects.nonNull(currentStatement)) {
            currentStatement.setMaxFieldSize(max);
        }
    }

    @Override
    public synchronized int getMaxRows() throws SQLException {
        return maxRows;
    }

    @Override
    public synchronized void setMaxRows(int max) throws SQLException {
        this.maxRows = max;
        if (Objects.nonNull(currentStatement)) {
            currentStatement.setMaxRows(max);
        }
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        if (Objects.nonNull(currentStatement)) {
            currentStatement.setEscapeProcessing(enable);
        }
        this.escapeProcessing = enable;
    }

    @Override
    public synchronized int getQueryTimeout() throws SQLException {
        return this.queryTimeout;
    }

    @Override
    public synchronized void setQueryTimeout(int seconds) throws SQLException {
        this.queryTimeout = seconds;
        if (Objects.nonNull(currentStatement)) {
            currentStatement.setQueryTimeout(seconds);
        }
    }

    @Override
    public void cancel() throws SQLException {
        if (Objects.nonNull(currentStatement)) {
            currentStatement.cancel();
        }
    }


    @Override
    public ResultSet getResultSet() throws SQLException {

        if (currentStatement == null) {
            return null;
        }
        SQLException e = null;
        long start = System.nanoTime();
        try {
            ResultSet resultSet = currentStatement.getResultSet();
            ResultSetInfo resultSetInfo = new ResultSetInfo();
            resultSetInfo.setResultSet(resultSet);
            resultSetInfo.setStatementInfo(currentStatementInfo);
            return new ResultSetWrapper(resultSet ,resultSetInfo , eventListener);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            currentStatementInfo.addException(e);
            eventListener.onAfterGetResultSet(currentStatementInfo, System.nanoTime() - start, e);
        }
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return Objects.nonNull(currentStatement) ? currentStatement.getUpdateCount() : 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return Objects.nonNull(currentStatement) && currentStatement.getMoreResults();
    }

    @Override
    public synchronized void setFetchDirection(int direction) throws SQLException {
        if (Objects.nonNull(currentStatement)) {
            currentStatement.setFetchDirection(direction);
        }
        this.fetchDirection = direction;
    }

    @Override
    public synchronized int getFetchDirection() throws SQLException {
        return this.fetchDirection;
    }

    @Override
    public synchronized void setFetchSize(int rows) throws SQLException {
        this.fetchSize = rows;
        if (Objects.nonNull(currentStatement)) {
            currentStatement.setFetchSize(rows);
        }
    }

    @Override
    public synchronized int getFetchSize() throws SQLException {
        return this.fetchSize;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return Objects.nonNull(currentStatement) ? currentStatement.getResultSetConcurrency() : resultSetConcurrency;
    }

    @Override
    public synchronized int getResultSetType() throws SQLException {
        return Objects.nonNull(currentStatement) ? currentStatement.getResultSetType() : resultSetType;
    }

    public synchronized void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // TODO 返回一个新的还是返回现有的 connection ？
        return dataSource.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return Objects.nonNull(currentStatement) && currentStatement.getMoreResults();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return Objects.nonNull(currentStatement) ? currentStatement.getGeneratedKeys() : null;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return Objects.nonNull(currentStatement) ? currentStatement.getResultSetHoldability() : resultSetHoldability;
    }

    @Override
    public synchronized boolean isClosed() throws SQLException {
        return this.closed;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return this.poolable;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        this.poolable = poolable;
        if (currentStatement != null) {
            currentStatement.setPoolable(poolable);
        }
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        this.cursorName = name;
        if (currentStatement != null) {
            currentStatement.setCursorName(cursorName);
        }
    }


    @Override
    public String toString() {
        return currentStatement.toString();
    }

    private synchronized StatementInfo acquireStatement(String sql) throws SQLException {

        // TODO 有可能造成连接泄露？
        ProxyConnection proxyConnection = (ProxyConnection) getConnection();
        ConnectionInfo connectionInfo = proxyConnection.getConnectionInfo();
        RoutedConnection routedConnection = proxyConnection.getConnection(sql);
        Connection connection = routedConnection.getConnection();
        StatementInfo statementInfo = new StatementInfo();
        statementInfo.setRouteInfo(routedConnection.getRoutedDataSource().getRouteInfo());
        connectionInfo.addStatementInfo(statementInfo);
        statementInfo.setConnectionInfo(connectionInfo);
        statementInfo.setSql(sql);
        SqlAttribute sqlAttribute = routedConnection.getRoutedDataSource().getRouteInfo().getSqlAttribute();
        if (sqlAttribute != null) {
            statementInfo.setNativeSql(sqlAttribute.getNativeSql());
        } else {
            statementInfo.setNativeSql(sql);
        }

        statementInfo.setBeforeTimeToCreateStatementNs(System.nanoTime());
        statementInfo.setBeforeTimeToCreateStatementMillis(System.currentTimeMillis());
        eventListener.onBeforeCreateStatement(statementInfo);
        Statement actualStatement;
        SQLException e = null;
        try {
            actualStatement = connection.createStatement();
            statementInfo.setStatement(actualStatement);
            actualStatement = routedConnection.getConnection().createStatement(resultSetType , resultSetConcurrency , resultSetHoldability);
            this.currentStatement = actualStatement;
            this.currentStatementInfo = statementInfo;
            if (this.fetchSize != null) {
                actualStatement.setFetchSize(this.fetchSize);
            }
            actualStatement.setFetchDirection(this.fetchDirection);
            if (this.maxFieldSize != null) {
                actualStatement.setMaxFieldSize(maxFieldSize);
            }
            if (this.maxRows != null) {
                actualStatement.setMaxRows(maxRows);
            }
            if (this.queryTimeout != null) {
                actualStatement.setQueryTimeout(this.queryTimeout);
            }
            if (this.poolable != null) {
                actualStatement.setPoolable(poolable);
            }
            if (this.escapeProcessing != null) {
                actualStatement.setEscapeProcessing(escapeProcessing);
            }
            if (this.cursorName != null) {
                actualStatement.setCursorName(cursorName);
            }
            statementInfoList.add(statementInfo);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            statementInfo.addException(e);
            statementInfo.setAfterTimeToCreateStatementNs(System.nanoTime());
            statementInfo.setAfterTimeToCreateStatementMillis(System.currentTimeMillis());
            eventListener.onAfterCreateStatement(statementInfo , e);
        }
        return statementInfo;
    }

    private void closeStatement(StatementInfo statementInfo) throws SQLException {

        Statement statement = statementInfo.getStatement();
        if (statement.isClosed()) {
            return;
        }
        SQLException e = null;
        try {
            statementInfo.setBeforeTimeToCloseNs(System.nanoTime());
            statementInfo.setBeforeTimeToCloseMillis(System.currentTimeMillis());
            eventListener.onBeforeCloseStatement(statementInfo);
            statementInfo.getStatement().close();
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            statementInfo.setAfterTimeToCloseNs(System.nanoTime());
            statementInfo.setAfterTimeToCloseMillis(System.currentTimeMillis());
            statementInfo.addException(e);
            eventListener.onAfterCloseStatement(statementInfo , e);
        }
    }

    private boolean isValidResultSetType(int resultSetType) {
        return resultSetType == ResultSet.TYPE_FORWARD_ONLY ||
                resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE ||
                resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE;
    }

    private boolean isValidResultSetConcurrency(int resultSetConcurrency) {
        return resultSetConcurrency == ResultSet.CONCUR_READ_ONLY ||
                resultSetConcurrency == ResultSet.CONCUR_UPDATABLE;
    }

    private boolean isValidResultSetHoldability(int resultSetHoldability) {
        return resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT ||
                resultSetHoldability == ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

}
