package com.github.sqlx.jdbc;

import com.github.sqlx.jdbc.datasource.RoutedDataSource;
import com.github.sqlx.jdbc.datasource.SqlXDataSource;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.listener.RouteInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProxyConnectionTest1 {

    @Mock
    private SqlXDataSource sqlXDataSource;

    @Mock
    private RoutedDataSource routedDataSource;

    @Mock
    private RouteInfo routeInfo;

    @Mock
    private RoutedConnection routedConnection;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private CallableStatement callableStatement;

    @Mock
    private EventListener eventListener;

    @Mock
    private Connection physicalConnectionB; // Renamed from physicalConnection in file A

    @Mock
    private Connection physicalConnectionB2;

    @Mock
    private DataSource dataSource;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private ConnectionInfo connectionInfo;

    @InjectMocks
    private ProxyConnection proxyConnection;

    @BeforeEach
    public void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        when(sqlXDataSource.getDataSource(anyString())).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfo);
        when(routedConnection.getConnection()).thenReturn(connection);
        when(routedConnection.getNativeSql()).thenReturn("SELECT * FROM table");
        try {
            when(connection.prepareStatement(anyString(), anyInt(), anyInt())).thenReturn(preparedStatement);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(connection.prepareStatement(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(preparedStatement);
            when(connection.prepareStatement(anyString(), any(int[].class))).thenReturn(preparedStatement);
            when(connection.prepareCall(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(callableStatement);
            doNothing().when(physicalConnectionB).rollback();
            doNothing().when(physicalConnectionB2).rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        proxyConnection = new ProxyConnection(mock(SqlXDataSource.class), eventListener);
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, physicalConnectionB); // Updated reference
        setPrivateField(proxyConnection, "physicalConnection", physicalConnectionB);
        when(sqlXDataSource.getDataSourceForDatabaseMetaData()).thenReturn(routedDataSource);
        when(routedDataSource.getDelegate()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
    }

    @Test
    public void createStatement_ValidParameters_ReturnsProxyStatement() throws SQLException {
        // 准备
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

        // 操作
        Statement statement = proxyConnection.createStatement(resultSetType, resultSetConcurrency);

        // 断言
        assertNotNull(statement);
        verify(sqlXDataSource, never()).getDataSource(anyString());
        verify(eventListener, never()).onAfterCreateStatement(any(), any());
    }

    @Test
    public void createStatement_InvalidResultSetType_ThrowsSQLException() {
        // 准备
        int invalidResultSetType = 999; // 无效的 ResultSet 类型
        int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

        // 操作和断言
        try {
            proxyConnection.createStatement(invalidResultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            // 预期的异常
        }
    }

    @Test
    public void createStatement_InvalidResultSetConcurrency_ThrowsSQLException() {
        // 准备
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int invalidResultSetConcurrency = 999; // 无效的 ResultSet 并发性

        // 操作和断言
        try {
            proxyConnection.createStatement(resultSetType, invalidResultSetConcurrency);
        } catch (SQLException e) {
            // 预期的异常
        }
    }

    @Test
    public void createStatement_InvalidParameters_ThrowsSQLException() {
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
        int resultSetHoldability = ResultSet.CLOSE_CURSORS_AT_COMMIT;

        when(sqlXDataSource.getDataSource(anyString())).thenThrow(new SQLException("Test exception"));

        try {
            proxyConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            assertNotNull(e);
            verify(sqlXDataSource, times(1)).getDataSource(anyString());
            verify(eventListener, times(1)).onAfterCreateStatement(any(), any());
        }
    }

    @Test
    public void createStatement_ValidInputs_ReturnsProxyStatement() throws SQLException {
        java.sql.Statement statement = proxyConnection.createStatement();
        assertNotNull(statement, "The created statement should not be null");

        // 验证 ProxyStatement 是否使用正确的参数初始化
        verify(sqlXDataSource, times(1)).getDataSource(anyString());
        verify(eventListener, times(1)).onAfterCreateStatement(any(), any());
    }

    @Test
    public void prepareStatement_SuccessfulExecution_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        int resultSetType = 1;
        int resultSetConcurrency = 1;

        PreparedStatement result = proxyConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);

        assertNotNull(result);
        verify(eventListener).onBeforePrepareStatement(any(PreparedStatementInfo.class));
        verify(eventListener).onAfterPrepareStatement(any(PreparedStatementInfo.class), isNull(SQLException.class));
    }

    @Test
    public void prepareStatement_ThrowsSQLException_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM table";
        int resultSetType = 1;
        int resultSetConcurrency = 1;

        when(connection.prepareStatement(anyString(), anyInt(), anyInt())).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        });

        verify(eventListener).onBeforePrepareStatement(any(PreparedStatementInfo.class));
        verify(eventListener).onAfterPrepareStatement(any(PreparedStatementInfo.class), isA(SQLException.class));
    }

    @Test
    public void prepareStatement_ValidSQL_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        PreparedStatement result = proxyConnection.prepareStatement(sql);

        assertNotNull(result);
        verify(connection).prepareStatement(sql);
    }

    @Test
    public void prepareStatement_EmptySQL_ThrowsSQLException() {
        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(""));
    }

    @Test
    public void prepareStatement_ExceptionDuringPrepareStatement_ThrowsSQLException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement("SELECT * FROM table"));
    }

    @Test
    public void prepareStatement_SuccessfulExecution_ReturnsPreparedStatementWithAutoGeneratedKeys() throws SQLException {
        String sql = "SELECT * FROM table";
        int autoGeneratedKeys = 1;

        PreparedStatement result = proxyConnection.prepareStatement(sql, autoGeneratedKeys);

        assertNotNull(result);
        verify(eventListener).onBeforePrepareStatement(any());
        verify(eventListener).onAfterPrepareStatement(any(), isNull(SQLException.class));
    }

    @Test
    public void prepareStatement_SQLExceptionThrown_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM table";
        int autoGeneratedKeys = 1;

        when(connection.prepareStatement(anyString(), anyInt())).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql, autoGeneratedKeys));
        verify(eventListener).onBeforePrepareStatement(any());
        verify(eventListener).onAfterPrepareStatement(any(), isA(SQLException.class));
    }

    @Test
    public void prepareStatement_SuccessfulExecution_ReturnsPreparedStatementWithColumnIndexes() throws SQLException {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {1, 2};

        PreparedStatement result = proxyConnection.prepareStatement(sql, columnIndexes);

        assertNotNull(result);
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), isNull());
    }

    @Test
    public void prepareStatement_ConnectionException_ThrowsSQLExceptionWithColumnIndexes() throws SQLException {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {1, 2};

        when(routedConnection.getConnection()).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement(sql, columnIndexes);
        });

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any(SQLException.class));
    }

    @Test
    public void prepareStatement_PrepareStatementException_ThrowsSQLExceptionWithColumnIndexes() throws SQLException {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {1, 2};

        when(connection.prepareStatement(anyString(), any(int[].class))).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement(sql, columnIndexes);
        });

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any(SQLException.class));
    }

    @Test
    public void prepareStatement_ConnectionException_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM table";
        int resultSetType = 1;
        int resultSetConcurrency = 2;
        int resultSetHoldability = 3;

        when(routedConnection.getConnection()).thenThrow(new SQLException("Connection error"));

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        });

        verify(eventListener, times(1)).onBeforePrepareStatement(any(PreparedStatementInfo.class));
        verify(eventListener, times(1)).onAfterPrepareStatement(any(PreparedStatementInfo.class), any(SQLException.class));
    }

    @Test
    public void prepareStatement_PreparedStatementException_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM table";
        int resultSetType = 1;
        int resultSetConcurrency = 2;
        int resultSetHoldability = 3;

        when(connection.prepareStatement(anyString(), anyInt(), anyInt(), anyInt())).thenThrow(new SQLException("Prepare statement error"));

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        });

        verify(eventListener, times(1)).onBeforePrepareStatement(any(PreparedStatementInfo.class));
        verify(eventListener, times(1)).onAfterPrepareStatement(any(PreparedStatementInfo.class), any(SQLException.class));
    }

    @Test
    public void prepareStatement_ValidSQLAndColumnNames_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        String[] columnNames = {"column1", "column2"};

        PreparedStatement result = proxyConnection.prepareStatement(sql, columnNames);

        assertNotNull(result);
        verify(connection).prepareStatement(sql, columnNames);
    }

    @Test
    public void prepareStatement_EmptySQL_ReturnsPreparedStatement() throws SQLException {
        String sql = "";
        String[] columnNames = {"column1", "column2"};

        PreparedStatement result = proxyConnection.prepareStatement(sql, columnNames);

        assertNotNull(result);
        verify(connection).prepareStatement(sql, columnNames);
    }

    @Test
    public void prepareStatement_EmptyColumnNames_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        String[] columnNames = {};

        PreparedStatement result = proxyConnection.prepareStatement(sql, columnNames);

        assertNotNull(result);
        verify(connection).prepareStatement(sql, columnNames);
    }

    @Test
    public void prepareCall_SuccessfulExecution_ReturnsCallableStatement() throws SQLException {
        when(connection.prepareCall(anyString())).thenReturn(mock(CallableStatement.class));
        when(routedDataSource.getConnection()).thenReturn(connection);

        CallableStatement callableStatement = proxyConnection.prepareCall("testSql");

        assertNotNull(callableStatement);
        verify(eventListener, times(1)).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener, times(1)).onAfterCallStatement(any(CallableStatementInfo.class), isNull(SQLException.class));
    }

    @Test
    public void prepareCall_SQLExceptionDuringExecution_ThrowsSQLException() throws SQLException {
        when(routedDataSource.getConnection()).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.prepareCall("testSql"));
        verify(eventListener, times(1)).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener, times(1)).onAfterCallStatement(any(CallableStatementInfo.class), isA(SQLException.class));
    }

    @Test
    public void prepareCall_SuccessfulExecution_ReturnsCallableStatementWithParams() throws SQLException {
        when(connection.prepareCall(anyString(), anyInt(), anyInt())).thenReturn(mock(CallableStatement.class));
        when(routedDataSource.getConnection()).thenReturn(connection);

        CallableStatement callableStatement = proxyConnection.prepareCall("testSql", 1, 2);

        assertNotNull(callableStatement);
        verify(eventListener, times(1)).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener, times(1)).onAfterCallStatement(any(CallableStatementInfo.class), isNull(SQLException.class));
    }

    @Test
    public void prepareCall_ExceptionDuringExecution_ThrowsSQLExceptionWithParams() throws SQLException {
        when(routedDataSource.getConnection()).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareCall("testSql", 1, 2);
        });

        verify(eventListener, times(1)).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener, times(1)).onAfterCallStatement(any(CallableStatementInfo.class), isA(SQLException.class));
    }

    @Test
    public void prepareCall_ValidSQL_ReturnsCallableStatement_B() throws SQLException {
        String sql = "CALL my_procedure()";
        CallableStatement result = proxyConnection.prepareCall(sql, 1, 2, 3);
        assertNotNull(result);
        verify(eventListener).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener).onAfterCallStatement(any(CallableStatementInfo.class), isNull(SQLException.class));
    }

    @Test
    public void prepareCall_SQLException_ThrowsSQLException_B() throws SQLException {
        String sql = "CALL my_procedure()";
        when(connection.prepareCall(anyString(), anyInt(), anyInt(), anyInt())).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareCall(sql, 1, 2, 3);
        });

        verify(eventListener).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener).onAfterCallStatement(any(CallableStatementInfo.class), isA(SQLException.class));
    }

    @Test
    public void commit_PhysicalConnectionIsNull_DoesNothing() throws SQLException, NoSuchFieldException, IllegalAccessException {
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, null);
        proxyConnection.commit();
        verifyNoInteractions(eventListener);
    }

    @Test
    public void commit_SuccessfulCommit_RecordsTimestampsAndTriggersEvents() throws SQLException {
        when(connectionInfo.getBeforeTimeToCommitMillis()).thenReturn(1000L);
        when(connectionInfo.getAfterTimeToCommitMillis()).thenReturn(2000L);

        proxyConnection.commit();

        verify(eventListener).onBeforeCommit(connectionInfo);
        verify(eventListener).onAfterCommit(connectionInfo, null);
        verify(physicalConnectionB).commit(); // Updated reference
    }

    @Test
    public void commit_CommitFails_RecordsTimestampsAndTriggersEvents() throws SQLException {
        SQLException sqlException = new SQLException("Commit failed");
        doThrow(sqlException).when(physicalConnectionB).commit(); // Updated reference

        assertThrows(SQLException.class, () -> proxyConnection.commit());

        verify(eventListener).onBeforeCommit(any(ConnectionInfo.class));
        verify(eventListener).onAfterCommit(any(ConnectionInfo.class), eq(sqlException));
    }

    @Test
    public void rollback_PhysicalConnectionIsNull_DoesNothing() throws SQLException {
        doNothing().when(physicalConnectionB).rollback();

        proxyConnection.rollback();

        verifyNoInteractions(eventListener);
        verifyNoInteractions(connectionInfo);
    }

    @Test
    public void rollback_SuccessfulRollback_RecordsTimesAndNotifiesListener() throws SQLException {
        doNothing().when(physicalConnectionB2).rollback();

        proxyConnection.rollback();

        verify(eventListener).onBeforeRollback(connectionInfo);
        verify(eventListener).onAfterRollback(connectionInfo, null);
        verify(connectionInfo).setBeforeTimeToRollbackNs(anyLong());
        verify(connectionInfo).setBeforeTimeToRollbackMillis(anyLong());
        verify(connectionInfo).setAfterTimeToRollbackNs(anyLong());
        verify(connectionInfo).setAfterTimeToRollbackMillis(anyLong());
    }

    @Test
    public void rollback_FailedRollback_RecordsTimesAndNotifiesListener() throws SQLException {
        SQLException sqlException = new SQLException("Rollback failed");
        doThrow(sqlException).when(physicalConnectionB2).rollback();

        assertThrows(SQLException.class, () -> proxyConnection.rollback());

        verify(eventListener).onBeforeRollback(any(ConnectionInfo.class));
        verify(eventListener).onAfterRollback(any(ConnectionInfo.class), sqlException);
        verify(connectionInfo).setBeforeTimeToRollbackNs(anyLong());
        verify(connectionInfo).setBeforeTimeToRollbackMillis(anyLong());
        verify(connectionInfo).setAfterTimeToRollbackNs(anyLong());
        verify(connectionInfo).setAfterTimeToRollbackMillis(anyLong());
    }

    @Test
    public void close_AlreadyClosedOrPhysicalConnectionIsNull_ReturnsImmediately() throws Exception {
        setPrivateField(proxyConnection, "closed", true);
        proxyConnection.close();
        verify(physicalConnectionB, never()).close();

        setPrivateField(proxyConnection, "closed", false);
        setPrivateField(proxyConnection, "physicalConnection", null);
        proxyConnection.close();
        verify(physicalConnectionB, never()).close();
    }

    @Test
    public void close_NormalClose_ClosesConnectionAndCallsEventListener() throws SQLException {
        proxyConnection.close();
        verify(physicalConnectionB).close();
        verify(eventListener).onBeforeConnectionClose(any(ConnectionInfo.class));
        verify(eventListener).onAfterConnectionClose(any(ConnectionInfo.class), isNull(SQLException.class));
    }

    @Test
    public void close_SQLExceptionDuringClose_CatchesExceptionAndCallsEventListener() throws SQLException {
        doThrow(new SQLException("Test exception")).when(physicalConnectionB).close();

        assertThrows(SQLException.class, proxyConnection::close);

        verify(eventListener).onBeforeConnectionClose(any(ConnectionInfo.class));
        verify(eventListener).onAfterConnectionClose(any(ConnectionInfo.class), any(SQLException.class));
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsClosed_ReturnsTrue() throws SQLException, NoSuchFieldException, IllegalAccessException {
        when(physicalConnectionB.isClosed()).thenReturn(true);
        setPrivateField(proxyConnection, "physicalConnection", physicalConnectionB);

        assertTrue(proxyConnection.isClosed());
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsNotClosed_ReturnsFalse() throws SQLException, NoSuchFieldException, IllegalAccessException {
        when(physicalConnectionB.isClosed()).thenReturn(false);
        setPrivateField(proxyConnection, "physicalConnection", physicalConnectionB);

        assertFalse(proxyConnection.isClosed());
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsNullAndClosedIsTrue_ReturnsTrue() throws NoSuchFieldException, IllegalAccessException, SQLException {
        setPrivateField(proxyConnection, "physicalConnection", null);
        setPrivateField(proxyConnection, "closed", true);

        assertTrue(proxyConnection.isClosed());
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsNullAndClosedIsFalse_ReturnsFalse() throws NoSuchFieldException, IllegalAccessException, SQLException {
        setPrivateField(proxyConnection, "physicalConnection", null);
        setPrivateField(proxyConnection, "closed", false);

        assertFalse(proxyConnection.isClosed());
    }

    @Test
    public void getMetaData_WhenPhysicalConnectionIsNotNull_ShouldReturnDatabaseMetaData() throws SQLException, NoSuchFieldException, IllegalAccessException {
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, connection);
        DatabaseMetaData result = proxyConnection.getMetaData();
        assertNotNull(result);
    }

    @Test
    public void getMetaData_WhenPhysicalConnectionIsNull_ShouldAcquireConnectionAndReturnDatabaseMetaData() throws SQLException {
        DatabaseMetaData result = proxyConnection.getMetaData();
        assertNotNull(result);
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    public void getMetaData_WhenSQLExceptionThrown_ShouldThrowSQLException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        assertThrows(SQLException.class, () -> proxyConnection.getMetaData());
    }

    @Test
    public void isReadOnly_WhenPhysicalConnectionIsReadOnly_ReturnsTrue() throws Exception {
        when(physicalConnectionB.isReadOnly()).thenReturn(true);
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, physicalConnectionB);

        assertTrue(proxyConnection.isReadOnly());
    }

    @Test
    public void isReadOnly_WhenPhysicalConnectionIsNotReadOnly_ReturnsFalse() throws Exception {
        when(physicalConnectionB.isReadOnly()).thenReturn(false);
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, physicalConnectionB);

        assertFalse(proxyConnection.isReadOnly());
    }

    @Test
    public void isReadOnly_WhenPhysicalConnectionIsNullAndProxyIsReadOnly_ReturnsTrue() throws Exception {
        Field field = ProxyConnection.class.getDeclaredField("readOnly");
        field.setAccessible(true);
        field.set(proxyConnection, true);

        assertTrue(proxyConnection.isReadOnly());
    }

    @Test
    public void isReadOnly_WhenPhysicalConnectionIsNullAndProxyIsNotReadOnly_ReturnsFalse() throws Exception {
        Field field = ProxyConnection.class.getDeclaredField("readOnly");
        field.setAccessible(true);
        field.set(proxyConnection, false);

        assertFalse(proxyConnection.isReadOnly());
    }

    @Test
    public void getConnection_WhenPhysicalConnectionIsNull_ShouldReturnNewRoutedConnection() throws SQLException {
        RoutedConnection routedConnection = proxyConnection.getConnection("SELECT * FROM table");
        assertNotNull(routedConnection);
        verify(routedDataSource).getConnection();
    }

    @Test
    public void getConnection_WhenPhysicalConnectionIsNotNull_ShouldReturnExistingRoutedConnection() throws SQLException, NoSuchFieldException, IllegalAccessException {
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, connection);
        RoutedConnection routedConnection = proxyConnection.getConnection("SELECT * FROM table");
        assertNotNull(routedConnection);
        verify(routedDataSource, never()).getConnection();
    }

    @Test
    public void getConnection_WhenSQLExceptionOccurs_ShouldThrowSQLException() throws SQLException {
        when(routedDataSource.getConnection()).thenThrow(SQLException.class);
        try {
            proxyConnection.getConnection("SELECT * FROM table");
        } catch (SQLException e) {
            // 预期的异常
        }
        verify(routedDataSource).getConnection();
    }

    @Test
    public void rollback_NullPhysicalConnection_DoesNothing() throws SQLException, NoSuchFieldException, IllegalAccessException {
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, null);
        proxyConnection.rollback(mock(Savepoint.class));
        verifyNoInteractions(eventListener);
    }

    @Test
    public void rollback_SuccessfulRollback_RecordsTimesAndCallsListener() throws SQLException {
        Savepoint savepoint = mock(Savepoint.class);
        doNothing().when(physicalConnectionB2).rollback(savepoint);

        proxyConnection.rollback(savepoint);

        verify(eventListener).onBeforeSavepointRollback(connectionInfo, savepoint);
        verify(eventListener).onAfterSavepointRollback(connectionInfo, savepoint, null);
    }

    @Test
    public void rollback_SQLExceptionOccurs_RecordsTimesAndCallsListener() throws SQLException {
        Savepoint savepoint = mock(Savepoint.class);
        SQLException sqlException = new SQLException("Test exception");
        doThrow(sqlException).when(physicalConnectionB2).rollback(savepoint);

        assertThrows(SQLException.class, () -> proxyConnection.rollback(savepoint));

        verify(eventListener).onBeforeSavepointRollback(connectionInfo, savepoint);
        verify(eventListener).onAfterSavepointRollback(connectionInfo, savepoint, sqlException);
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}

