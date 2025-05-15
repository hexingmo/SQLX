package io.github.sqlx.rule;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.loadbalance.LoadBalance;
import io.github.sqlx.sql.SqlAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link RouteWritableRule}.
 * This class tests the routing logic to ensure it always routes to a writable node.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class RouteWritableRuleTest {

    private LoadBalance readLoadBalance;
    private LoadBalance writeLoadBalance;
    private RouteWritableRule routeWritableRule;

    @BeforeEach
    void setUp() {
        readLoadBalance = mock(LoadBalance.class);
        writeLoadBalance = mock(LoadBalance.class);
        routeWritableRule = new RouteWritableRule(1, readLoadBalance, writeLoadBalance);
    }

    @Test
    void testRoutingAlwaysChoosesWriteNode() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        NodeAttribute expectedNode = mock(NodeAttribute.class);

        when(writeLoadBalance.choose()).thenReturn(expectedNode);

        NodeAttribute nodeAttribute = routeWritableRule.routing(sqlAttribute);
        assertNotNull(nodeAttribute);
        assertEquals(expectedNode, nodeAttribute);
    }

    @Test
    void testRoutingWithNullSqlAttribute() {
        NodeAttribute expectedNode = mock(NodeAttribute.class);

        when(writeLoadBalance.choose()).thenReturn(expectedNode);

        NodeAttribute nodeAttribute = routeWritableRule.routing(null);
        assertNotNull(nodeAttribute);
        assertEquals(expectedNode, nodeAttribute);
    }
}