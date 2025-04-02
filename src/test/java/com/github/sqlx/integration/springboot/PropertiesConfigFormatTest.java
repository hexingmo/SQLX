package com.github.sqlx.integration.springboot;

import com.github.sqlx.NodeType;
import com.github.sqlx.config.DataSourceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * testing properties config file
 * @author He Xing Mo
 * @since 1.0
 */

@TestPropertySource(locations = "classpath:application.properties")
@Slf4j
class PropertiesConfigFormatTest extends SpringBootIntegrationTest {

    @Autowired
    SqlXProperties properties;

    @Test
    void testPropertiesFileDataSourcesConfig() {

        log.info("testing DataSourceConfiguration using spring boot properties config file");

        assertThat(properties).isNotNull();
        Assertions.assertThat(properties.getConfig().getDataSources()).isNotEmpty();
        Assertions.assertThat(properties.getConfig().getDataSourceNames()).containsOnly("write_0" , "read_0" , "read_1");

        assertWrite0();
        assertRead0();
        assertRead1();
    }

//    @Test
//    void testPropertiesFileTableRoutingRuleConfig() {
//
//        log.info("testing Table Routing Rule using spring boot properties config file");
//
//        assertThat(properties.getRouting().getRules()).isNotNull();
//        Map<String, Map<String, SqlTypeConfiguration>> tables = properties.getRouting().getRules().getTables();
//        assertThat(tables).isNotEmpty().containsOnlyKeys("employee");
//
//        for (Map.Entry<String, Map<String, SqlTypeConfiguration>> entry : tables.entrySet()) {
//            Map<String, SqlTypeConfiguration> datasourceMap = entry.getValue();
//            assertThat(datasourceMap).isNotEmpty();
//            assertThat(datasourceMap).containsOnlyKeys("write_0" , "read_0" , "read_1");
//            SqlTypeConfiguration write0 = datasourceMap.get("write_0");
//            assertThat(write0).isNotNull();
//            assertThat(write0.getAllowAllSqlTypes()).isNull();
//            assertThat(write0.getSqlTypes()).containsOnly(SqlType.INSERT , SqlType.UPDATE , SqlType.DELETE , SqlType.OTHER);
//
//            SqlTypeConfiguration read0 = datasourceMap.get("read_0");
//            assertThat(read0).isNotNull();
//            assertThat(read0.getAllowAllSqlTypes()).isNull();
//            assertThat(read0.getSqlTypes()).containsOnly(SqlType.SELECT);
//
//            SqlTypeConfiguration read1 = datasourceMap.get("read_1");
//            assertThat(read1).isNotNull();
//            assertThat(read1.getAllowAllSqlTypes()).isNull();
//            assertThat(read1.getSqlTypes()).containsOnly(SqlType.SELECT);
//        }
//    }

