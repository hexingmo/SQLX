package io.github.sqlx.rule;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.sql.AnnotationSqlAttribute;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.parser.SqlHint;
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

    private SqlXConfiguration configuration;
    private DataSourceNameSqlHintRouteRule dataSourceNameSqlHintRouteRule;

    @BeforeEach
    void setUp() {
        configuration = mock(SqlXConfiguration.class);
        dataSourceNameSqlHintRouteRule = new DataSourceNameSqlHintRouteRule(1, configuration);
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