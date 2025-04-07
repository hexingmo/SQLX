package com.github.sqlx.jdbc.datasource;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.cluster.Cluster;
import com.github.sqlx.cluster.ClusterManager;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.rule.RouteRule;
import com.github.sqlx.rule.RoutingKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DefaultSqlXDataSource}.
 * This class tests the routing logic of DefaultSqlXDataSource.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class DefaultSqlXDataSourceTest {

    private ClusterManager clusterManager;
    private DatasourceManager datasourceManager;
    private EventListener eventListener;
    private RouteRule routeRule;
    private DefaultSqlXDataSource sqlXDataSource;

    @BeforeEach
    void setUp() {
        clusterManager = mock(ClusterManager.class);
        datasourceManager = mock(DatasourceManager.class);
        eventListener = mock(EventListener.class);
        routeRule = mock(RouteRule.class);
        sqlXDataSource = new DefaultSqlXDataSource(clusterManager, datasourceManager, eventListener, routeRule);
    }

    @Test
    void testGetDataSourceWithoutCluster() {
        String sql = "SELECT * FROM users";
        NodeAttribute nodeAttribute = mock(NodeAttribute.class);
        DataSourceWrapper dataSourceWrapper = mock(DataSourceWrapper.class);
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setHitNodeAttr(nodeAttribute);

        when(clusterManager.getDefaultCluster()).thenReturn(null);
        when(routeRule.route(any(RoutingKey.class))).thenReturn(routeInfo);
        when(nodeAttribute.getName()).thenReturn("DataSource1");
        when(datasourceManager.getDataSource("DataSource1")).thenReturn(dataSourceWrapper);

        RoutedDataSource routedDataSource = sqlXDataSource.getDataSource(sql);
        assertNotNull(routedDataSource);
        assertEquals(dataSourceWrapper, routedDataSource.getDelegate());
    }

    @Test
    void testGetDataSourceWithCluster() {
        String sql = "SELECT * FROM users";
        Cluster cluster = mock(Cluster.class);
        NodeAttribute nodeAttribute = mock(NodeAttribute.class);
        DataSourceWrapper dataSourceWrapper = mock(DataSourceWrapper.class);
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setHitNodeAttr(nodeAttribute);

        when(clusterManager.getDefaultCluster()).thenReturn(cluster);
        when(cluster.getRule()).thenReturn(routeRule);
        when(routeRule.route(any(RoutingKey.class))).thenReturn(routeInfo);
        when(nodeAttribute.getName()).thenReturn("DataSource1");
        when(datasourceManager.getDataSource("DataSource1")).thenReturn(dataSourceWrapper);

        RoutedDataSource routedDataSource = sqlXDataSource.getDataSource(sql);
        assertNotNull(routedDataSource);
        assertEquals(dataSourceWrapper, routedDataSource.getDelegate());
    }

    @Test
    void testGetDataSourceForDatabaseMetaDataWithoutCluster() {
        DataSourceWrapper dataSourceWrapper = mock(DataSourceWrapper.class);
        when(datasourceManager.isSameDatabaseProduct()).thenReturn(true);
        when(datasourceManager.getDataSourceList()).thenReturn(Collections.singletonList(dataSourceWrapper));

        RoutedDataSource routedDataSource = sqlXDataSource.getDataSourceForDatabaseMetaData();
        assertNotNull(routedDataSource);
        assertEquals(dataSourceWrapper, routedDataSource.getDelegate());
    }

    @Test
    void testGetDataSourceForDatabaseMetaDataWithCluster() {
        Cluster cluster = mock(Cluster.class);
        NodeAttribute nodeAttribute = mock(NodeAttribute.class);
        DataSourceWrapper dataSourceWrapper = mock(DataSourceWrapper.class);

        when(clusterManager.getDefaultCluster()).thenReturn(cluster);
        when(cluster.getNodes()).thenReturn(Collections.singleton(nodeAttribute));
        when(nodeAttribute.getName()).thenReturn("DataSource1");
        when(datasourceManager.getDataSource("DataSource1")).thenReturn(dataSourceWrapper);

        RoutedDataSource routedDataSource = sqlXDataSource.getDataSourceForDatabaseMetaData();
        assertNotNull(routedDataSource);
        assertEquals(dataSourceWrapper, routedDataSource.getDelegate());
    }
}