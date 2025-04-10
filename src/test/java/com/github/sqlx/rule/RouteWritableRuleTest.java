package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;
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

    private SqlParser sqlParser;
    private LoadBalance readLoadBalance;
    private LoadBalance writeLoadBalance;
    private RouteWritableRule routeWritableRule;

    @BeforeEach
    void setUp() {
        sqlParser = mock(SqlParser.class);
        readLoadBalance = mock(LoadBalance.class);
        writeLoadBalance = mock(LoadBalance.class);
        routeWritableRule = new RouteWritableRule(1, sqlParser, readLoadBalance, writeLoadBalance);
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