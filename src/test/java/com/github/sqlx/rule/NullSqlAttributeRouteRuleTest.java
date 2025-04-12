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
 * Tests for {@link NullSqlAttributeRouteRule}.
 * This class tests the routing logic when SQL attributes are null.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class NullSqlAttributeRouteRuleTest {

    private LoadBalance readLoadBalance;
    private LoadBalance writeLoadBalance;
    private NullSqlAttributeRouteRule nullSqlAttributeRouteRule;

    @BeforeEach
    void setUp() {
        readLoadBalance = mock(LoadBalance.class);
        writeLoadBalance = mock(LoadBalance.class);
        nullSqlAttributeRouteRule = new NullSqlAttributeRouteRule(1, readLoadBalance, writeLoadBalance);
    }

    @Test
    void testRoutingWithNullSqlAttribute() {
        NodeAttribute expectedNode = mock(NodeAttribute.class);
        when(writeLoadBalance.choose()).thenReturn(expectedNode);

        NodeAttribute nodeAttribute = nullSqlAttributeRouteRule.routing(null);
        assertNotNull(nodeAttribute);
        assertEquals(expectedNode, nodeAttribute);
    }

    @Test
    void testRoutingWithNonNullSqlAttribute() {
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);

        NodeAttribute nodeAttribute = nullSqlAttributeRouteRule.routing(sqlAttribute);
        assertNull(nodeAttribute);
    }
}