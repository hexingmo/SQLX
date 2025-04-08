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

import java.lang.reflect.Field;
import java.sql.Connection;

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
        when(routedDataSource.getConnection()).thenReturn(physicalConnection);

        when(routedConnection.getRoutedDataSource()).thenReturn(routedDataSource);
        when(routedConnection.getConnection()).thenReturn(physicalConnection);
//        when(proxyConnection.getConnection(anyString())).thenReturn(routedConnection);

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

    private void setPrivateField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
