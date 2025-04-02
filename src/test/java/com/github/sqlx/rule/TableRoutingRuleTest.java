//package com.github.sql.routing.rule;
//
//import com.github.sql.routing.config.RoutingConfiguration;
//import com.github.sql.routing.config.RoutingRuleConfiguration;
//import com.github.sql.routing.config.SqlTypeConfiguration;
//import com.github.sql.routing.sql.SqlType;
//import com.github.sql.routing.sql.parser.JSqlParser;
//import com.github.sql.routing.sql.parser.SqlParser;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.setAllowComparingPrivateFields;
//
///**
// * @author He Xing Mo
// * @since 1.0
// */
//class TableRoutingRuleTest {
//
//    @Test
//    void testRoutingPositive() {
//
//        SqlParser sqlParser = new JSqlParser();
//        RoutingConfiguration routingConfiguration = new RoutingConfiguration();
//        RoutingRuleConfiguration routingRuleConfiguration = new RoutingRuleConfiguration();
//
//        Map<String, Map<String, SqlTypeConfiguration>> tableRule = new HashMap<>();
//        Map<String, SqlTypeConfiguration> sqlTypeMap = new HashMap<>();
//        SqlTypeConfiguration sqlTypeConfiguration = new SqlTypeConfiguration();
//        sqlTypeConfiguration.setAllowAllSqlTypes(false);
//        Set<SqlType> sqlTypes = new HashSet<>();
//        sqlTypes.add(SqlType.SELECT);
//        sqlTypeConfiguration.setSqlTypes(sqlTypes);
//        sqlTypeMap.put("read0" , sqlTypeConfiguration);
//        sqlTypeMap.put("read1" , sqlTypeConfiguration);
//        sqlTypeMap.put("read2" , sqlTypeConfiguration);
//        tableRule.put("employee" , sqlTypeMap);
//        routingRuleConfiguration.setTables(tableRule);
//        routingConfiguration.setRules(routingRuleConfiguration);
//        TableRoutingRule routingRule = new TableRoutingRule(sqlParser , routingConfiguration);
//        String sql = "select * from employee where name = 'DevX'";
//        String result = routingRule.routing(sqlParser.parse(sql));
//
//        setAllowComparingPrivateFields(true);
//        assertThat(sqlTypeMap).containsKey(result);
//
//
//    }
//
//    @Test
//    void testRoutingNegative() {
//
//        SqlParser sqlParser = new JSqlParser();
//
//        RoutingConfiguration routingConfiguration = new RoutingConfiguration();
//
//        RoutingRuleConfiguration routingRuleConfiguration = new RoutingRuleConfiguration();
//        Map<String, Map<String, SqlTypeConfiguration>> tableRule = new HashMap<>();
//        Map<String, SqlTypeConfiguration> sqlTypeMap = new HashMap<>();
//        SqlTypeConfiguration sqlTypeConfiguration = new SqlTypeConfiguration();
//        sqlTypeConfiguration.setAllowAllSqlTypes(false);
//        Set<SqlType> sqlTypes = new HashSet<>();
//        sqlTypes.add(SqlType.SELECT);
//        sqlTypeConfiguration.setSqlTypes(sqlTypes);
//        sqlTypeMap.put("read0" , sqlTypeConfiguration);
//        sqlTypeMap.put("read1" , sqlTypeConfiguration);
//        sqlTypeMap.put("read2" , sqlTypeConfiguration);
//        tableRule.put("employee" , sqlTypeMap);
//        routingRuleConfiguration.setTables(tableRule);
//        routingConfiguration.setRules(routingRuleConfiguration);
//
//        TableRoutingRule routingRule = new TableRoutingRule(sqlParser , routingConfiguration);
//
//        String sql = "SELECT e.* , u.* \n" +
//                "FROM dept e , users u\n" +
//                "WHERE e.user_id = u.id;";
//        String result = routingRule.routing(sqlParser.parse(sql));
//
//        assertThat(sqlTypeMap).doesNotContainKeys(result);
//    }
//}