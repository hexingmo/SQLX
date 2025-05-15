package io.github.sqlx.jdbc;

import io.github.sqlx.jdbc.datasource.RoutedDataSource;
import io.github.sqlx.jdbc.datasource.SqlXDataSource;
import io.github.sqlx.listener.EventListener;
import io.github.sqlx.rule.RouteInfo;
import io.github.sqlx.sql.SqlAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ProxyStatement}.
 * @author He Xing Mo
 * @since 1.0
 */
class ProxyStatementTest {

    @Mock
    private SqlXDataSource sqlXDataSource;

    @Mock
    private EventListener eventListener;

    @InjectMocks
    private ProxyStatement proxyStatement;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        ProxyConnection proxyConnection = mock(ProxyConnection.class);
        when(sqlXDataSource.getConnection()).thenReturn(proxyConnection);
        RoutedConnection routedConnection = mock(RoutedConnection.class);
        when(proxyConnection.getConnection(anyString())).thenReturn(routedConnection);

        ConnectionInfo connectionInfo = mock(ConnectionInfo.class);
        when(proxyConnection.getConnectionInfo()).thenReturn(connectionInfo);

        Connection connection = mock(Connection.class);
        when(routedConnection.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(mock(Statement.class));
        when(connection.createStatement(anyInt() , anyInt())).thenReturn(mock(Statement.class));
        when(connection.createStatement(anyInt() , anyInt() , anyInt())).thenReturn(mock(Statement.class));

        RoutedDataSource routedDataSource = mock(RoutedDataSource.class);
        when(routedConnection.getRoutedDataSource()).thenReturn(routedDataSource);

        RouteInfo routeInfo = mock(RouteInfo.class);
        when(routedDataSource.getRouteInfo()).thenReturn(routeInfo);
        when(routeInfo.getSqlAttribute()).thenReturn(Mockito.mock(SqlAttribute.class));

        proxyStatement = new ProxyStatement(sqlXDataSource, eventListener);
    }

    @Test
    void acquireStatement() throws Exception {

        String sql = "SELECT * FROM table";
        Method method = ProxyStatement.class.getDeclaredMethod("acquireStatement", String.class);
        method.setAccessible(true);
        Object val = method.invoke(proxyStatement, sql);
        assertNotNull(val);
        assertInstanceOf(StatementInfo.class, val);
        verify(eventListener, times(1)).onBeforeCreateStatement(any());
        verify(eventListener, times(1)).onAfterCreateStatement(any(), any());
    }
}