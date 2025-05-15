package io.github.sqlx.rule.group;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.NodeState;
import io.github.sqlx.rule.RouteInfo;
import io.github.sqlx.rule.SqlAttributeRouteRule;
import io.github.sqlx.rule.RoutingKey;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.parser.SqlParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DefaultRouteGroup}.
 * This class tests the routing logic of DefaultRouteGroup.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class DefaultRouteGroupTest {

    private SqlParser sqlParser;
    private DefaultRouteGroup defaultRouteGroup;

    @BeforeEach
    void setUp() {
        sqlParser = mock(SqlParser.class);
        defaultRouteGroup = new DefaultRouteGroup(sqlParser);
    }

    @Test
    void testRouteWithValidSql() {
        RoutingKey routingKey = new RoutingKey();
        routingKey.setSql("SELECT * FROM users");
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        NodeAttribute expectedNode = mock(NodeAttribute.class);
        SqlAttributeRouteRule routeRule = mock(SqlAttributeRouteRule.class);

        when(sqlParser.parse(routingKey.getSql())).thenReturn(sqlAttribute);
        when(routeRule.routing(sqlAttribute)).thenReturn(expectedNode);
        when(expectedNode.getNodeState()).thenReturn(NodeState.UP);

        defaultRouteGroup.install(routeRule);

        RouteInfo routeInfo = defaultRouteGroup.route(routingKey);
        assertNotNull(routeInfo);
        Assertions.assertEquals(expectedNode, routeInfo.getHitNodeAttr());
        assertEquals(routeRule, routeInfo.getHitRule());
    }

    @Test
    void testRouteWithUnavailableNode() {
        RoutingKey routingKey = new RoutingKey();
        routingKey.setSql("SELECT * FROM users");
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        NodeAttribute unavailableNode = mock(NodeAttribute.class);
        SqlAttributeRouteRule routeRule = mock(SqlAttributeRouteRule.class);

        when(sqlParser.parse(routingKey.getSql())).thenReturn(sqlAttribute);
        when(routeRule.routing(sqlAttribute)).thenReturn(unavailableNode);
        when(unavailableNode.getNodeState()).thenReturn(NodeState.DOWN);

        defaultRouteGroup.install(routeRule);

        RouteInfo routeInfo = defaultRouteGroup.route(routingKey);
        assertNull(routeInfo.getHitNodeAttr());
        assertNull(routeInfo.getHitRule());
    }

    @Test
    void testRouteWithNullSql() {
        RoutingKey routingKey = new RoutingKey();
        routingKey.setSql(null);

        RouteInfo routeInfo = defaultRouteGroup.route(routingKey);
        assertNull(routeInfo.getHitNodeAttr());
        assertNull(routeInfo.getHitRule());
    }

    @Test
    void testRouteWithNoMatchingRule() {
        RoutingKey routingKey = new RoutingKey();
        routingKey.setSql("SELECT * FROM users");
        SqlAttribute sqlAttribute = mock(SqlAttribute.class);
        SqlAttributeRouteRule routeRule = mock(SqlAttributeRouteRule.class);

        when(sqlParser.parse(routingKey.getSql())).thenReturn(sqlAttribute);
        when(routeRule.routing(sqlAttribute)).thenReturn(null);

        defaultRouteGroup.install(routeRule);

        RouteInfo routeInfo = defaultRouteGroup.route(routingKey);
        assertNull(routeInfo.getHitNodeAttr());
        assertNull(routeInfo.getHitRule());
    }
}