package io.github.sqlx.utils;

import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * datasource test utils
 *
 * @author jing yun
 * @since 1.0
 */
public class DataSourceTestUtils {

    public static void executeSqlScript(DataSource dataSource, String sqlPath) {
        TestUtils.runInTry(() -> {
            Connection conn = dataSource.getConnection();
            ResultSet rs = RunScript.execute(conn, new FileReader(sqlPath));
            TestUtils.closeResources(rs, conn);
        });
    }


    public static List<Map<String, Object>> executeSqlQuery(DataSource dataSource, String sql) throws Exception {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        ResultSet rs = stmt.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        List<Map<String, Object>> resultList = new ArrayList<>();
        while (rs.next()) {
            resultList.add(IntStream.range(1, columnCount + 1)
                    .boxed()
                    .collect(Collectors.toMap(
                            index -> TestUtils.callInTry(() -> metaData.getColumnLabel(index).toLowerCase()),
                            index -> TestUtils.callInTry(() -> rs.getObject(index)))));
        }
        TestUtils.closeResources(rs, stmt, conn);
        return resultList;
    }

    public static void dropAllObjects(DataSource dataSource) {
        TestUtils.runInTry(() -> {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            Statement stmt = conn.createStatement();
            stmt.execute("DROP ALL OBJECTS");
            TestUtils.closeResources(null, stmt, conn);
        });
    }
}
