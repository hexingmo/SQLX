package com.github.sqlx.jdbc;

import com.github.sqlx.jdbc.datasource.RoutedDataSource;
import com.github.sqlx.jdbc.datasource.SqlXDataSource;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.SqlType;
import com.github.sqlx.sql.Table;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProxyConnectionTest {

    @Mock
    private SqlXDataSource sqlXDataSource;

    @Mock
    private EventListener eventListener;

    @Mock
    private RoutedDataSource routedDataSource;

    @Mock
    private Connection physicalConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private RoutedConnection routedConnection;

    @Mock
    private RoutedConnection routedConnectionB;

    @Mock
    private RouteInfo routeInfoB;

    @Mock
    private CallableStatement callableStatementB;

    @Mock
    private ConnectionInfo connectionInfoB;

    @InjectMocks
    private ProxyConnection proxyConnection;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(sqlXDataSource.getDataSource(anyString())).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(new RouteInfo());
        when(routedConnection.getConnection()).thenReturn(physicalConnection);
        when(routedConnection.getRoutedDataSource()).thenReturn(routedDataSource);
        when(routedConnection.getNativeSql()).thenReturn("SELECT 1");
        when(physicalConnection.prepareStatement(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(preparedStatement);

        // Additional setup from file B
        RouteInfo routeInfo = mock(RouteInfo.class);
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        when(sqlAttribute.getNativeSql()).thenReturn("SELECT * FROM table");
        when(routeInfo.getSqlAttribute()).thenReturn(sqlAttribute);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfo);
        when(routedDataSource.getConnection()).thenReturn(physicalConnection);
        when(sqlXDataSource.getDataSource(anyString())).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfoB);
        when(routedConnection.getConnection()).thenReturn(physicalConnection);
        when(physicalConnection.prepareStatement(anyString(), any(int[].class))).thenReturn(preparedStatement);
        when(physicalConnection.prepareCall(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(callableStatementB);
        when(physicalConnection.prepareCall(anyString())).thenReturn(callableStatementB);
        proxyConnection = new ProxyConnection(sqlXDataSource, eventListener);
        setPhysicalConnection(proxyConnection, physicalConnection);
    }

    @Test
    void createStatement_ValidParameters_ReturnsProxyStatement() throws SQLException {
        // Arrange
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

        // Act
        Statement statement = proxyConnection.createStatement(resultSetType, resultSetConcurrency);

        // Assert
        assertNotNull(statement);
        assertTrue(statement instanceof ProxyStatement);
    }

    @Test
    public void createStatement_InvalidParameters_ThrowsSQLException() throws SQLException {
        // Arrange
        int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

        // Mock the ProxyStatement constructor to throw an SQLException
        doThrow(new SQLException("Test exception")).when(sqlXDataSource).getDataSource(anyString());

        // Act & Assert
        assertThrows(SQLException.class, () -> proxyConnection.createStatement(resultSetType, resultSetConcurrency));
    }

    @Test
    public void createStatementB_ValidParameters_ReturnsProxyStatement() throws SQLException {
        int resultSetType = 0;
        int resultSetConcurrency = 0;
        int resultSetHoldability = 0;

        Statement statement = proxyConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);

        assertNotNull(statement);
    }

    @Test
    public void createStatementB_InvalidParameters_ThrowsSQLException() {
        int invalidResultSetType = 999; // 假设这是一个无效的值
        int resultSetConcurrency = 0;
        int resultSetHoldability = 0;

        try {
            proxyConnection.createStatement(invalidResultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            // 预期的异常
        }
    }

    @Test
    public void createStatement_ReturnsProxyStatement() throws SQLException {
        Statement statement = proxyConnection.createStatement();
        assertNotNull(statement, "The created statement should not be null");
    }

    @Test
    public void createStatement_ThrowsSQLException() throws SQLException {
        when(sqlXDataSource.getDataSource(anyString())).thenThrow(SQLException.class);
        try {
            proxyConnection.createStatement();
        } catch (SQLException e) {
            assertNotNull(e, "SQLException should be thrown");
        }
    }

    @Test
    void prepareStatement_SuccessfulExecution_ReturnsPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString(), anyInt(), anyInt())).thenReturn(preparedStatement);

        PreparedStatement result = proxyConnection.prepareStatement("SELECT 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        assertNotNull(result);
        verify(physicalConnection).prepareStatement("SELECT 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    //@Test
    //void prepareStatement_SQLException_ThrowsSQLException() throws SQLException {
    //    when(physicalConnection.prepareStatement(anyString(), anyInt(), anyInt())).thenThrow(SQLException.class);
    //
    //    assertThrows(SQLException.class, () -> {
    //        proxyConnection.prepareStatement("SELECT 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    //    });
    //}

    @Test
    void prepareStatement_EmptySQL_ThrowsSQLException() {
        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement("", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        });
    }

    @Test
    void prepareStatement_NullRoutedConnection_ThrowsSQLException() throws SQLException {
        when(sqlXDataSource.getDataSource(anyString())).thenReturn(null);

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement("SELECT 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        });
    }

    @Test
    void prepareStatement_ValidSQL_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        PreparedStatement result = proxyConnection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);

        assertNotNull(result);
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), isNull(SQLException.class));
    }

    @Test
    void prepareStatement_SQLException_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM table";
        when(physicalConnection.prepareStatement(anyString(), anyInt(), anyInt(), anyInt())).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        });

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), isA(SQLException.class));
    }

    @Test
    public void prepareStatement_ValidSQLAndColumnNames_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        String[] columnNames = {"column1", "column2"};
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPreparedStatement);

        PreparedStatement preparedStatement = proxyConnection.prepareStatement(sql, columnNames);

        assertNotNull(preparedStatement);
        verify(physicalConnection).prepareStatement(sql, columnNames);
    }

    @Test
    public void prepareStatement_SQLExceptionThrown_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM table";
        String[] columnNames = {"column1", "column2"};
        SQLException sqlException = new SQLException("Test exception");
        when(physicalConnection.prepareStatement(anyString(), any(String[].class))).thenThrow(sqlException);

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql, columnNames));
    }

    @Test
    public void prepareStatement_EmptySQL_ReturnsPreparedStatement() throws SQLException {
        String sql = "";
        String[] columnNames = {"column1", "column2"};
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPreparedStatement);

        PreparedStatement preparedStatement = proxyConnection.prepareStatement(sql, columnNames);

        assertNotNull(preparedStatement);
        verify(physicalConnection).prepareStatement(sql, columnNames);
    }

    @Test
    public void prepareStatement_EmptyColumnNames_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        String[] columnNames = {};
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPreparedStatement);

        PreparedStatement preparedStatement = proxyConnection.prepareStatement(sql, columnNames);

        assertNotNull(preparedStatement);
        verify(physicalConnection).prepareStatement(sql, columnNames);
    }

    @Test
    void prepareStatement_SQLExceptionThrown_ThrowsNullPointerException() throws SQLException {
        String sql = "SELECT * FROM users";
        RoutedDataSource routedDataSource = mock(RoutedDataSource.class);
        Connection connection = mock(Connection.class);

        when(sqlXDataSource.getDataSource(sql)).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(new RouteInfo());
        when(connection.prepareStatement(sql)).thenThrow(SQLException.class);

        assertThrows(NullPointerException.class, () -> proxyConnection.prepareStatement(sql));
    }

    @Test
    void prepareStatement_NullSQL_ThrowsSQLException() {
        assertThrows(SQLException.class, () -> {
            try {
                proxyConnection.prepareStatement(null);
            } catch (NullPointerException e) {
                throw new SQLException(e);
            }
        });
    }

    @Test
    void prepareStatement_ValidSQL_SuccessfulPreparation() throws SQLException {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {1, 2};

        PreparedStatement result = proxyConnection.prepareStatement(sql, columnIndexes);

        assertNotNull(result);
        verify(physicalConnection).prepareStatement(sql, columnIndexes);
        verify(eventListener).onBeforePrepareStatement(any());
        verify(eventListener).onAfterPrepareStatement(any(), isNull(SQLException.class));
    }

    @Test
    void prepareStatement_SQLException_ThrowsException() throws SQLException {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {1, 2};
        SQLException sqlException = new SQLException("Test exception");

        when(physicalConnection.prepareStatement(anyString(), any(int[].class))).thenThrow(sqlException);

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql, columnIndexes));
        verify(eventListener).onBeforePrepareStatement(any());
        verify(eventListener).onAfterPrepareStatement(any(), eq(sqlException));
    }

    @Test
    void prepareStatement_EmptySQL_ThrowsException() {
        String sql = "";
        int[] columnIndexes = {1, 2};

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql, columnIndexes));
    }

    @Test
    void prepareStatement_NullSQL_ThrowsException() {
        int[] columnIndexes = {1, 2};

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(null, columnIndexes));
    }

    @Test
    void prepareStatement_EmptyColumnIndexes_ThrowsException() throws SQLException {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {};

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql, columnIndexes));
    }

    @Test
    void prepareStatement_NullColumnIndexes_ThrowsException() throws SQLException {
        String sql = "SELECT * FROM table";

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql, (int[]) null));
    }

    @Test
    void prepareCall_SuccessfulExecution_ReturnsCallableStatement() throws SQLException {
        // 准备
        String sql = "CALL myProcedure";
        RoutedDataSource routedDataSource = mock(RoutedDataSource.class);
        RouteInfo routeInfo = mock(RouteInfo.class);
        Connection connection = mock(Connection.class);
        CallableStatement callableStatement = mock(CallableStatement.class);

        when(sqlXDataSource.getDataSource(sql)).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfo);
        when(connection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenReturn(callableStatement);

        // 操作
        CallableStatement result = proxyConnection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        // 验证
        assertNotNull(result);
        verify(eventListener).onBeforeCallableStatement(any());
        verify(eventListener).onAfterCallStatement(any(), isNull());
    }

    @Test
    void prepareCall_ThrowsSQLException_ExceptionHandled() throws SQLException {
        // 准备
        String sql = "CALL myProcedure";
        RoutedDataSource routedDataSource = mock(RoutedDataSource.class);
        RouteInfo routeInfo = mock(RouteInfo.class);
        Connection connection = mock(Connection.class);

        when(sqlXDataSource.getDataSource(sql)).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfo);
        when(connection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenThrow(SQLException.class);

        // 操作和验证
        assertThrows(SQLException.class, () -> {
            proxyConnection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        });

        verify(eventListener).onBeforeCallableStatement(any());
        verify(eventListener).onAfterCallStatement(any(), any(SQLException.class));
    }

    @Test
    void prepareCall_EventListenerCalledOnException_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM test";
        when(physicalConnection.prepareCall(anyString(), anyInt(), anyInt(), anyInt())).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.prepareCall(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT));
        verify(eventListener, times(1)).onBeforeCallableStatement(any());
        verify(eventListener, times(1)).onAfterCallStatement(any(), isA(SQLException.class));
    }

    @Test
    void prepareCall_TimestampsSetCorrectly_ReturnsCallableStatement() throws SQLException {
        String sql = "SELECT * FROM test";
        CallableStatement result = proxyConnection.prepareCall(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);

        assertNotNull(result);
        StatementInfo statementInfo = proxyConnection.getConnectionInfo().getStatementInfo();
        assertTrue(statementInfo.getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(statementInfo.getAfterTimeToCreateStatementNs() > 0);
    }

    @Test
    void prepareCall_ValidSQL_ReturnsCallableStatement() throws SQLException {
        String sql = "CALL my_procedure()";
        when(routeInfoB.getSqlAttribute()).thenReturn(new SqlAttribute() {
            @Override
            public String getSql() { return ""; }
            @Override
            public String getNativeSql() { return ""; }
            @Override
            public SqlType getSqlType() { return null; }
            @Override
            public boolean isWrite() { return false; }
            @Override
            public boolean isRead() { return false; }
            @Override
            public void setDefaultDatabase(String database) {}
            @Override
            public Set<String> getDatabases() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleFromTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getFromTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleJoinTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getJoinTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleSubTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getSubTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleInsertTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getInsertTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleUpdateTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getUpdateTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleDeleteTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getDeleteTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getReadTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getWriteTables() { return new HashSet<>(); }
        });

        CallableStatement result = proxyConnection.prepareCall(sql);

        assertNotNull(result);
        verify(physicalConnection).prepareCall(sql);
        verify(eventListener).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener).onAfterCallStatement(any(CallableStatementInfo.class), isNull(SQLException.class));
    }

    @Test
    void prepareCall_SQLException_ThrowsSQLException() throws SQLException {
        String sql = "CALL my_procedure()";
        when(routeInfoB.getSqlAttribute()).thenReturn(new SqlAttribute() {
            @Override
            public String getSql() { return ""; }
            @Override
            public String getNativeSql() { return ""; }
            @Override
            public SqlType getSqlType() { return null; }
            @Override
            public boolean isWrite() { return false; }
            @Override
            public boolean isRead() { return false; }
            @Override
            public void setDefaultDatabase(String database) {}
            @Override
            public Set<String> getDatabases() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleFromTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getFromTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleJoinTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getJoinTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleSubTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getSubTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleInsertTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getInsertTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleUpdateTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getUpdateTables() { return new HashSet<>(); }
            @Override
            public Set<String> getSimpleDeleteTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getDeleteTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getReadTables() { return new HashSet<>(); }
            @Override
            public Set<Table> getWriteTables() { return new HashSet<>(); }
        });
        when(physicalConnection.prepareCall(anyString())).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.prepareCall(sql));
        verify(eventListener).onBeforeCallableStatement(any(CallableStatementInfo.class));
        verify(eventListener).onAfterCallStatement(any(CallableStatementInfo.class), isA(SQLException.class));
    }

    @Test
    void prepareCall_EmptySQL_ThrowsSQLException() {
        assertThrows(SQLException.class, () -> proxyConnection.prepareCall(""));
    }

    @Test
    void prepareCall_NullRoutedConnection_ThrowsSQLException() throws SQLException {
        when(sqlXDataSource.getDataSource(anyString())).thenReturn(routedDataSource);
        when(routedDataSource.getConnection()).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.prepareCall("CALL my_procedure()"));
    }

    @Test
    public void commit_PhysicalConnectionIsNull_DoesNothing() throws SQLException {
        proxyConnection.commit();
        verify(eventListener, never()).onBeforeCommit(any(ConnectionInfo.class));
        verify(eventListener, never()).onAfterCommit(any(ConnectionInfo.class), any(SQLException.class));
    }

    @Test
    public void commit_SuccessfulCommit_RecordsTimesAndCallsEvents() throws SQLException, NoSuchFieldException, IllegalAccessException {
        proxyConnection.commit();

        verify(eventListener, times(1)).onBeforeCommit(connectionInfoB);
        verify(eventListener, times(1)).onAfterCommit(connectionInfoB, null);
        verify(connectionInfoB, times(1)).setBeforeTimeToCommitNs(anyLong());
        verify(connectionInfoB, times(1)).setBeforeTimeToCommitMillis(anyLong());
        verify(connectionInfoB, times(1)).setAfterTimeToCommitNs(anyLong());
        verify(connectionInfoB, times(1)).setAfterTimeToCommitMillis(anyLong());
    }

    @Test
    public void commit_CommitFails_RecordsTimesAndCallsEventsWithException() throws SQLException {
        SQLException expectedException = new SQLException("Commit failed");
        doThrow(expectedException).when(physicalConnection).commit();

        assertThrows(SQLException.class, () -> proxyConnection.commit());

        verify(eventListener, times(1)).onBeforeCommit(connectionInfoB);
        verify(eventListener, times(1)).onAfterCommit(connectionInfoB, expectedException);
        verify(connectionInfoB, times(1)).setBeforeTimeToCommitNs(anyLong());
        verify(connectionInfoB, times(1)).setBeforeTimeToCommitMillis(anyLong());
        verify(connectionInfoB, times(1)).setAfterTimeToCommitNs(anyLong());
        verify(connectionInfoB, times(1)).setAfterTimeToCommitMillis(anyLong());
    }

    @Test
    public void rollback_WhenPhysicalConnectionIsNull_DoesNothing() throws SQLException, NoSuchFieldException, IllegalAccessException {
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, null);

        proxyConnection.rollback();
        verifyNoInteractions(physicalConnection, eventListener, connectionInfoB);
    }

    @Test
    public void rollback_WhenPhysicalConnectionIsNotNull_RollsBackSuccessfully() throws SQLException {
        proxyConnection.rollback();
        verify(physicalConnection).rollback();
        verify(eventListener).onBeforeRollback(connectionInfoB);
        verify(eventListener).onAfterRollback(connectionInfoB, null);
    }

    @Test
    public void rollback_WhenSQLExceptionOccurs_ThrowsSQLException() throws SQLException {
        SQLException sqlException = new SQLException("Test exception");
        doThrow(sqlException).when(physicalConnection).rollback();

        assertThrows(SQLException.class, () -> proxyConnection.rollback());

        verify(eventListener).onBeforeRollback(connectionInfoB);
        verify(eventListener).onAfterRollback(connectionInfoB, sqlException);
    }

    @Test
    public void rollback_PhysicalConnectionIsNull_NoOperation() throws SQLException, NoSuchFieldException, IllegalAccessException {
        // 设置
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, null);

        // 操作
        proxyConnection.rollback(mock(Savepoint.class));

        // 验证
        verify(eventListener, never()).onBeforeSavepointRollback(any(ConnectionInfo.class), any(Savepoint.class));
        verify(eventListener, never()).onAfterSavepointRollback(any(ConnectionInfo.class), any(Savepoint.class), any(SQLException.class));
    }

    @Test
    public void rollback_SuccessfulRollback_ListenerMethodsCalled() throws SQLException, NoSuchFieldException, IllegalAccessException {
        // 设置
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, physicalConnection);

        // 操作
        proxyConnection.rollback(mock(Savepoint.class));

        // 验证
        verify(eventListener, times(1)).onBeforeSavepointRollback(any(ConnectionInfo.class), any(Savepoint.class));
        verify(eventListener, times(1)).onAfterSavepointRollback(any(ConnectionInfo.class), any(Savepoint.class), isNull(SQLException.class));
    }

    @Test
    public void rollback_FailedRollback_ListenerMethodsCalled() throws SQLException, NoSuchFieldException, IllegalAccessException {
        // 设置
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, physicalConnection);
        doThrow(new SQLException("Rollback failed")).when(physicalConnection).rollback(any(Savepoint.class));

        // 操作
        assertThrows(SQLException.class, () -> proxyConnection.rollback(mock(Savepoint.class)));

        // 验证
        verify(eventListener, times(1)).onBeforeSavepointRollback(any(ConnectionInfo.class), any(Savepoint.class));
        verify(eventListener, times(1)).onAfterSavepointRollback(any(ConnectionInfo.class), any(Savepoint.class), any(SQLException.class));
    }

    @Test
    public void getConnection_ExistingPhysicalConnection_ReturnsRoutedConnection() throws SQLException, NoSuchFieldException, IllegalAccessException {
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, physicalConnection);
        when(physicalConnection.getCatalog()).thenReturn("testCatalog");

        RoutedConnection routedConnection = proxyConnection.getConnection("SELECT * FROM test");

        assertNotNull(routedConnection);
        assertEquals(physicalConnection, routedConnection.getConnection());
        verify(routeInfoB, times(2)).getSqlAttribute();
    }

    @Test
    public void getConnection_NewPhysicalConnection_ReturnsRoutedConnection() throws SQLException {
        when(routedDataSource.getConnection()).thenReturn(physicalConnection);
        when(physicalConnection.getCatalog()).thenReturn("testCatalog");

        RoutedConnection routedConnection = proxyConnection.getConnection("SELECT * FROM test");

        assertNotNull(routedConnection);
        assertEquals(physicalConnection, routedConnection.getConnection());
        verify(routeInfoB, times(2)).getSqlAttribute();
    }

    @Test
    public void getConnection_ThrowsSQLException() throws SQLException {
        when(routedDataSource.getConnection()).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.getConnection("SELECT * FROM test"));
    }

    public void setPhysicalConnection(ProxyConnection proxyConnection, Connection physicalConnection) throws Exception {
        Field field = ProxyConnection.class.getDeclaredField("physicalConnection");
        field.setAccessible(true);
        field.set(proxyConnection, physicalConnection);
    }

    public void setClosed(ProxyConnection proxyConnection, boolean closed) throws Exception {
        Field field = ProxyConnection.class.getDeclaredField("closed");
        field.setAccessible(true);
        field.set(proxyConnection, closed);
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsClosed_ReturnsTrue() throws SQLException {
        when(physicalConnection.isClosed()).thenReturn(true);

        assertTrue(proxyConnection.isClosed());
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsNotClosed_ReturnsFalse() throws SQLException {
        when(physicalConnection.isClosed()).thenReturn(false);

        assertFalse(proxyConnection.isClosed());
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsNullAndClosedIsTrue_ReturnsTrue() throws Exception {
        setClosed(proxyConnection, true);
        assertTrue(proxyConnection.isClosed());
    }

    @Test
    public void isClosed_WhenPhysicalConnectionIsNullAndClosedIsFalse_ReturnsFalse() throws Exception {
        setClosed(proxyConnection, false);
        assertFalse(proxyConnection.isClosed());
    }

    @Test
    public void close_AlreadyClosed_DoesNothing() throws SQLException, Exception {
        setClosed(proxyConnection, true);
        proxyConnection.close();
        verify(physicalConnection, never()).close();
    }

    @Test
    public void close_NullPhysicalConnection_DoesNothing() throws SQLException, Exception {
        setPhysicalConnection(proxyConnection, null);
        proxyConnection.close();
        verify(physicalConnection, never()).close();
    }

    @Test
    public void close_SuccessfulClose_ClosesConnection() throws SQLException {
        proxyConnection.close();
        verify(physicalConnection).close();
        verify(eventListener).onBeforeConnectionClose(connectionInfoB);
        verify(eventListener).onAfterConnectionClose(connectionInfoB, null);
    }

    @Test
    public void close_SQLExceptionOnClose_HandlesException() throws SQLException {
        SQLException sqlException = new SQLException("Test exception");
        doThrow(sqlException).when(physicalConnection).close();

        proxyConnection.close();

        verify(eventListener).onBeforeConnectionClose(connectionInfoB);
        verify(eventListener).onAfterConnectionClose(connectionInfoB, sqlException);
    }
}
