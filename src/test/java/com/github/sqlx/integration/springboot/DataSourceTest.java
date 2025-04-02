package com.github.sqlx.integration.springboot;

import com.github.sqlx.jdbc.datasource.SqlXDataSource;
import com.github.sqlx.jdbc.ProxyConnection;
import com.github.sqlx.jdbc.ProxyPreparedStatement;
import com.github.sqlx.jdbc.ProxyStatement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class DataSourceTest extends SpringBootIntegrationTest {

    @Autowired
    DataSource dataSource;



    @Test
    void testDataSource() {
        log.info("testing DataSource object [{}] isInstanceOf RoutingDataSource" , dataSource);
        assertThat(dataSource).isNotNull().isInstanceOf(SqlXDataSource.class);
    }

    @Test
    void testGetConnection() throws Exception {

        log.info("testing DataSource get connection isInstanceOf RoutingConnection");
        Connection connection = dataSource.getConnection();
        assertThat(connection).isNotNull().isInstanceOf(ProxyConnection.class);
        connection.close();
    }

    @Test
    void testPreparedStatement() throws Exception {
        log.info("testing PreparedStatement isInstanceOf RoutingContextClearPreparedStatement");
        Connection connection = dataSource.getConnection();
        assertThat(connection).isNotNull().isInstanceOf(ProxyConnection.class);
        PreparedStatement prepareStatement = connection.prepareStatement("show databases");
        assertThat(prepareStatement).isNotNull().isInstanceOf(ProxyPreparedStatement.class);
        prepareStatement.close();
        connection.close();
    }

    @Test
    void testStatement() throws Exception {
        log.info("testing Statement isInstanceOf RoutingStatement");
        Connection connection = dataSource.getConnection();
        assertThat(connection).isNotNull().isInstanceOf(ProxyConnection.class);
        Statement statement = connection.createStatement();
        assertThat(statement).isNotNull().isInstanceOf(ProxyStatement.class);
        statement.close();
        connection.close();
    }
}
