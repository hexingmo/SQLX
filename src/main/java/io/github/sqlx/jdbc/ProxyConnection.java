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

package io.github.sqlx.jdbc;


import io.github.sqlx.jdbc.datasource.SqlXDataSource;
import io.github.sqlx.jdbc.datasource.RoutedDataSource;
import io.github.sqlx.rule.RouteInfo;
import io.github.sqlx.listener.EventListener;
import io.github.sqlx.util.MapUtils;
import io.github.sqlx.util.RoutingUtils;
import io.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class ProxyConnection extends AbstractConnectionAdapter {

    private final List<Statement> openStatements = Collections.synchronizedList(new LinkedList<>());

    private Connection physicalConnection;

    private volatile boolean autoCommit = true;

    private volatile boolean readOnly = false;

    private volatile Integer isolation;

    private volatile String schema;

    private volatile Integer holdability;

    private String catalog;

    private Executor executor;

    private Integer networkTimeout;

    private final Map<String , String> clientInfoMap = new HashMap<>();

    private Map<String, Class<?>> typeMap = new HashMap<>();

    private final List<RouteInfo> routeInfoList = new ArrayList<>();

    private final ConnectionInfo connectionInfo = new ConnectionInfo();

    private Properties clientInfo;

    private String username;

    private String password;

    private final DatabaseMetaDataWrapper databaseMetaData;

    private final SqlXDataSource sqlXDataSource;

    private EventListener eventListener;

    public ProxyConnection(SqlXDataSource sqlXDataSource, EventListener eventListener , String username , String password) {
        this(sqlXDataSource , eventListener);
        this.username = username;
        this.password = password;
    }

    public ProxyConnection(SqlXDataSource sqlXDataSource, EventListener eventListener) {
        this(sqlXDataSource);
        this.eventListener = eventListener;
    }

    public ProxyConnection(SqlXDataSource sqlXDataSource) {
        this.sqlXDataSource = sqlXDataSource;
        this.connectionInfo.setRouteInfoList(routeInfoList);
        this.connectionInfo.setConnection(this);
        this.databaseMetaData = new DatabaseMetaDataWrapper(null , this);
    }

    @Override
    public Statement createStatement() throws SQLException {
        ProxyStatement statement = new ProxyStatement(sqlXDataSource, null, null, null, eventListener);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        ProxyStatement statement = new ProxyStatement(sqlXDataSource, resultSetType, resultSetConcurrency, null, eventListener);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        ProxyStatement statement = new ProxyStatement(sqlXDataSource, resultSetType, resultSetConcurrency, resultSetHoldability, eventListener);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement statement = createPreparedStatement(sql);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        PreparedStatement statement = createPreparedStatement(sql, resultSetType, resultSetConcurrency);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        PreparedStatement statement = createPreparedStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        PreparedStatement statement = createPreparedStatement(sql, autoGeneratedKeys);
        openStatements.add(statement);
        return statement;
    }

    @SuppressWarnings("all")
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        PreparedStatement statement = createPreparedStatement(sql, new Object[]{columnIndexes});
        openStatements.add(statement);
        return statement;
    }

    @SuppressWarnings("all")
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        PreparedStatement statement = createPreparedStatement(sql, new Object[]{columnNames});
        openStatements.add(statement);
        return statement;
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        CallableStatement statement = prepareCallableStatement(sql);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        CallableStatement statement = prepareCallableStatement(sql, resultSetType, resultSetConcurrency);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        CallableStatement statement = prepareCallableStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        openStatements.add(statement);
        return statement;
    }

    @Override
    public synchronized void setAutoCommit(boolean autoCommit) throws SQLException {

        SQLException e = null;
        boolean oldAutoCommit = this.autoCommit;
        try {
            eventListener.onBeforeSetAutoCommit(connectionInfo, autoCommit, oldAutoCommit);
            this.autoCommit = autoCommit;
            if (Objects.nonNull(physicalConnection)) {
                physicalConnection.setAutoCommit(autoCommit);
            }
        } catch (SQLException sqle){
            e = sqle;
            throw e;
        } finally {
            eventListener.onAfterSetAutoCommit(connectionInfo, autoCommit, oldAutoCommit, e);
        }
    }

    @Override
    public synchronized boolean getAutoCommit() throws SQLException {
        return Objects.nonNull(physicalConnection) ? physicalConnection.getAutoCommit() : this.autoCommit;
    }

    @Override
    public Map<String,Class<?>> getTypeMap() throws SQLException {
        return this.typeMap;
    }

    @Override
    public void setTypeMap(Map<String,Class<?>> typeMap) throws SQLException {
        this.typeMap = typeMap;
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setTypeMap(typeMap);
        }
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        this.executor = executor;
        this.networkTimeout = milliseconds;
    }


    @Override
    public int getNetworkTimeout() throws SQLException {
        if (Objects.nonNull(physicalConnection)) {
            return physicalConnection.getNetworkTimeout();
        }
        if (Objects.nonNull(networkTimeout)) {
            return networkTimeout;
        }
        return 0;
    }


    @Override
    public void commit() throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to commit.");
        }
        SQLException e = null;
        try {
            connectionInfo.setBeforeTimeToCommitNs(System.nanoTime());
            connectionInfo.setBeforeTimeToCommitMillis(System.currentTimeMillis());
            eventListener.onBeforeCommit(connectionInfo);
            physicalConnection.commit();
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            connectionInfo.setAfterTimeToCommitNs(System.nanoTime());
            connectionInfo.setAfterTimeToCommitMillis(System.currentTimeMillis());
            eventListener.onAfterCommit(connectionInfo, e);
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to rollback.");
        }
        SQLException e = null;
        try {
            connectionInfo.setBeforeTimeToRollbackNs(System.nanoTime());
            connectionInfo.setBeforeTimeToRollbackMillis(System.currentTimeMillis());
            eventListener.onBeforeRollback(connectionInfo);
            physicalConnection.rollback();
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            connectionInfo.setAfterTimeToRollbackNs(System.nanoTime());
            connectionInfo.setAfterTimeToRollbackMillis(System.currentTimeMillis());
            eventListener.onAfterRollback(connectionInfo, e);
        }
    }

    @Override
    public synchronized void close() throws SQLException {
        if (physicalConnection == null) {
            for (Statement openStatement : openStatements) {
                openStatement.close();
            }
            return;
        }
        SQLException e = null;
        try {
            connectionInfo.setBeforeTimeToCloseConnectionNs(System.nanoTime());
            connectionInfo.setBeforeTimeToCloseConnectionMillis(System.currentTimeMillis());
            eventListener.onBeforeConnectionClose(connectionInfo);
            physicalConnection.close();
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            connectionInfo.setAfterTimeToCloseConnectionNs(System.nanoTime());
            connectionInfo.setAfterTimeToCloseConnectionMillis(System.currentTimeMillis());
            eventListener.onAfterConnectionClose(connectionInfo , e);
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        if (Objects.nonNull(physicalConnection)) {
            return physicalConnection.isClosed();
        }
        return false;
    }

    @Override
    public synchronized DatabaseMetaData getMetaData() throws SQLException {
        if (this.physicalConnection != null) {
            if (this.databaseMetaData.getDelegate() == null) {
                this.databaseMetaData.setDelegate(physicalConnection.getMetaData());
            }
        } else {
            RoutedDataSource routedDataSource = sqlXDataSource.getDataSourceForDatabaseMetaData();
            RouteInfo routeInfo = routedDataSource.getRouteInfo();
            this.connectionInfo.setCurrentRouteInfo(routeInfo);
            this.routeInfoList.add(routeInfo);
            this.physicalConnection = acquireConnection(routedDataSource);
            DatabaseMetaData metaData = physicalConnection.getMetaData();
            this.databaseMetaData.setDelegate(metaData);
        }
        return databaseMetaData;
    }

    @Override
    public synchronized void setReadOnly(boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setReadOnly(readOnly);
        }
    }

    @Override
    public synchronized boolean isReadOnly() throws SQLException {
        return Objects.nonNull(physicalConnection) ? physicalConnection.isReadOnly() : this.readOnly;
    }

    @Override
    public synchronized void setTransactionIsolation(int level) throws SQLException {
        this.isolation = level;
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setTransactionIsolation(level);
        }
    }

    @Override
    public synchronized int getTransactionIsolation() throws SQLException {
        if (Objects.nonNull(physicalConnection)) {
            return physicalConnection.getTransactionIsolation();
        }
        if (Objects.nonNull(isolation)) {
            return isolation;
        }
        return TRANSACTION_READ_COMMITTED;
    }


    public ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to setSavepoint.");
        }
        return physicalConnection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to setSavepoint.");
        }
        return physicalConnection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to rollback.");
        }
        SQLException e = null;
        try {
            connectionInfo.setBeforeTimeToRollbackNs(System.nanoTime());
            connectionInfo.setBeforeTimeToRollbackMillis(System.currentTimeMillis());
            eventListener.onBeforeSavepointRollback(connectionInfo , savepoint);
            physicalConnection.rollback(savepoint);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            connectionInfo.setAfterTimeToRollbackNs(System.nanoTime());
            connectionInfo.setAfterTimeToRollbackMillis(System.currentTimeMillis());
            eventListener.onAfterSavepointRollback(connectionInfo, savepoint , e);
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to release Savepoint.");
        }
        physicalConnection.releaseSavepoint(savepoint);
    }

    @Override
    public Clob createClob() throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to create Clob.");
        }
        return physicalConnection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to create Blob.");
        }
        return physicalConnection.createBlob();
    }

    @Override
    public synchronized NClob createNClob() throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to create NClob.");
        }
        return physicalConnection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to create SQLXML.");
        }
        return physicalConnection.createSQLXML();
    }


    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to create ArrayOf.");
        }
        return physicalConnection.createArrayOf(typeName , elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        if (Objects.isNull(physicalConnection)) {
            throw new SQLException("Physical connection is not initialized. Unable to create Struct.");
        }
        return physicalConnection.createStruct(typeName , attributes);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        if (Objects.nonNull(physicalConnection)) {
            return physicalConnection.isValid(timeout);
        }
        return true;
    }

    @Override
    public synchronized void setClientInfo(String name, String value) throws SQLClientInfoException {
        clientInfoMap.put(name , value);
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setClientInfo(name , value);
        }
    }

    @Override
    public synchronized void setClientInfo(Properties properties) throws SQLClientInfoException {
        this.clientInfo = properties;
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setClientInfo(properties);
        }
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return Objects.nonNull(physicalConnection) ? physicalConnection.getClientInfo(name) : null;
    }

    @Override
    public synchronized Properties getClientInfo() throws SQLException {
        return Objects.nonNull(physicalConnection) ? physicalConnection.getClientInfo() : new Properties();
    }


    @Override
    public synchronized void setSchema(String schema) throws SQLException {
        this.schema = schema;
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setSchema(schema);
        }
    }

    @Override
    public synchronized String getSchema() throws SQLException {
        return Objects.nonNull(physicalConnection) ? physicalConnection.getSchema() : this.schema;
    }

    @Override
    public synchronized void setHoldability(int holdability) throws SQLException {
        this.holdability = holdability;
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setHoldability(holdability);
        }
    }

    @Override
    public synchronized int getHoldability() throws SQLException {
        if (Objects.nonNull(physicalConnection)) {
            return physicalConnection.getHoldability();
        }
        if (Objects.nonNull(holdability)) {
            return holdability;
        }
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return physicalConnection != null ? physicalConnection.getWarnings() : null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        if (physicalConnection != null) {
            physicalConnection.clearWarnings();
        }
    }

    @Override
    public String getCatalog() throws SQLException {
        return catalog;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        this.catalog = catalog;
        if (Objects.nonNull(physicalConnection)) {
            physicalConnection.setCatalog(catalog);
        }
    }

    /**
     * Retrieves a routed connection based on the provided SQL statement.
     * This method performs several key operations:
     * 1. Obtains the appropriate RoutedDataSource based on the provided SQL.
     * 2. Checks if a physical connection already exists.
     * 3. If a physical connection exists, it updates the route information, transaction details, and adds the route info to the list.
     * 4. If no physical connection exists, it retrieves the route information, updates the connection details, and acquires a new physical connection.
     * 5. Sets the default database for the physical connection using the route information.
     * 6. Returns a new RoutedConnection object containing the routed data source and the physical connection.
     *
     * @param sql the SQL statement used to determine the appropriate data source and route information
     * @return a RoutedConnection object containing the routed data source and the physical connection
     * @throws SQLException if a database access error occurs or the data source is invalid
     */
    public synchronized RoutedConnection getConnection(String sql) throws SQLException {
        RoutedDataSource routedDataSource = sqlXDataSource.getDataSource(sql);
        if (Objects.nonNull(physicalConnection)) {
            RouteInfo routeInfo = routedDataSource.getRouteInfo();
            RoutingUtils.setDefaultDatabase(physicalConnection.getCatalog() , routeInfo);
            this.connectionInfo.setCurrentRouteInfo(routeInfo);
            this.connectionInfo.setTransactionId(routeInfo.getTransactionId());
            this.connectionInfo.setTransactionName(routeInfo.getTransactionName());
            this.routeInfoList.add(routeInfo);
            return new RoutedConnection(routedDataSource , this.physicalConnection);
        }

        RouteInfo routeInfo = routedDataSource.getRouteInfo();
        this.connectionInfo.setCurrentRouteInfo(routeInfo);
        this.connectionInfo.setTransactionId(routeInfo.getTransactionId());
        this.connectionInfo.setTransactionName(routeInfo.getTransactionName());
        this.routeInfoList.add(routeInfo);
        Connection connection = acquireConnection(routedDataSource);
        RoutingUtils.setDefaultDatabase(connection.getCatalog() , routeInfo);
        return new RoutedConnection(routedDataSource , connection);
    }

    public Connection getPhysicalConnection() {
        return this.physicalConnection;
    }

    /**
     * Acquires a database connection from the provided DataSource.
     * This method performs several key operations:
     * 1. Records the start time for connection acquisition.
     * 2. Notifies the event listener before attempting to get a connection.
     * 3. Attempts to obtain a connection from the DataSource.
     * 4. Sets the delegate of the databaseMetaData to the metadata of the acquired connection.
     * 5. Sets various connection properties such as auto-commit, read-only, transaction isolation, schema, holdability, client info, and catalog.
     * 6. Records the end time for connection acquisition.
     * 7. Notifies the event listener after attempting to get a connection, including any exceptions that occurred.
     *
     * @param dataSource the DataSource from which to acquire the connection
     * @return the acquired Connection object
     * @throws SQLException if a database access error occurs or the DataSource is invalid
     */
    private synchronized Connection acquireConnection(DataSource dataSource) throws SQLException {

        SQLException e = null;
        try {
            connectionInfo.setBeforeTimeToGetConnectionNs(System.nanoTime());
            connectionInfo.setBeforeTimeToGetConnectionMillis(System.currentTimeMillis());
            eventListener.onBeforeGetConnection(connectionInfo);
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                this.physicalConnection = dataSource.getConnection(username , password);
            } else {
                this.physicalConnection = dataSource.getConnection();
            }
            this.databaseMetaData.setDelegate(this.physicalConnection.getMetaData());
            connectionPropertiesSet();
            return this.physicalConnection;
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            connectionInfo.setAfterTimeToGetConnectionNs(System.nanoTime());
            connectionInfo.setAfterTimeToGetConnectionMillis(System.currentTimeMillis());
            eventListener.onAfterGetConnection(connectionInfo , e);
        }
    }

    private void connectionPropertiesSet() throws SQLException {
        this.physicalConnection.setAutoCommit(this.autoCommit);
        this.physicalConnection.setReadOnly(this.readOnly);
        if (this.schema != null) {
            this.physicalConnection.setSchema(this.schema);
        }
        if (holdability != null) {
            this.physicalConnection.setHoldability(holdability);
        }
        if (isolation != null) {
            this.physicalConnection.setTransactionIsolation(isolation);
        }
        if (this.clientInfo != null) {
            this.physicalConnection.setClientInfo(clientInfo);
        }
        for (Map.Entry<String, String> entry : this.clientInfoMap.entrySet()) {
            this.physicalConnection.setClientInfo(entry.getKey() , entry.getValue());
        }
        if (StringUtils.isNotBlank(catalog)) {
            this.physicalConnection.setCatalog(catalog);
        }
        if (MapUtils.isNotEmpty(typeMap)) {
            this.physicalConnection.setTypeMap(typeMap);
        }
        if (this.executor != null && this.networkTimeout != null) {
            this.physicalConnection.setNetworkTimeout(executor , networkTimeout);
        }
    }

    /**
     * Creates a PreparedStatement with the given SQL and arguments.
     * This method retrieves a routed connection based on the provided SQL,
     * sets up necessary information for event listeners, and prepares the statement
     * with the provided arguments. It handles different types of arguments to
     * accommodate various PreparedStatement creation methods.
     *
     * @param sql  the SQL statement to be pre-compiled
     * @param args the arguments for the PreparedStatement
     * @return a new default PreparedStatement object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    private PreparedStatement createPreparedStatement(String sql, Object... args) throws SQLException {
        PreparedStatementInfo preparedStatementInfo = new PreparedStatementInfo();
        connectionInfo.addStatementInfo(preparedStatementInfo);
        preparedStatementInfo.setConnectionInfo(connectionInfo);
        Exception e = null;
        try {
            preparedStatementInfo.setSql(sql);
            preparedStatementInfo.setBeforeTimeToCreateStatementNs(System.nanoTime());
            preparedStatementInfo.setBeforeTimeToCreateStatementMillis(System.currentTimeMillis());
            RoutedConnection routedConnection = getConnection(sql);
            preparedStatementInfo.setRouteInfo(routedConnection.getRoutedDataSource().getRouteInfo());
            eventListener.onBeforePrepareStatement(preparedStatementInfo);
            Connection connection = routedConnection.getConnection();
            String nativeSql = routedConnection.getNativeSql();
            PreparedStatement ps = createPreparedStatementWithArgs(connection, nativeSql, args);
            preparedStatementInfo.setNativeSql(nativeSql);
            preparedStatementInfo.setStatement(ps);
            return new ProxyPreparedStatement(this.sqlXDataSource, preparedStatementInfo, this.eventListener);
        } catch (Exception ex) {
            e = ex;
            throw ex;
        } finally {
            preparedStatementInfo.addException(e);
            preparedStatementInfo.setAfterTimeToCreateStatementNs(System.nanoTime());
            preparedStatementInfo.setAfterTimeToCreateStatementMillis(System.currentTimeMillis());
            eventListener.onAfterPrepareStatement(preparedStatementInfo, e);
        }
    }

    /**
     * Creates a PreparedStatement with the given connection, SQL, and arguments.
     * This method handles different types of arguments to accommodate various
     * PreparedStatement creation methods, such as specifying result set type and concurrency,
     * auto-generated keys, column indexes, and column names.
     *
     * @param connection the connection to the database
     * @param nativeSql  the SQL statement to be pre-compiled
     * @param args       the arguments for the PreparedStatement
     * @return a new default PreparedStatement object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    @SuppressWarnings("all")
    private PreparedStatement createPreparedStatementWithArgs(Connection connection, String nativeSql, Object... args) throws SQLException {
        if (args.length == 0) {
            return connection.prepareStatement(nativeSql);
        } else if (args.length == 2 && args[0] instanceof Integer && args[1] instanceof Integer) {
            // PreparedStatement.prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            int resultSetType = (int) args[0];
            int resultSetConcurrency = (int) args[1];
            return connection.prepareStatement(nativeSql, resultSetType, resultSetConcurrency);
        } else if (args.length == 3 && args[0] instanceof Integer && args[1] instanceof Integer && args[2] instanceof Integer) {
            // PreparedStatement.prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            int resultSetType = (int) args[0];
            int resultSetConcurrency = (int) args[1];
            int resultSetHoldability = (int) args[2];
            return connection.prepareStatement(nativeSql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } else if (args.length == 1 && args[0] instanceof Integer) {
            // PreparedStatement.prepareStatement(String sql, int autoGeneratedKeys)
            int autoGeneratedKeys = (int) args[0];
            return connection.prepareStatement(nativeSql, autoGeneratedKeys);
        } else if (args.length == 1 && args[0] instanceof int[]) {
            // PreparedStatement.prepareStatement(String sql, int[] columnIndexes)
            int[] columnIndexes = (int[]) args[0];
            return connection.prepareStatement(nativeSql, columnIndexes);
        } else if (args.length == 1 && args[0] instanceof String[]) {
            // PreparedStatement.prepareStatement(String sql, String[] columnNames)
            String[] columnNames = (String[]) args[0];
            return connection.prepareStatement(nativeSql, columnNames);
        } else {
            throw new SQLException("Unsupported arguments for prepareStatement");
        }
    }

    /**
     * Prepares a CallableStatement with the given SQL and arguments.
     * This method retrieves a routed connection based on the provided SQL,
     * sets up necessary information for event listeners, and prepares the callable statement
     * with the provided arguments. It handles different types of arguments to
     * accommodate various CallableStatement creation methods.
     *
     * @param sql  the SQL statement to be pre-compiled
     * @param args the arguments for the CallableStatement
     * @return a new default CallableStatement object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    private CallableStatement prepareCallableStatement(String sql, Object... args) throws SQLException {
        CallableStatementInfo statementInfo = new CallableStatementInfo();
        connectionInfo.addStatementInfo(statementInfo);
        statementInfo.setConnectionInfo(connectionInfo);
        SQLException e = null;
        try {
            RoutedConnection routedConnection = getConnection(sql);
            statementInfo.setRouteInfo(routedConnection.getRoutedDataSource().getRouteInfo());
            statementInfo.setSql(sql);
            statementInfo.setBeforeTimeToCreateStatementNs(System.nanoTime());
            statementInfo.setBeforeTimeToCreateStatementMillis(System.currentTimeMillis());
            eventListener.onBeforeCallableStatement(statementInfo);
            Connection connection = routedConnection.getConnection();
            String nativeSql = routedConnection.getNativeSql();
            CallableStatement callableStatement = createCallableStatementWithArgs(connection, nativeSql, args);
            statementInfo.setNativeSql(nativeSql);
            statementInfo.setStatement(callableStatement);
            statementInfo.setCallableStatement(callableStatement);
            return new ProxyCallableStatement(this.sqlXDataSource, statementInfo, callableStatement, this.eventListener);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            statementInfo.addException(e);
            statementInfo.setAfterTimeToCreateStatementNs(System.nanoTime());
            statementInfo.setAfterTimeToCreateStatementMillis(System.currentTimeMillis());
            eventListener.onAfterCallStatement(statementInfo, e);
        }
    }

    /**
     * Creates a CallableStatement with the given connection, SQL, and arguments.
     * This method handles different types of arguments to accommodate various
     * CallableStatement creation methods, such as specifying result set type and concurrency,
     * and result set holdability.
     *
     * @param connection the connection to the database
     * @param nativeSql  the SQL statement to be pre-compiled
     * @param args       the arguments for the CallableStatement
     * @return a new default CallableStatement object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    private CallableStatement createCallableStatementWithArgs(Connection connection, String nativeSql, Object... args) throws SQLException {
        if (args.length == 0) {
            return connection.prepareCall(nativeSql);
        } else if (args.length == 2 && args[0] instanceof Integer && args[1] instanceof Integer) {
            // CallableStatement.prepareCall(String sql, int resultSetType, int resultSetConcurrency)
            int resultSetType = (int) args[0];
            int resultSetConcurrency = (int) args[1];
            return connection.prepareCall(nativeSql, resultSetType, resultSetConcurrency);
        } else if (args.length == 3 && args[0] instanceof Integer && args[1] instanceof Integer && args[2] instanceof Integer) {
            // CallableStatement.prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            int resultSetType = (int) args[0];
            int resultSetConcurrency = (int) args[1];
            int resultSetHoldability = (int) args[2];
            return connection.prepareCall(nativeSql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } else {
            throw new SQLException("Unsupported arguments for prepareCall");
        }
    }
}
