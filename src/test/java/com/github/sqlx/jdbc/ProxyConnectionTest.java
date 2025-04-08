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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ProxyConnection}.
 *
 * Author: He Xing Mo
 * Version: 1.0
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

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        proxyConnection = new ProxyConnection(sqlXDataSource, eventListener);
        setPrivateField(proxyConnection, "physicalConnection", physicalConnection);

        when(sqlXDataSource.getDataSourceForDatabaseMetaData()).thenReturn(routedDataSource);
        when(sqlXDataSource.getDataSource(anyString())).thenReturn(routedDataSource);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfo);
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
