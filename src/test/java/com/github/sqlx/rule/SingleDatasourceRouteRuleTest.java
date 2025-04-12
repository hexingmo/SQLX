package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.jdbc.datasource.DataSourceWrapper;
import com.github.sqlx.jdbc.datasource.DatasourceManager;
import com.github.sqlx.sql.SqlAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SingleDatasourceRouteRule}.
 * This class tests the routing logic when there is a single data source.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class SingleDatasourceRouteRuleTest {

    private DatasourceManager datasourceManager;
    private SingleDatasourceRouteRule singleDatasourceRouteRule;

    @BeforeEach
    void setUp() {
        datasourceManager = mock(DatasourceManager.class);
        singleDatasourceRouteRule = new SingleDatasourceRouteRule(1, datasourceManager);
    }

    @Test
    void testRoutingWithSingleDataSource() {
        NodeAttribute expectedNode = mock(NodeAttribute.class);
        DataSourceWrapper dataSourceWrapper = mock(DataSourceWrapper.class);
        when(dataSourceWrapper.getNodeAttribute()).thenReturn(expectedNode);
        when(datasourceManager.getDataSourceList()).thenReturn(Collections.singletonList(dataSourceWrapper));

        NodeAttribute nodeAttribute = singleDatasourceRouteRule.routing(mock(SqlAttribute.class));
        assertNotNull(nodeAttribute);
        assertEquals(expectedNode, nodeAttribute);
    }

    @Test
    void testRoutingWithMultipleDataSources() {
        DataSourceWrapper dataSourceWrapper1 = mock(DataSourceWrapper.class);
        DataSourceWrapper dataSourceWrapper2 = mock(DataSourceWrapper.class);
        List<DataSourceWrapper> dataSourceList = new ArrayList<>(Arrays.asList(dataSourceWrapper1, dataSourceWrapper2));
        when(datasourceManager.getDataSourceList()).thenReturn(dataSourceList);

        NodeAttribute nodeAttribute = singleDatasourceRouteRule.routing(mock(SqlAttribute.class));
        assertNull(nodeAttribute);
    }

    @Test
    void testRoutingWithNoDataSources() {
        when(datasourceManager.getDataSourceList()).thenReturn(Collections.emptyList());

        NodeAttribute nodeAttribute = singleDatasourceRouteRule.routing(mock(SqlAttribute.class));
        assertNull(nodeAttribute);
    }
}