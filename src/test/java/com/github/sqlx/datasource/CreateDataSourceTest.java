package com.github.sqlx.datasource;

import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.integration.datasource.CompositeDataSourceInitializer;
import com.github.sqlx.integration.datasource.GenericDataSourceInitializer;
import com.github.sqlx.integration.datasource.ReflectNameMatchesDataSourceInitializer;
import com.github.sqlx.integration.datasource.SpringDataSourceInitializer;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.*;

import static com.github.sqlx.utils.DataSourceTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


/**
 * create datasource test
 *
 * @author jing yun
 * @see com.github.sqlx.integration.datasource.ReflectNameMatchesDataSourceInitializer
 * @see com.github.sqlx.integration.datasource.SpringDataSourceInitializer
 * @see com.github.sqlx.integration.datasource.CompositeDataSourceInitializer
 * @since 1.0
 */
public class CreateDataSourceTest {

    static String initSqlPath = "src/test/resources/init.sql";

    static DataSourceConfiguration dsConfig;


    @Test
    public void testReflectNameMatchesDataSourceInitializer() throws Exception {
        // create datasource
        ReflectNameMatchesDataSourceInitializer dataSourceInitializer = new ReflectNameMatchesDataSourceInitializer();
        DataSource dataSource = dataSourceInitializer.initialize(dsConfig);
        // init datasource
        executeSqlScript(dataSource, initSqlPath);
        // test datasource
        testAndClearDatasource(dataSource);
    }

    @Test
    public void testSpringDataSourceInitializer() throws Exception {
        // create datasource
        SpringDataSourceInitializer springDataSourceInitializer = new SpringDataSourceInitializer();
        DataSource dataSource = springDataSourceInitializer.initialize(dsConfig);
        // init datasource
        executeSqlScript(dataSource, initSqlPath);
        // test datasource
        testAndClearDatasource(dataSource);
    }

    @Test
    public void testCompositeDataSourceInitializer() throws Exception {
        // create datasource
        Set<GenericDataSourceInitializer<?>> initializers = new HashSet<>();
        initializers.add(new ReflectNameMatchesDataSourceInitializer());
        initializers.add(new SpringDataSourceInitializer());
        CompositeDataSourceInitializer compositeDataSourceInitializer = new CompositeDataSourceInitializer(initializers);
        DataSource dataSource = compositeDataSourceInitializer.initialize(dsConfig);
        // init datasource
        executeSqlScript(dataSource, initSqlPath);
        // test datasource
        testAndClearDatasource(dataSource);
    }

    @BeforeAll
    public static void getHikariDataSourceConfig() {
        Map<String, String> properties = new HashMap<>(10);
        properties.put("jdbcUrl", "jdbc:h2:mem:test");
        properties.put("driverClassName", "org.h2.Driver");
        properties.put("username", "sa");
        properties.put("password", "");
        properties.put("minIdle", "5");
        properties.put("maxPoolSize", "30");
        properties.put("connectionTimeout", "30000");
        properties.put("isAutoCommit", "false");
        properties.put("poolName", "p-hikari");

        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration();
        dataSourceConfig.setDataSourceClass(HikariDataSource.class.getCanonicalName());
        dataSourceConfig.setProps(properties);
        dsConfig = dataSourceConfig;
    }


    private void testAndClearDatasource(DataSource dataSource) throws Exception {
        String sql = "select id,name from area where id in (1,2)";
        List<Map<String, Object>> results = executeSqlQuery(dataSource, sql);
        assertThat(results)
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(1L, "East"),
                        tuple(2L, "South")
                );
        dropAllObjects(dataSource);
    }

}
