package com.github.sqlx.rule.group;

import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.jdbc.datasource.DatasourceManager;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.rule.*;
import com.github.sqlx.sql.parser.SqlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link NoneClusterRoutingGroupBuilder}.
 * This class tests the building logic of DefaultRoutingGroupBuilder.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class NoneClusterRoutingGroupBuilderTest {

    private SqlXConfiguration configuration;
    private SqlParser sqlParser;
    private Transaction transaction;
    private DatasourceManager datasourceManager;
    private NoneClusterRoutingGroupBuilder builder;

    @BeforeEach
    void setUp() {
        configuration = mock(SqlXConfiguration.class);
        sqlParser = mock(SqlParser.class);
        transaction = mock(Transaction.class);
        datasourceManager = mock(DatasourceManager.class);
        builder = NoneClusterRoutingGroupBuilder.builder()
                .sqlXConfiguration(configuration)
                .sqlParser(sqlParser)
                .transaction(transaction)
                .datasourceManager(datasourceManager);
    }

    @Test
    void testBuild() {
        DefaultRouteGroup routeGroup = builder.build();

        assertNotNull(routeGroup);
        assertEquals(5, routeGroup.getRules().size());

        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof SingleDatasourceRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof TransactionRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof ForceRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof DataSourceNameSqlHintRouteRule));
        assertTrue(routeGroup.getRules().stream().anyMatch(rule -> rule instanceof DefaultDataSourceRouteRule));
    }
}