    private void assertWrite0() {
        DataSourceConfiguration write0Configuration = properties.getConfig().getDataSourceConfByName("write_0");
        assertThat(write0Configuration).isNotNull();
        Assertions.assertThat(write0Configuration.getType()).isEqualTo(NodeType.READ_WRITE);
        assertThat(write0Configuration.getDataSourceClass()).isEqualTo("com.zaxxer.hikari.HikariDataSource");
        assertThat(write0Configuration.getWeight()).isEqualTo(99);

        Map<String, String> write0Props = write0Configuration.getProps();
        assertThat(write0Props).isNotNull();
        assertThat(write0Props).extracting("jdbcUrl").isEqualTo("jdbc:h2:mem:~/test1;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;");
        assertThat(write0Props).extracting("username").isEqualTo("sa");
        assertThat(write0Props).extracting("password").isEqualTo("");
        assertThat(write0Props).extracting("minIdle").isEqualTo("6");
        assertThat(write0Props).extracting("maxPoolSize").isEqualTo("30");
        assertThat(write0Props).extracting("connectionTimeout").isEqualTo("30000");
        assertThat(write0Props).extracting("isAutoCommit").isEqualTo("false");
        assertThat(write0Props).extracting("isReadOnly").isEqualTo("false");

        log.info("write_0 DataSourceConfiguration testing pass {} " , write0Configuration);
    }

//    @Test
//    void testDatabaseRoutingRuleConfig() {
//        log.info("testing Database Routing Rule using spring boot properties config file");
//        assertThat(properties.getRouting().getRules()).isNotNull();
//        List<DatabaseRoutingConfiguration> databases = properties.getRouting().getRules().getDatabases();
//        assertThat(databases).isNotEmpty();
//        Stream<String> databaseNames = databases.stream().map(DatabaseRoutingConfiguration::getName);
//        assertThat(databaseNames).containsOnly("test1" , "test2" , "test3");
//
//        DatabaseRoutingConfiguration test1Rule = databases.get(0);
//        assertThat(test1Rule).isNotNull().extracting(DatabaseRoutingConfiguration::getName).isEqualTo("test1");
//        Map<String, SqlTypeConfiguration> test1Map = test1Rule.getNodes();
//        assertThat(test1Map).containsOnlyKeys("write_0");
//        SqlTypeConfiguration write0SqlTypeConf = test1Map.get("write_0");
//        assertThat(write0SqlTypeConf).isNotNull()
//                .extracting(SqlTypeConfiguration::getAllowAllSqlTypes).isEqualTo(true);
//
//        DatabaseRoutingConfiguration test2Rule = databases.get(1);
//        assertThat(test2Rule).isNotNull().extracting(DatabaseRoutingConfiguration::getName).isEqualTo("test2");
//        Map<String, SqlTypeConfiguration> test2Map = test2Rule.getNodes();
//        assertThat(test2Map).containsOnlyKeys("read_0");
//        SqlTypeConfiguration read0SqlTypeConf = test2Map.get("read_0");
//        assertThat(read0SqlTypeConf).isNotNull()
//                .extracting(SqlTypeConfiguration::getAllowAllSqlTypes).isEqualTo(true);
//
//        DatabaseRoutingConfiguration test3Rule = databases.get(2);
//        assertThat(test3Rule).isNotNull().extracting(DatabaseRoutingConfiguration::getName).isEqualTo("test3");
//        Map<String, SqlTypeConfiguration> test3Map = test3Rule.getNodes();
//        assertThat(test3Map).containsOnlyKeys("read_1");
//        SqlTypeConfiguration read1SqlTypeConf = test3Map.get("read_1");
//        assertThat(read1SqlTypeConf).isNotNull()
//                .extracting(SqlTypeConfiguration::getAllowAllSqlTypes).isNull();
//        assertThat(read1SqlTypeConf.getSqlTypes()).containsOnly(SqlType.SELECT);
//    }

    private void assertRead0() {
        DataSourceConfiguration read0Configuration = properties.getConfig().getDataSourceConfByName("read_0");
        assertThat(read0Configuration).isNotNull();
        Assertions.assertThat(read0Configuration.getType()).isEqualTo(NodeType.READ);
        assertThat(read0Configuration.getDataSourceClass()).isEqualTo("com.zaxxer.hikari.HikariDataSource");
        assertThat(read0Configuration.getWeight()).isEqualTo(6);

        Map<String, String> read0Props = read0Configuration.getProps();
        assertThat(read0Props).isNotNull();
        assertThat(read0Props).extracting("jdbcUrl").isEqualTo("jdbc:h2:mem:~/test2;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;");
        assertThat(read0Props).extracting("username").isEqualTo("sa");
        assertThat(read0Props).extracting("password").isEqualTo("");
        assertThat(read0Props).extracting("minIdle").isEqualTo("10");
        assertThat(read0Props).extracting("maxPoolSize").isEqualTo("30");
        assertThat(read0Props).extracting("connectionTimeout").isEqualTo("40000");
        assertThat(read0Props).extracting("isAutoCommit").isEqualTo("false");
        assertThat(read0Props).extracting("isReadOnly").isEqualTo("true");

        log.info("read_0 DataSourceConfiguration testing pass {} " , read0Configuration);
    }

    private void assertRead1() {
        DataSourceConfiguration read1Configuration = properties.getConfig().getDataSourceConfByName("read_1");
        assertThat(read1Configuration).isNotNull();
        Assertions.assertThat(read1Configuration.getType()).isEqualTo(NodeType.READ);
        assertThat(read1Configuration.getDataSourceClass()).isEqualTo("com.zaxxer.hikari.HikariDataSource");
        assertThat(read1Configuration.getWeight()).isEqualTo(10);

        Map<String, String> read1Props = read1Configuration.getProps();
        assertThat(read1Props).isNotNull();
        assertThat(read1Props).extracting("jdbcUrl").isEqualTo("jdbc:h2:mem:~/test3;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;");
        assertThat(read1Props).extracting("username").isEqualTo("sa");
        assertThat(read1Props).extracting("password").isEqualTo("");
        assertThat(read1Props).extracting("minIdle").isEqualTo("15");
        assertThat(read1Props).extracting("maxPoolSize").isEqualTo("30");
        assertThat(read1Props).extracting("connectionTimeout").isEqualTo("60000");
        assertThat(read1Props).extracting("isAutoCommit").isEqualTo("false");
        assertThat(read1Props).extracting("isReadOnly").isEqualTo("true");

        log.info("read_1 DataSourceConfiguration testing pass {} " , read1Configuration);
    }

}
