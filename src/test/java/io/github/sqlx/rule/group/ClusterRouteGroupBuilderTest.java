package io.github.sqlx.rule.group;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.RoutingContext;
import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.integration.springboot.RouteAttribute;
import io.github.sqlx.jdbc.transaction.Transaction;
import io.github.sqlx.rule.RouteInfo;
import io.github.sqlx.loadbalance.LoadBalance;
import io.github.sqlx.sql.AnnotationSqlAttribute;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.parser.SqlHint;
import io.github.sqlx.sql.parser.SqlParser;
import io.github.sqlx.rule.DataSourceNameSqlHintRouteRule;
import io.github.sqlx.rule.ForceRouteRule;
import io.github.sqlx.rule.NullSqlAttributeRouteRule;
import io.github.sqlx.rule.ReadWriteSplittingRouteRule;
import io.github.sqlx.rule.RouteWritableRule;
import io.github.sqlx.rule.RoutingKey;
import io.github.sqlx.rule.TransactionRouteRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ClusterRouteGroupBuilder}.
 * This class tests the building logic of ClusterRoutingGroupBuilder.
 *
 * Author: He Xing Mo
 * Version: 1.0
 */
class ClusterRouteGroupBuilderTest {

    private SqlXConfiguration configuration;
    private SqlParser sqlParser;
    private Transaction transaction;
    private LoadBalance readLoadBalance;
    private LoadBalance writeLoadBalance;
    private ClusterRouteGroupBuilder builder;

    @BeforeEach
    void setUp() {
        configuration = mock(SqlXConfiguration.class);
        sqlParser = mock(SqlParser.class);
        transaction = mock(Transaction.class);
        readLoadBalance = mock(LoadBalance.class);
        writeLoadBalance = mock(LoadBalance.class);

        when(sqlParser.parse(anyString())).thenReturn(Mockito.mock(SqlAttribute.class));

        builder = ClusterRouteGroupBuilder.builder()
                .sqlXConfiguration(configuration)
                .sqlParser(sqlParser)
                .transaction(transaction)
                .readLoadBalance(readLoadBalance)
                .writeLoadBalance(writeLoadBalance);
    }

    @Test
    void testBuild() {
        DefaultRouteGroup routeGroup = builder.build();

        assertNotNull(routeGroup);
        assertEquals(6, routeGroup.getRules().size());

        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof TransactionRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof DataSourceNameSqlHintRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof ForceRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof ReadWriteSplittingRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof NullSqlAttributeRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof RouteWritableRule));
    }

    @Test
    void testForceRouteRuleEffective() {
        DefaultRouteGroup routeGroup = builder.build();
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(Collections.singletonList("DataSource1"));

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);
            when(configuration.getNodeAttribute("DataSource1")).thenReturn(Mockito.mock(NodeAttribute.class));
            RoutingKey routingKey = new RoutingKey().setSql("SELECT * FROM table");
            RouteInfo routeInfo = routeGroup.route(routingKey);
            assertNotNull(routeInfo);
            assertInstanceOf(ForceRouteRule.class , routeInfo.getHitRule());
        }
    }

    @Test
    void testForceRouteRuleIneffective() {
        DefaultRouteGroup routeGroup = builder.build();
        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(null);
            RoutingKey routingKey = new RoutingKey().setSql("SELECT * FROM table");
            RouteInfo routeInfo = routeGroup.route(routingKey);
            assertNotNull(routeInfo);
            assertFalse(routeInfo.getHitRule() instanceof ForceRouteRule);
        }
    }

    @Test
    void testDataSourceNameSqlHintRouteRuleEffective() {
        DefaultRouteGroup routeGroup = builder.build();
        RoutingKey routingKey = new RoutingKey().setSql("/*!nodeName=DS1;*/ SELECT * FROM table");
        AnnotationSqlAttribute sqlAttribute = mock(AnnotationSqlAttribute.class);
        when(sqlParser.parse(anyString())).thenReturn(sqlAttribute);
        SqlHint sqlHint = new SqlHint();
        Map<String, String> hints = new HashMap<>();
        hints.put("nodeName", "DS1");
        sqlHint.setHints(hints);
        when(sqlAttribute.getSqlHint()).thenReturn(sqlHint);
        when(configuration.getNodeAttribute("DS1")).thenReturn(mock(NodeAttribute.class));

        RouteInfo routeInfo = routeGroup.route(routingKey);
        assertNotNull(routeInfo);
        assertInstanceOf(DataSourceNameSqlHintRouteRule.class , routeInfo.getHitRule());
    }

    @Test
    void testDataSourceNameSqlHintRouteRuleIneffective() {
        DefaultRouteGroup routeGroup = builder.build();
        RoutingKey routingKey = new RoutingKey().setSql("SELECT * FROM table");
        AnnotationSqlAttribute sqlAttribute = mock(AnnotationSqlAttribute.class);
        when(sqlParser.parse(anyString())).thenReturn(sqlAttribute);
        when(sqlAttribute.getSqlHint()).thenReturn(mock(SqlHint.class));

        RouteInfo routeInfo = routeGroup.route(routingKey);
        assertNotNull(routeInfo);
        assertFalse(routeInfo.getHitRule() instanceof DataSourceNameSqlHintRouteRule);
    }

    @Test
    void testTransactionRouteRuleEffective() {
        DefaultRouteGroup routeGroup = builder.build();
        RoutingKey routingKey = new RoutingKey().setSql("SELECT * FROM table");
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(Collections.singletonList("DataSource1"));
        when(transaction.isActive()).thenReturn(true);
        when(configuration.getNodeAttribute("DataSource1")).thenReturn(mock(NodeAttribute.class));

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);

            RouteInfo routeInfo = routeGroup.route(routingKey);
            assertNotNull(routeInfo);
            assertInstanceOf(TransactionRouteRule.class , routeInfo.getHitRule());
        }
    }

    @Test
    void testTransactionRouteRuleInactive() {
        DefaultRouteGroup routeGroup = builder.build();
        RoutingKey routingKey = new RoutingKey().setSql("SELECT * FROM table");
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(Collections.singletonList("DataSource1"));
        when(transaction.isActive()).thenReturn(false);
        when(configuration.getNodeAttribute("DataSource1")).thenReturn(mock(NodeAttribute.class));

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);

            RouteInfo routeInfo = routeGroup.route(routingKey);
            assertNotNull(routeInfo);
            assertFalse(routeInfo.getHitRule() instanceof TransactionRouteRule);
        }
    }
}