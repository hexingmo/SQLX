package com.github.sqlx.jdbc;


import com.github.sqlx.jdbc.datasource.RoutedDataSource;
import com.github.sqlx.jdbc.datasource.SqlXDataSource;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.sql.SqlAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ProxyConnection}.
 *
 * @author : He Xing Mo
 */
class ProxyConnectionTest {

    @Mock
    private Connection physicalConnection;

    @Mock
    private DataSource realDataSource;

    @Mock
    private SqlXDataSource sqlXDataSource;

    @Mock
    private EventListener eventListener;

    @InjectMocks
    private ProxyConnection proxyConnection;

    @Mock
    private RoutedDataSource routedDataSource;

    @Mock
    private RoutedConnection routedConnection;

    @Mock
    private RouteInfo routeInfo;

    @Mock
    private SqlAttribute sqlAttribute;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        proxyConnection = new ProxyConnection(sqlXDataSource, eventListener);
        setPrivateField(proxyConnection, "physicalConnection", physicalConnection);

        when(sqlXDataSource.getDataSourceForDatabaseMetaData()).thenReturn(routedDataSource);
        when(sqlXDataSource.getDataSource(anyString())).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfo);
        when(routeInfo.getSqlAttribute()).thenReturn(sqlAttribute);

        when(routedDataSource.getDelegate()).thenReturn(realDataSource);
        when(routedDataSource.getConnection()).thenReturn(physicalConnection);
        when(routedDataSource.getConnection(anyString() , anyString())).thenReturn(physicalConnection);

        when(realDataSource.getConnection()).thenReturn(physicalConnection);
        when(realDataSource.getConnection(anyString() , anyString())).thenReturn(physicalConnection);


        when(routedConnection.getRoutedDataSource()).thenReturn(routedDataSource);
        when(routedConnection.getConnection()).thenReturn(physicalConnection);
