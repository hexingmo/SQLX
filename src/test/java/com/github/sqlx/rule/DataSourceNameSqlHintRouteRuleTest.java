package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.sql.AnnotationSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlHint;
import com.github.sqlx.sql.parser.SqlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DataSourceNameSqlHintRouteRule}.
 * This class tests the routing logic based on SQL hints for data source names.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class DataSourceNameSqlHintRouteRuleTest {

    private SqlParser sqlParser;
    private SqlXConfiguration configuration;
    private DataSourceNameSqlHintRouteRule dataSourceNameSqlHintRouteRule;

    @BeforeEach
    void setUp() {
        sqlParser = mock(SqlParser.class);
        configuration = mock(SqlXConfiguration.class);
        dataSourceNameSqlHintRouteRule = new DataSourceNameSqlHintRouteRule(1, sqlParser, configuration);
    }

    @Test
    void testRoutingWithValidDataSourceNameHint() {
        SqlHint sqlHint = new SqlHint();
        Map<String, String> hints = new HashMap<>();
        hints.put("nodeName", "DataSource1");
        sqlHint.setHints(hints);

        AnnotationSqlAttribute sqlAttribute = mock(AnnotationSqlAttribute.class);
        NodeAttribute expectedNode = mock(NodeAttribute.class);

        when(sqlAttribute.getSqlHint()).thenReturn(sqlHint);
        when(configuration.getNodeAttribute("DataSource1")).thenReturn(expectedNode);

        NodeAttribute nodeAttribute = dataSourceNameSqlHintRouteRule.routing(sqlAttribute);
        assertNotNull(nodeAttribute);
        assertEquals(expectedNode, nodeAttribute);
    }

    @Test
    void testRoutingWithInvalidDataSourceNameHint() {
        SqlHint sqlHint = new SqlHint();
        Map<String, String> hints = new HashMap<>();
        hints.put("nodeName", "InvalidDataSource");
        sqlHint.setHints(hints);

        AnnotationSqlAttribute sqlAttribute = mock(AnnotationSqlAttribute.class);

        when(sqlAttribute.getSqlHint()).thenReturn(sqlHint);
        when(configuration.getNodeAttribute("InvalidDataSource")).thenReturn(null);

        NodeAttribute nodeAttribute = dataSourceNameSqlHintRouteRule.routing(sqlAttribute);
        assertNull(nodeAttribute);
    }

    @Test
    void testRoutingWithNullDataSourceNameHint() {
        SqlHint sqlHint = new SqlHint();
        AnnotationSqlAttribute sqlAttribute = mock(AnnotationSqlAttribute.class);

        when(sqlAttribute.getSqlHint()).thenReturn(sqlHint);

        NodeAttribute nodeAttribute = dataSourceNameSqlHintRouteRule.routing(sqlAttribute);
        assertNull(nodeAttribute);
    }

    @Test
    void testRoutingWithNullSqlAttribute() {
        NodeAttribute nodeAttribute = dataSourceNameSqlHintRouteRule.routing(null);
        assertNull(nodeAttribute);
    }

    @Test
    void testRoutingWithNoneAnnotationSqlAttribute() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        NodeAttribute nodeAttribute = dataSourceNameSqlHintRouteRule.routing(sqlAttribute);
        assertNull(nodeAttribute);
    }
}