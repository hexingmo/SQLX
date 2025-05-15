package io.github.sqlx.rule.group;

import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.jdbc.datasource.DatasourceManager;
import io.github.sqlx.jdbc.transaction.Transaction;
import io.github.sqlx.sql.parser.SqlParser;
import io.github.sqlx.rule.DataSourceNameSqlHintRouteRule;
import io.github.sqlx.rule.DefaultDataSourceRouteRule;
import io.github.sqlx.rule.ForceRouteRule;
import io.github.sqlx.rule.SingleDatasourceRouteRule;
import io.github.sqlx.rule.TransactionRouteRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link NoneClusterRouteGroupBuilder}.
 * This class tests the building logic of DefaultRoutingGroupBuilder.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class NoneClusterRouteGroupBuilderTest {

    private SqlXConfiguration configuration;
    private SqlParser sqlParser;
    private Transaction transaction;
    private DatasourceManager datasourceManager;
    private NoneClusterRouteGroupBuilder builder;

    @BeforeEach
    void setUp() {
        configuration = mock(SqlXConfiguration.class);
        sqlParser = mock(SqlParser.class);
        transaction = mock(Transaction.class);
        datasourceManager = mock(DatasourceManager.class);
        builder = NoneClusterRouteGroupBuilder.builder()
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