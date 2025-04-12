package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.RoutingContext;
import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.integration.springboot.RouteAttribute;
import com.github.sqlx.sql.AnnotationSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;
import com.github.sqlx.util.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ForceRouteRule}.
 * This class tests the routing logic of ForceRouteRule.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class ForceRouteRuleTest {

    private SqlXConfiguration configuration;
    private SqlParser sqlParser;
    private ForceRouteRule forceRouteRule;

    @BeforeEach
    void setUp() {
        configuration = new SqlXConfiguration();
        DataSourceConfiguration dsConfig1 = new DataSourceConfiguration();
        dsConfig1.setName("DataSource1");
        dsConfig1.setDataSourceClass("org.h2.jdbcx.JdbcDataSource");
        HashMap<String, String> props1 = new HashMap<>();
        props1.put("url", "jdbc:h2:mem:testdb1");
        props1.put("driverClassName", "org.h2.Driver");
        props1.put("username", "sa");
        props1.put("password", "password");
        dsConfig1.setProps(props1);

        DataSourceConfiguration dsConfig2 = new DataSourceConfiguration();
        dsConfig2.setName("DataSource2");
        dsConfig2.setDataSourceClass("org.h2.jdbcx.JdbcDataSource");
        HashMap<String, String> props2 = new HashMap<>();
        props2.put("url", "jdbc:h2:mem:testdb2");
        props2.put("driverClassName", "org.h2.Driver");
        props2.put("username", "sa");
        props2.put("password", "password");
        dsConfig2.setProps(props2);

        configuration.setDataSources(new ArrayList<>(Arrays.asList(dsConfig1, dsConfig2)));
        configuration.init();

        sqlParser = mock(SqlParser.class);
        forceRouteRule = new ForceRouteRule(1, sqlParser, configuration);
    }

    @Test
    void testAnnotationSql_ShouldReturnNull() {
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(new ArrayList<>(Arrays.asList("DataSource1", "DataSource2")));

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);
            NodeAttribute nodeAttribute = forceRouteRule.routing(mock(AnnotationSqlAttribute.class));
            assertNull(nodeAttribute);
        }
    }

    @Test
    void testRoutingWithSingleNode() {
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(Collections.singletonList("DataSource1"));

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);

            NodeAttribute nodeAttribute = forceRouteRule.routing(mock(SqlAttribute.class));
            assertNotNull(nodeAttribute);
            assertEquals("DataSource1", nodeAttribute.getName());
        }
    }

    @Test
    void testRoutingWithMultipleNodes() {
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(new ArrayList<>(Arrays.asList("DataSource1", "DataSource2")));

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class);
             MockedStatic<RandomUtils> mockedRandom = Mockito.mockStatic(RandomUtils.class)) {

            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);
            mockedRandom.when(() -> RandomUtils.nextInt(0, 2)).thenReturn(1);

            NodeAttribute nodeAttribute = forceRouteRule.routing(mock(SqlAttribute.class));
            assertNotNull(nodeAttribute);
            assertEquals("DataSource2", nodeAttribute.getName());
        }
    }

    @Test
    void testRoutingWithNoNodes() {
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(Collections.emptyList());

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);

            NodeAttribute nodeAttribute = forceRouteRule.routing(mock(SqlAttribute.class));
            assertNull(nodeAttribute);
        }
    }

    @Test
    void testRoutingWithNullRouteAttribute() {
        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(null);

            NodeAttribute nodeAttribute = forceRouteRule.routing(mock(SqlAttribute.class));
            assertNull(nodeAttribute);
        }
    }
}