//        when(proxyConnection.getConnection(anyString())).thenReturn(routedConnection);

    }



    @Test
    void prepareStatement_SuccessfulExecution_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);
        when(physicalConnection.prepareStatement(anyString() , anyInt() , anyInt())).thenReturn(preparedStatement);

        PreparedStatement ps = proxyConnection.prepareStatement(sql, 0, 0);

        assertNotNull(ps);
        assertTrue(ps instanceof ProxyPreparedStatement);
        ProxyPreparedStatement proxyPreparedStatement = (ProxyPreparedStatement) ps;
        assertNotNull(proxyPreparedStatement.getDelegate());
        assertNotNull(proxyPreparedStatement.getPreparedStatementInfo());
        assertTrue(proxyPreparedStatement.getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(proxyPreparedStatement.getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(proxyPreparedStatement.getPreparedStatementInfo().getNativeSql(), sql);
        assertEquals(proxyPreparedStatement.getPreparedStatementInfo().getStatement(), preparedStatement);

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any());
    }


    @Test
    void testPrepareStatement_ThrowsSQLException() throws Exception {
        String sql = "SELECT * FROM table";

        when(sqlAttribute.getNativeSql()).thenReturn(sql);
        when(physicalConnection.prepareStatement(sql)).thenThrow(new SQLException("prepareStatement error"));

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql));

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any());
    }

    @Test
    void testPrepareStatement_ExceptionDuringGetConnection() throws Exception {
        setPrivateField(proxyConnection , "physicalConnection", null);
        String sql = "SELECT * FROM table";
        when(routedConnection.getNativeSql()).thenReturn(sql);
        when(routedDataSource.getConnection()).thenThrow(new SQLException("Connection error"));

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql));

        verify(eventListener, times(1)).onBeforeGetConnection(any());
        verify(eventListener, times(1)).onAfterGetConnection(any() , any());

        verify(eventListener, never()).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any());
    }

    @Test
    void testPrepareStatement_ExceptionDuringPreparedStatement() throws Exception {
        String sql = "SELECT * FROM table";
        when(routedConnection.getNativeSql()).thenReturn(sql);
        when(physicalConnection.prepareStatement(any())).thenThrow(new SQLException("Prepare error"));

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql));

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any());
    }

    @Test
    void prepareStatement_NormalExecution_ReturnsPreparedStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString(), any(int[].class))).thenReturn(preparedStatement);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);
        PreparedStatement ps = proxyConnection.prepareStatement(sql, new int[]{1, 2});

        assertNotNull(ps);
        assertInstanceOf(ProxyPreparedStatement.class, ps);
        assertNotNull(((ProxyPreparedStatement) ps).getDelegate());
        assertNotNull(((ProxyPreparedStatement) ps).getPreparedStatementInfo());
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getNativeSql(), sql);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getStatement(), preparedStatement);

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any());
    }

    @Test
    void prepareStatement_SQLException_ThrowsSQLException() throws SQLException {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {1, 2};
        when(sqlAttribute.getNativeSql()).thenReturn(sql);
        when(physicalConnection.prepareStatement(sql , columnIndexes)).thenThrow(SQLException.class);

        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement(sql, columnIndexes));

        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any());
    }

    @Test
    void prepareStatement_EmptySQL_ThrowsSQLException() throws SQLException {
        when(physicalConnection.prepareStatement(any() , any(int[].class))).thenThrow(SQLException.class);
        assertThrows(SQLException.class, () -> proxyConnection.prepareStatement("", new int[]{1, 2}));
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any(), any());
    }

    @Test
    void prepareStatement_EmptyColumnIndexes_NoException() throws SQLException {
        String sql = "SELECT * FROM table";
        PreparedStatement ps = proxyConnection.prepareStatement(sql, new int[]{});
        assertNotNull(ps);
    }

    @Test
    void createPreparedStatementWithArgs_ThrowsSQLException() throws Exception {
        String sql = "SELECT * FROM table";
        Method method = proxyConnection.getClass().getDeclaredMethod("createPreparedStatementWithArgs", Connection.class, String.class, Object[].class);
        method.setAccessible(true);

        assertThrows(SQLException.class, () -> {
            try {
                method.invoke(proxyConnection, physicalConnection, sql, new Object[]{1 , "test" , 3 , "test"});
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    void prepareStatement_ResultSetType_ResultSetConcurrency_ResultSetHoldability() throws Exception {
        String sql = "SELECT * FROM table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString() , anyInt() , anyInt() , anyInt())).thenReturn(preparedStatement);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);

        PreparedStatement ps = proxyConnection.prepareStatement(sql , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY , ResultSet.CLOSE_CURSORS_AT_COMMIT);

        assertNotNull(ps);
        assertInstanceOf(ProxyPreparedStatement.class , ps);
        assertNotNull(((ProxyPreparedStatement) ps).getDelegate());
        assertNotNull(((ProxyPreparedStatement) ps).getPreparedStatementInfo());
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getNativeSql() , sql);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getStatement() , preparedStatement);

        verify(physicalConnection , times(1)).prepareStatement(sql , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any() , any());
    }

    @Test
    void prepareStatement_ResultSetType_ResultSetConcurrency() throws Exception {
        String sql = "SELECT * FROM table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString() , anyInt() , anyInt())).thenReturn(preparedStatement);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);

        PreparedStatement ps = proxyConnection.prepareStatement(sql , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY);

        assertNotNull(ps);
        assertInstanceOf(ProxyPreparedStatement.class , ps);
        assertNotNull(((ProxyPreparedStatement) ps).getDelegate());
        assertNotNull(((ProxyPreparedStatement) ps).getPreparedStatementInfo());
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getNativeSql() , sql);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getStatement() , preparedStatement);

        verify(physicalConnection , times(1)).prepareStatement(sql , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY);
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any() , any());
    }

    @Test
    void prepareStatement_ColumnNames() throws Exception {
        String sql = "SELECT * FROM table";
        String[] columnNames = {"c1" , "c2", "c3"};
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(sql , columnNames)).thenReturn(preparedStatement);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);

        PreparedStatement ps = proxyConnection.prepareStatement(sql , columnNames);

        assertNotNull(ps);
        assertInstanceOf(ProxyPreparedStatement.class , ps);
        assertNotNull(((ProxyPreparedStatement) ps).getDelegate());
        assertNotNull(((ProxyPreparedStatement) ps).getPreparedStatementInfo());
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getNativeSql() , sql);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getStatement() , preparedStatement);

        verify(physicalConnection , times(1)).prepareStatement(sql , columnNames);
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any() , any());
    }

    @Test
    void prepareStatement_ColumnIndexes() throws Exception {
        String sql = "SELECT * FROM table";
        int[] columnIndexes = {0 , 1, 2};
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(sql , columnIndexes)).thenReturn(preparedStatement);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);

        PreparedStatement ps = proxyConnection.prepareStatement(sql , columnIndexes);

        assertNotNull(ps);
        assertInstanceOf(ProxyPreparedStatement.class , ps);
        assertNotNull(((ProxyPreparedStatement) ps).getDelegate());
        assertNotNull(((ProxyPreparedStatement) ps).getPreparedStatementInfo());
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getNativeSql() , sql);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getStatement() , preparedStatement);

        verify(physicalConnection , times(1)).prepareStatement(sql , columnIndexes);
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any() , any());
    }

    @Test
    void prepareStatement_AutoGeneratedKeys() throws Exception {
        String sql = "SELECT * FROM table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(sql , Statement.RETURN_GENERATED_KEYS)).thenReturn(preparedStatement);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);

        PreparedStatement ps = proxyConnection.prepareStatement(sql , Statement.RETURN_GENERATED_KEYS);

        assertNotNull(ps);
        assertInstanceOf(ProxyPreparedStatement.class , ps);
        assertNotNull(((ProxyPreparedStatement) ps).getDelegate());
        assertNotNull(((ProxyPreparedStatement) ps).getPreparedStatementInfo());
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getNativeSql() , sql);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getStatement() , preparedStatement);

        verify(physicalConnection , times(1)).prepareStatement(sql , Statement.RETURN_GENERATED_KEYS);
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any() , any());
    }

    @Test
    void testPrepareStatement() throws Exception {

        String sql = "SELECT * FROM table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(physicalConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(sqlAttribute.getNativeSql()).thenReturn(sql);

        PreparedStatement ps = proxyConnection.prepareStatement(sql);

        assertNotNull(ps);
        assertInstanceOf(ProxyPreparedStatement.class , ps);
        assertNotNull(((ProxyPreparedStatement) ps).getDelegate());
        assertNotNull(((ProxyPreparedStatement) ps).getPreparedStatementInfo());
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementNs() > 0);
        assertTrue(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getBeforeTimeToCreateStatementMillis() > 0);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getNativeSql() , sql);
        assertEquals(((ProxyPreparedStatement) ps).getPreparedStatementInfo().getStatement() , preparedStatement);

        verify(physicalConnection , times(1)).prepareStatement(anyString());
        verify(eventListener, times(1)).onBeforePrepareStatement(any());
        verify(eventListener, times(1)).onAfterPrepareStatement(any() , any());
    }

    @Test
    void testCreateStatement_With_ResultSetType_ResultSetConcurrency_ResultSetHoldability() throws Exception {
        Statement statement = proxyConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY , ResultSet.HOLD_CURSORS_OVER_COMMIT);
        assertNotNull(statement);
        assertInstanceOf(ProxyStatement.class , statement);
        assertEquals(ResultSet.TYPE_FORWARD_ONLY , statement.getResultSetType());
        assertEquals(ResultSet.CONCUR_READ_ONLY , statement.getResultSetConcurrency());
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT , statement.getResultSetHoldability());
        verify(sqlXDataSource, never()).getDataSource(anyString());
        verify(eventListener, never()).onBeforeCreateStatement(any());
        verify(eventListener, never()).onAfterCreateStatement(any(), any());
    }

    @Test
    void testCreateStatement_With_ResultSetType_ResultSetConcurrency() throws Exception {
        Statement statement = proxyConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_UPDATABLE);
        assertNotNull(statement);
        assertInstanceOf(ProxyStatement.class , statement);
        assertEquals(ResultSet.TYPE_FORWARD_ONLY , statement.getResultSetType());
        assertEquals(ResultSet.CONCUR_UPDATABLE , statement.getResultSetConcurrency());
        verify(sqlXDataSource, never()).getDataSource(anyString());
        verify(eventListener, never()).onBeforeCreateStatement(any());
        verify(eventListener, never()).onAfterCreateStatement(any(), any());
    }

    @Test
    void testCreateStatement() throws Exception {
        Statement statement = proxyConnection.createStatement();
        assertNotNull(statement);
        assertInstanceOf(ProxyStatement.class , statement);
        verify(sqlXDataSource, never()).getDataSource(anyString());
        verify(eventListener, never()).onBeforeCreateStatement(any());
        verify(eventListener, never()).onAfterCreateStatement(any(), any());
    }

    @Test
    void testGetMetaData_WhenPhysicalConnectionIsNull_ShouldReturnNewRoutedConnection() throws Exception {
        setPrivateField(proxyConnection , "physicalConnection", null);
        assertNull(proxyConnection.getPhysicalConnection());
        DatabaseMetaData metaData = proxyConnection.getMetaData();
        assertNotNull(metaData);
        assertNotNull(routedConnection.getConnection());
        assertEquals(metaData, proxyConnection.getMetaData());
        verify(routedDataSource , times(1)).getConnection();
        verify(eventListener, times(1)).onBeforeGetConnection(any());
        verify(eventListener, times(1)).onAfterGetConnection(any() , any());
    }

    @Test
    void testGetMetaData_WhenPhysicalConnectionIsNotNull_ShouldReturnExistingRoutedConnection() throws SQLException, NoSuchFieldException, IllegalAccessException {
        setPrivateField(proxyConnection , "physicalConnection", physicalConnection);
        assertNotNull(proxyConnection.getPhysicalConnection());
        DatabaseMetaData metaData = proxyConnection.getMetaData();
        assertNotNull(metaData);
        assertNotNull(routedConnection.getConnection());
        assertEquals(metaData, proxyConnection.getMetaData());
        verify(routedDataSource, never()).getConnection();
        verify(eventListener, never()).onBeforeGetConnection(any());
        verify(eventListener, never()).onAfterGetConnection(any() , any());
    }

    @Test
    void testGetMetaData_WhenSQLExceptionOccurs_ShouldThrowSQLException() throws Exception {
        setPrivateField(proxyConnection , "physicalConnection", null);
        SQLException sqlException = new SQLException("test exception");
        when(routedDataSource.getConnection()).thenThrow(sqlException);
        assertThrows(SQLException.class, () -> proxyConnection.getMetaData());
        ConnectionInfo connectionInfo = proxyConnection.getConnectionInfo();
        verify(routedDataSource , times(1)).getConnection();
        verify(eventListener, times(1)).onBeforeGetConnection(any());
        verify(eventListener, times(1)).onAfterGetConnection(connectionInfo , sqlException);
    }

    @Test
    void testGetConnection_WhenPhysicalConnectionIsNull_ShouldReturnNewRoutedConnection() throws Exception {
        setPrivateField(proxyConnection , "physicalConnection", null);
        assertNull(proxyConnection.getPhysicalConnection());
        RoutedConnection routedConnection = proxyConnection.getConnection("select * from table");
        assertNotNull(routedConnection);
        assertNotNull(routedConnection.getConnection());
        assertEquals(physicalConnection, routedConnection.getConnection());
        verify(routedDataSource , times(1)).getConnection();
        verify(eventListener, times(1)).onBeforeGetConnection(any());
        verify(eventListener, times(1)).onAfterGetConnection(any() , any());
    }

    @Test
    void testGetConnection_WhenPhysicalConnectionIsNotNull_ShouldReturnExistingRoutedConnection() throws SQLException, NoSuchFieldException, IllegalAccessException {
        setPrivateField(proxyConnection , "physicalConnection", physicalConnection);
        RoutedConnection routedConnection = proxyConnection.getConnection("SELECT * FROM table");
        assertNotNull(routedConnection);
        assertNotNull(routedConnection.getConnection());
        assertEquals(physicalConnection, routedConnection.getConnection());
        verify(routedDataSource, never()).getConnection();
        verify(eventListener, never()).onBeforeGetConnection(any());
        verify(eventListener, never()).onAfterGetConnection(any() , any());
    }

    @Test
    void testGetConnection_WhenSQLExceptionOccurs_ShouldThrowSQLException() throws Exception {
        setPrivateField(proxyConnection , "physicalConnection", null);
        SQLException sqlException = new SQLException("test exception");
        when(routedDataSource.getConnection()).thenThrow(sqlException);
        assertThrows(SQLException.class, () -> proxyConnection.getConnection("SELECT * FROM table"));
        ConnectionInfo connectionInfo = proxyConnection.getConnectionInfo();
        verify(routedDataSource , times(1)).getConnection();
        verify(eventListener, times(1)).onBeforeGetConnection(any());
        verify(eventListener, times(1)).onAfterGetConnection(connectionInfo , sqlException);
    }

    @Test
    void testAcquireConnection_WithUsernameAndPassword() throws Exception {
        setPrivateField(proxyConnection , "username", "uname");
        setPrivateField(proxyConnection , "password", "pwd");

        Method method = proxyConnection.getClass().getDeclaredMethod("acquireConnection", DataSource.class);
        method.setAccessible(true);
        Object retVal = method.invoke(proxyConnection, routedDataSource);
        ConnectionInfo connectionInfo = proxyConnection.getConnectionInfo();

        assertNotNull(retVal);
        assertEquals(retVal , physicalConnection);
        assertNotNull(connectionInfo);
        assertTrue(connectionInfo.getBeforeTimeToGetConnectionNs() > 0);
        assertTrue(connectionInfo.getBeforeTimeToGetConnectionMillis() > 0);
        assertTrue(connectionInfo.getAfterTimeToGetConnectionNs() > 0);
        assertTrue(connectionInfo.getAfterTimeToGetConnectionMillis() > 0);
        verify(routedDataSource, times(1)).getConnection(anyString() , anyString());
        verify(eventListener, times(1)).onBeforeGetConnection(connectionInfo);
        verify(eventListener, times(1)).onAfterGetConnection(connectionInfo , null);

    }

    @Test
    void testAcquireConnection_WhenPhysicalConnectionIsNull_ShouldReturnNewRoutedConnection() throws Exception {
        setPrivateField(proxyConnection , "physicalConnection", null);

        Method method = proxyConnection.getClass().getDeclaredMethod("acquireConnection", DataSource.class);
        method.setAccessible(true);
        Object retVal = method.invoke(proxyConnection, routedDataSource);
        ConnectionInfo connectionInfo = proxyConnection.getConnectionInfo();

        assertNotNull(retVal);
        assertEquals(retVal , physicalConnection);
        assertNotNull(connectionInfo);
        assertTrue(connectionInfo.getBeforeTimeToGetConnectionNs() > 0);
        assertTrue(connectionInfo.getBeforeTimeToGetConnectionMillis() > 0);
        assertTrue(connectionInfo.getAfterTimeToGetConnectionNs() > 0);
        assertTrue(connectionInfo.getAfterTimeToGetConnectionMillis() > 0);
        verify(eventListener, times(1)).onBeforeGetConnection(connectionInfo);
        verify(eventListener, times(1)).onAfterGetConnection(connectionInfo , null);
    }

    @Test
    void testAcquireConnection_WhenSQLExceptionOccurs_ShouldThrowSQLException() throws Exception {
        setPrivateField(proxyConnection , "physicalConnection", null);
        SQLException sqlException = new SQLException("test exception");

        when(routedDataSource.getConnection()).thenThrow(sqlException);
        Method method = proxyConnection.getClass().getDeclaredMethod("acquireConnection", DataSource.class);
        method.setAccessible(true);

        assertThrows(SQLException.class, () -> {
            try {
                method.invoke(proxyConnection, routedDataSource);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });

        ConnectionInfo connectionInfo = proxyConnection.getConnectionInfo();

        assertNotNull(connectionInfo);
        assertTrue(connectionInfo.getBeforeTimeToGetConnectionNs() > 0);
        assertTrue(connectionInfo.getBeforeTimeToGetConnectionMillis() > 0);
        assertTrue(connectionInfo.getAfterTimeToGetConnectionNs() > 0);
        assertTrue(connectionInfo.getAfterTimeToGetConnectionMillis() > 0);
        verify(eventListener, times(1)).onBeforeGetConnection(connectionInfo);
        verify(eventListener, times(1)).onAfterGetConnection(connectionInfo , sqlException);
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
