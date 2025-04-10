package com.github.sqlx.rule.group;

import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.rule.*;
import com.github.sqlx.sql.parser.SqlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ClusterRoutingGroupBuilder}.
 * This class tests the building logic of ClusterRoutingGroupBuilder.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class ClusterRoutingGroupBuilderTest {

    private SqlXConfiguration configuration;
    private SqlParser sqlParser;
    private Transaction transaction;
    private LoadBalance readLoadBalance;
    private LoadBalance writeLoadBalance;
    private ClusterRoutingGroupBuilder builder;

    @BeforeEach
    void setUp() {
        configuration = mock(SqlXConfiguration.class);
        sqlParser = mock(SqlParser.class);
        transaction = mock(Transaction.class);
        readLoadBalance = mock(LoadBalance.class);
        writeLoadBalance = mock(LoadBalance.class);
        builder = ClusterRoutingGroupBuilder.builder()
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
        assertEquals(5, routeGroup.getRules().size());

        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof TransactionRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof ForceRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof ReadWriteSplittingRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof NullSqlAttributeRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof RouteWritableRule));
    }
}