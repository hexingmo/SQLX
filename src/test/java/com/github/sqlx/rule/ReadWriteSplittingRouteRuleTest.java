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
 * Tests for {@link ReadWriteSplittingRouteRule}.
 * This class tests the routing logic for read-write splitting.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class ReadWriteSplittingRouteRuleTest {

    private SqlParser sqlParser;
    private LoadBalance readLoadBalance;
    private LoadBalance writeLoadBalance;
    private ReadWriteSplittingRouteRule readWriteSplittingRouteRule;

    @BeforeEach
    void setUp() {
        sqlParser = mock(SqlParser.class);
        readLoadBalance = mock(LoadBalance.class);
        writeLoadBalance = mock(LoadBalance.class);
        readWriteSplittingRouteRule = new ReadWriteSplittingRouteRule(1, sqlParser, readLoadBalance, writeLoadBalance);
    }

    @Test
    void testRoutingWithWriteSqlAttribute() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        NodeAttribute expectedNode = mock(NodeAttribute.class);

        when(sqlAttribute.isWrite()).thenReturn(true);
        when(writeLoadBalance.choose()).thenReturn(expectedNode);

        NodeAttribute nodeAttribute = readWriteSplittingRouteRule.routing(sqlAttribute);
        assertNotNull(nodeAttribute);
        assertEquals(expectedNode, nodeAttribute);
    }

    @Test
    void testRoutingWithReadSqlAttribute() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        NodeAttribute expectedNode = mock(NodeAttribute.class);

        when(sqlAttribute.isWrite()).thenReturn(false);
        when(readLoadBalance.choose()).thenReturn(expectedNode);

        NodeAttribute nodeAttribute = readWriteSplittingRouteRule.routing(sqlAttribute);
        assertNotNull(nodeAttribute);
        assertEquals(expectedNode, nodeAttribute);
    }

    @Test
    void testRoutingWithNullSqlAttribute() {
        NodeAttribute nodeAttribute = readWriteSplittingRouteRule.routing(null);
        assertNull(nodeAttribute);
    }
}