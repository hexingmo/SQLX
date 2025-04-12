package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.RoutingContext;
import com.github.sqlx.config.ClusterConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.exception.SqlRouteException;
import com.github.sqlx.integration.springboot.RouteAttribute;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.sql.SqlAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link TransactionRouteRule}.
 *
 * Author: He Xing Mo
 * Version: 1.0
 */
class TransactionRouteRuleTest {

    private SqlXConfiguration configuration;
    private Transaction transaction;
    private TransactionRouteRule transactionRouteRule;

    @BeforeEach
    void setUp() {
        configuration = mock(SqlXConfiguration.class);
        transaction = mock(Transaction.class);
        transactionRouteRule = new TransactionRouteRule(1, configuration, transaction);
    }

    @Test
    void testRoutingWithActiveTransactionAndCurrentNode() {
        NodeAttribute currentNode = mock(NodeAttribute.class);
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);

        when(transaction.isActive()).thenReturn(true);
        when(transaction.getCurrentNode()).thenReturn(currentNode);

        NodeAttribute nodeAttribute = transactionRouteRule.routing(sqlAttribute);
        assertNotNull(nodeAttribute);
        assertEquals(currentNode, nodeAttribute);
        verify(transaction).addSql(sqlAttribute);
    }

    @Test
    void testRoutingWithActiveTransactionAndNoCurrentNode() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setNodes(Collections.singletonList("DataSource1"));
        NodeAttribute expectedNode = mock(NodeAttribute.class);

        when(transaction.isActive()).thenReturn(true);
        when(transaction.getCurrentNode()).thenReturn(null);
        when(configuration.getNodeAttribute("DataSource1")).thenReturn(expectedNode);

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);

            NodeAttribute nodeAttribute = transactionRouteRule.routing(sqlAttribute);
            assertNotNull(nodeAttribute);
            assertEquals(expectedNode, nodeAttribute);
            verify(transaction).registerNode(expectedNode, sqlAttribute);
        }
    }

    @Test
    void testRoutingWithInactiveTransaction() {
        when(transaction.isActive()).thenReturn(false);

        NodeAttribute nodeAttribute = transactionRouteRule.routing(mock(SqlAttribute.class));
        assertNull(nodeAttribute);
    }

    @Test
    void testRoutingWithNoRouteAttribute() {
        when(transaction.isActive()).thenReturn(true);
        when(transaction.getCurrentNode()).thenReturn(null);

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(null);

            assertThrows(SqlRouteException.class, () -> transactionRouteRule.routing(mock(SqlAttribute.class)));
        }
    }

    @Test
    void testRoutingWithActiveTransactionAndCluster() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setCluster("Cluster1");
        ClusterConfiguration clusterConfiguration = mock(ClusterConfiguration.class);
        NodeAttribute expectedNode = mock(NodeAttribute.class);
        Set<NodeAttribute> writableNodes = new HashSet<>(Collections.singletonList(expectedNode));

        when(transaction.isActive()).thenReturn(true);
        when(transaction.getCurrentNode()).thenReturn(null);
        when(configuration.getCluster("Cluster1")).thenReturn(clusterConfiguration);
        when(clusterConfiguration.getWritableRoutingNodeAttributes()).thenReturn(writableNodes);

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);

            NodeAttribute nodeAttribute = transactionRouteRule.routing(sqlAttribute);
            assertNotNull(nodeAttribute);
            assertEquals(expectedNode, nodeAttribute);
            verify(transaction).registerNode(expectedNode, sqlAttribute);
        }
    }

    @Test
    void testRoutingWithActiveTransactionAndEmptyCluster() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        RouteAttribute routeAttribute = new RouteAttribute();
        routeAttribute.setCluster("Cluster1");
        ClusterConfiguration clusterConfiguration = mock(ClusterConfiguration.class);

        when(transaction.isActive()).thenReturn(true);
        when(transaction.getCurrentNode()).thenReturn(null);
        when(configuration.getCluster("Cluster1")).thenReturn(clusterConfiguration);
        when(clusterConfiguration.getWritableRoutingNodeAttributes()).thenReturn(Collections.emptySet());

        try (MockedStatic<RoutingContext> mockedContext = Mockito.mockStatic(RoutingContext.class)) {
            mockedContext.when(RoutingContext::getRoutingAttribute).thenReturn(routeAttribute);

            assertThrows(SqlRouteException.class, () -> transactionRouteRule.routing(sqlAttribute));
        }
    }
}