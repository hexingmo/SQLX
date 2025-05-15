package io.github.sqlx.integration.springboot;

import io.github.sqlx.RoutingContext;
import io.github.sqlx.jdbc.datasource.DatasourceManager;
import io.github.sqlx.jdbc.datasource.SqlXDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@TestPropertySource(locations = "classpath:application.yaml")
public class BeforeAfterEachHandleDataTest extends SpringBootIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    DatasourceManager datasourceManager;

    //@BeforeEach
    protected void initData() throws Exception {

        SqlXDataSource dataSource;
        if (this.dataSource.isWrapperFor(DelegatingDataSource.class)) {
            dataSource = (SqlXDataSource) ((DelegatingDataSource) this.dataSource).getTargetDataSource();
        } else {
            dataSource = (SqlXDataSource) this.dataSource;
        }
        String initSqlPath = "src/test/resources/init.sql";

        DataSource write0 = datasourceManager.getDataSource("write_0");

        Connection write0Conn = write0.getConnection();
        ResultSet write0Rs = RunScript.execute(write0Conn, new FileReader(initSqlPath));
        close(write0Rs, null, write0Conn);

        DataSource read0 = datasourceManager.getDataSource("read_0");

        Connection read0Conn = read0.getConnection();
        ResultSet read0Rs = RunScript.execute(read0Conn, new FileReader(initSqlPath));
        close(read0Rs, null, read0Conn);

        DataSource read1 = datasourceManager.getDataSource("read_1");
        Connection read1Conn = read1.getConnection();
        ResultSet read1Rs = RunScript.execute(read1Conn, new FileReader(initSqlPath));
        close(read1Rs, null, read1Conn);
    }

    @AfterEach
    protected void clearData() throws Exception {

        SqlXDataSource dataSource;
        if (this.dataSource.isWrapperFor(DelegatingDataSource.class)) {
            dataSource = (SqlXDataSource) ((DelegatingDataSource) this.dataSource).getTargetDataSource();
        } else {
            dataSource = (SqlXDataSource) this.dataSource;
        }
        RoutingContext.clear();
        DataSource write0 = datasourceManager.getDataSource("write_0");
        Connection write0Conn = write0.getConnection();
        Statement write0Stmt = write0Conn.createStatement();
        write0Stmt.execute("SHUTDOWN");


        DataSource read0 = datasourceManager.getDataSource("read_0");
        Connection read0Conn = read0.getConnection();
        Statement read0Stmt = read0Conn.createStatement();
        read0Stmt.execute("SHUTDOWN");

        DataSource read1 = datasourceManager.getDataSource("read_1");
        Connection read1Conn = read1.getConnection();
        Statement read1Stmt = read1Conn.createStatement();
        read1Stmt.execute("SHUTDOWN");
    }

    private static void close(ResultSet rs, Statement stmt, Connection conn) throws Exception {
        if (Objects.nonNull(rs)) {
            rs.close();
        }
        if (Objects.nonNull(stmt)) {
            stmt.close();
        }
        if (Objects.nonNull(conn)) {
            conn.close();
        }
    }

}
