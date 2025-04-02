package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.sql.parser.JSqlParser;
import com.github.sqlx.sql.parser.SqlParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author He Xing Mo
 * @since 1.0
 */
class ForceTargetRouteRuleTest {

    @Test
    void routingPositive() {

        String[] dataSourceNames = {"write_0" , "write_1" , "write_2"};

        SqlXConfiguration sqlXConfiguration = new SqlXConfiguration();
        SqlParser sqlParser = new JSqlParser(sqlXConfiguration);
        ForceTargetRouteRule routingRule = new ForceTargetRouteRule(0 , sqlParser , null , null , sqlXConfiguration);

        String sql = "INSERT INTO area (id, name) VALUES (6, 'Birmingham')";
        NodeAttribute nodeAttribute = routingRule.routing(sqlParser.parse(sql));
        assertThat(dataSourceNames).contains(nodeAttribute.getName());
    }

    @Test
    void routingNegative () {

        String[] dataSourceNames = {"write_0" , "write_1" , "write_2"};


        SqlXConfiguration sqlXConfiguration = new SqlXConfiguration();
        SqlParser sqlParser = new JSqlParser(sqlXConfiguration);
        ForceTargetRouteRule routingRule = new ForceTargetRouteRule(0 , sqlParser , null , null , sqlXConfiguration);
        String sql = "INSERT INTO area (id, name) VALUES (6, 'Birmingham')";
        NodeAttribute nodeAttribute = routingRule.routing(sqlParser.parse(sql));
        assertThat(nodeAttribute).isNull();
    }

}