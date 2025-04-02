package com.github.sqlx.datasource;

import com.github.sqlx.NodeType;
import com.github.sqlx.RoutingContext;
import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.integration.datasource.ReflectNameMatchesDataSourceInitializer;
import com.github.sqlx.integration.springboot.RouteAttribute;
import com.github.sqlx.integration.springboot.SpringTransaction;
import com.github.sqlx.jdbc.datasource.DataSourceWrapper;
import com.github.sqlx.jdbc.datasource.DatasourceManager;
import com.github.sqlx.jdbc.datasource.SqlXDataSourceImpl;
import com.github.sqlx.jdbc.transaction.TimestampUUIDTransactionIdGenerator;
import com.github.sqlx.listener.DefaultEventListener;
import com.github.sqlx.loadbalance.ReadLoadBalanceType;
import com.github.sqlx.loadbalance.WriteLoadBalanceType;
import com.github.sqlx.rule.ForceTargetRouteRule;
import com.github.sqlx.rule.group.CompositeRouteGroup;
import com.github.sqlx.rule.group.DefaultRouteGroup;
import com.github.sqlx.rule.group.DefaultRoutingGroupBuilder;
import com.github.sqlx.sql.parser.JSqlParser;
import com.github.sqlx.sql.parser.SqlParser;
import com.github.sqlx.util.RoutingUtils;
import com.github.sqlx.utils.DataSourceTestUtils;
import com.github.sqlx.utils.TestUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.sqlx.utils.DataSourceTestUtils.dropAllObjects;
import static com.github.sqlx.utils.DataSourceTestUtils.executeSqlQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * include unit test and Benchmark test
 *
 * @author He Xing Mo
 * @see SqlXDataSourceImpl
 * @since 1.0
 */

@State(Scope.Benchmark)
@Slf4j
public class ProxyDataSourceImplTest {

    static DataSource dataSource;
    static List<DataSource> delegateDataSources = new ArrayList<>();

    static String writeDataSourceName = "write";
    static String readDataSource0Name = "read0";
    static String readDataSource1Name = "read1";

    static Random random = new Random();

    static String initSqlPath = "src/test/resources/init.sql";

    @BeforeAll
    public static void initProxyDataSource() {

        List<DataSourceConfiguration> dataSourceConfigs = new ArrayList<>();
        DataSourceConfiguration writeDSConf = new DataSourceConfiguration();
        writeDSConf.setDataSourceClass(HikariDataSource.class.getCanonicalName());
        writeDSConf.setName(writeDataSourceName);
        writeDSConf.setType(NodeType.READ_WRITE);
        writeDSConf.setWeight(1d);
        writeDSConf.setHeartbeatSql("select 1");
        writeDSConf.addProperty("jdbcUrl", "jdbc:h2:mem:~/test1;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;");
        dataSourceConfigs.add(writeDSConf);

        DataSourceConfiguration read0DSConf = new DataSourceConfiguration();
        read0DSConf.setDataSourceClass(HikariDataSource.class.getCanonicalName());
        read0DSConf.setName(readDataSource0Name);
        read0DSConf.setType(NodeType.READ);
        read0DSConf.setWeight(1d);
        read0DSConf.setHeartbeatSql("select 2");
        read0DSConf.addProperty("jdbcUrl", "jdbc:h2:mem:~/test2;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;");
        dataSourceConfigs.add(read0DSConf);

        DataSourceConfiguration read1DSConf = new DataSourceConfiguration();
        read1DSConf.setDataSourceClass(HikariDataSource.class.getCanonicalName());
        read1DSConf.setName(readDataSource1Name);
        read1DSConf.setType(NodeType.READ);
        read1DSConf.setWeight(1d);
        read1DSConf.setHeartbeatSql("select 3");
        read1DSConf.addProperty("jdbcUrl", "jdbc:h2:mem:~/test3;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;");
        dataSourceConfigs.add(read1DSConf);

        SqlXConfiguration conf = new SqlXConfiguration();
        conf.setDataSources(dataSourceConfigs);
        conf.setReadLoadBalanceType(ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_ONLY_READ);
        conf.setWriteLoadBalanceType(WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_READ_WRITE);
        conf.init();

        SqlParser sqlParser = new JSqlParser(conf);
        DefaultRouteGroup defaultRoutingGroup = new DefaultRouteGroup(sqlParser);
        defaultRoutingGroup.install(new ForceTargetRouteRule(1, sqlParser, conf.getReadLoadBalance(), conf.getWriteLoadBalance(), conf));
        DefaultEventListener defaultEventListener = new DefaultEventListener();

        CompositeRouteGroup compositeRoutingGroup = new CompositeRouteGroup(defaultEventListener, new SpringTransaction(new TimestampUUIDTransactionIdGenerator()));
        compositeRoutingGroup.installLast(defaultRoutingGroup);

        DatasourceManager dsManager = new DatasourceManager(conf);
        dataSourceConfigs.forEach(dsConfig -> dsManager.addDataSource(dsConfig.getName(), new DataSourceWrapper(dsConfig.getName(), initHikariDataSource(dsConfig.getProps()),
                conf.getRoutingNodeAttribute(writeDataSourceName), true)));
        dataSource = new SqlXDataSourceImpl(dsManager, defaultEventListener, compositeRoutingGroup);
    }

    @TearDown
    @AfterAll
    public static void clearDataBase() throws Exception {

    }

    @AfterEach
    void clearData() throws SQLException {
        for (DataSource dataSource : delegateDataSources) {
            dropAllObjects(dataSource);
        }
    }


    @Test
    void testGetEmployeeNamesAndDepartmentNamesByArea() throws Exception {
        RouteAttribute routeAttribute = new RouteAttribute(null, Arrays.asList(writeDataSourceName, readDataSource0Name, readDataSource1Name), true, true, null, null);
        RoutingContext.force(routeAttribute);
        String sql = "SELECT e.name AS employee_name, d.name AS department_name " +
                "FROM employee e " +
                "INNER JOIN department d ON e.department_id = d.id " +
                "WHERE d.area_id = 1";
        List<Map<String, Object>> results = executeSqlQuery(dataSource, sql);
        assertThat(results)
                .extracting("employee_name", "department_name")
                .containsExactlyInAnyOrder(
                        tuple("John Doe", "Research and Development"),
                        tuple("Jane Doe", "Research and Development")
                );
    }

    @Test
    void testTxInsert() throws Exception {

        String sql1 = "INSERT INTO area (id, name) VALUES (5, 'New York')";

        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        PreparedStatement stmt1 = conn.prepareStatement(sql1);

        int row1 = stmt1.executeUpdate();
        stmt1.close();
        assertThat(row1).isEqualTo(1);


        String sql2 = "INSERT INTO department (id, name, area_id) VALUES  (5, 'Research', 5)";
        PreparedStatement stmt2 = conn.prepareStatement(sql2);

        int row2 = stmt2.executeUpdate();
        stmt2.close();
        assertThat(row2).isEqualTo(1);

        String sql3 = "INSERT INTO employee (id, name, department_id) VALUES (6, 'Doge Lee', 5) ";
        PreparedStatement stmt3 = conn.prepareStatement(sql3);

        int row3 = stmt3.executeUpdate();
        stmt3.close();
        assertThat(row3).isEqualTo(1);

        String sql4 = "SELECT e.name AS employee_name, d.name AS department_name , a.name AS area_name " +
                "FROM employee e " +
                "INNER JOIN department d ON e.department_id = d.id " +
                "INNER JOIN area a ON d.area_id = a.id " +
                "WHERE e.id = ?";

        PreparedStatement stmt4 = conn.prepareStatement(sql4);
        stmt4.setInt(1, 6);
        ResultSet rs = stmt4.executeQuery();

        List<Map<String, Object>> resultList = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("employee_name", rs.getString("employee_name"));
            resultMap.put("department_name", rs.getString("department_name"));
            resultMap.put("area_name", rs.getString("area_name"));
            resultList.add(resultMap);
        }

        assertThat(resultList)
                .extracting("employee_name", "department_name", "area_name")
                .containsExactlyInAnyOrder(
                        tuple("Doge Lee", "Research", "New York")
                );

        close(rs, stmt4, conn);
    }

    @Test
    void testNoTxWriteAndRead() throws Exception {
        String sql1 = "INSERT INTO area (id, name) VALUES (6, 'Birmingham')";
        Connection conn1 = dataSource.getConnection();
        PreparedStatement stmt1 = conn1.prepareStatement(sql1);
        int row1 = stmt1.executeUpdate();
        assertThat(RoutingUtils.isRoutingWrite(conn1)).isTrue();
        assertThat(row1).isEqualTo(1);
        close(null, stmt1, conn1);


        String sql2 = "SELECT * FROM employee WHERE id = ?";
        Connection conn2 = dataSource.getConnection();
        PreparedStatement stmt2 = conn2.prepareStatement(sql2);
        assertThat(RoutingUtils.isRoutingRead(conn2)).isTrue();
        stmt2.setInt(1, 1);
        ResultSet rs = stmt2.executeQuery();
        List<Map<String, Object>> resultList = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("employee_name", rs.getString("name"));
            resultMap.put("employee_id", rs.getString("id"));
            resultList.add(resultMap);
        }

        assertThat(resultList)
                .extracting("employee_id", "employee_name")
                .containsExactlyInAnyOrder(
                        tuple("1", "John Doe")
                );

    }

    @Test
    void testTxReadonly() throws Exception {

        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        conn.setReadOnly(true);

        String sql1 = "SELECT * FROM employee WHERE id = ?";
        PreparedStatement stmt1 = conn.prepareStatement(sql1);
        stmt1.setInt(1, 1);

        assertThat(RoutingUtils.isRoutingRead(conn)).isTrue();

        ResultSet rs = stmt1.executeQuery();
        List<Map<String, Object>> resultList = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("employee_name", rs.getString("name"));
            resultMap.put("employee_id", rs.getString("id"));
            resultList.add(resultMap);
        }

        assertThat(resultList)
                .extracting("employee_id", "employee_name")
                .containsExactlyInAnyOrder(
                        tuple("1", "John Doe")
                );
    }

    @Test
    void testTxReadonlyNegative() throws Exception {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        conn.setReadOnly(true);

        String sql1 = "INSERT INTO area (id, name) VALUES (6, 'Manchester')";
        PreparedStatement stmt1 = conn.prepareStatement(sql1);

        assertThat(RoutingUtils.isRoutingRead(conn)).isTrue();

        int rows = stmt1.executeUpdate();
        assertThat(rows).isEqualTo(1);
        close(null, stmt1, conn);
    }

    @Test
    void testForceRoutingWriteDataSource() throws Exception {

        Connection conn = dataSource.getConnection();
        String sql1 = "SELECT * FROM employee WHERE id = ?";
        PreparedStatement stmt1 = conn.prepareStatement(sql1);
        stmt1.setInt(1, 1);

        assertThat(RoutingUtils.isRoutingWrite(conn)).isTrue();

        ResultSet rs = stmt1.executeQuery();
        List<Map<String, Object>> resultList = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("employee_name", rs.getString("name"));
            resultMap.put("employee_id", rs.getString("id"));
            resultList.add(resultMap);
        }

        assertThat(resultList)
                .extracting("employee_id", "employee_name")
                .containsExactlyInAnyOrder(
                        tuple("1", "John Doe")
                );
    }

    @Test
    void testConcurrentBehavior() throws Exception {
        int threads = 20;
        CyclicBarrier startBarrier = new CyclicBarrier(threads);
        List<Callable<List<Integer>>> tasks = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(10);
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                List<Integer> ids = new ArrayList<>();
                for (int j = 0; j < 10; j++) {
                    Connection conn = dataSource.getConnection();
                    conn.setAutoCommit(true);
                    conn.setReadOnly(false);

                    String sql1 = "INSERT INTO area (id, name) VALUES (? , 'Manchester')";
                    PreparedStatement stmt1 = conn.prepareStatement(sql1);
                    int id = count.incrementAndGet();
                    stmt1.setInt(1, id);
                    assertThat(RoutingUtils.isRoutingWrite(conn)).isTrue();
                    int rows = stmt1.executeUpdate();
                    assertThat(rows).isEqualTo(1);
                    close(null, stmt1, conn);
                    ids.add(id);
                }
                startBarrier.await();
                return ids;
            });
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<List<Integer>>> futures = executorService.invokeAll(tasks);
        List<Integer> idList = new ArrayList<>();
        for (Future<List<Integer>> future : futures) {
            idList.addAll(future.get());
        }

        executorService.shutdown();

        Connection conn = dataSource.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM area");
        List<Map<String, Object>> resultList = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", Integer.parseInt(rs.getString("id")));
            resultMap.put("name", rs.getString("name"));
            resultList.add(resultMap);
        }

        assertThat(resultList)
                .extracting("id")
                .containsAll(idList);
    }

    @Test
    public void testBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(ProxyDataSourceImplTest.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                //.warmupIterations(5)
                //.measurementIterations(5)
                //.measurementTime(TimeValue.minutes(1))
                .forks(0)
                //.threads(Runtime.getRuntime().availableProcessors() * 16)
                .threads(1)
                .syncIterations(true)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .verbosity(VerboseMode.EXTRA)
                .resultFormat(ResultFormatType.JSON)
                .output("./DefaultRoutingDataSourceBenchmark.json")
                .build();

        new Runner(opt).run();

    }

    @Benchmark
    @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 100, time = 1, timeUnit = TimeUnit.SECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    public int testInsertBenchmark() throws Exception {

        long id = System.nanoTime() + random.nextLong();
        String sql = "INSERT INTO area (id, name) VALUES (?, 'New York')";
        log.info("testInsertBenchmark sql [INSERT INTO area (id, name) VALUES ({}, 'New York')]", id);
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(true);
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, id);

        int row = stmt.executeUpdate();
        assertThat(row).isEqualTo(1);
        close(null, stmt, conn);
        return row;
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


    private static DataSource initHikariDataSource(Map<String, String> properties) {
        Map<String, String> props = new HashMap<>(10);
        props.put("driverClassName", "org.h2.Driver");
        props.put("username", "sa");
        props.put("password", "");
        props.put("minIdle", "5");
        props.put("maxPoolSize", "30");
        props.put("connectionTimeout", "30000");
        props.put("isAutoCommit", "false");
        props.put("poolName", "p-hikari");
        Optional.ofNullable(properties).ifPresent(props::putAll);
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration();
        dataSourceConfig.setDataSourceClass(HikariDataSource.class.getCanonicalName());
        dataSourceConfig.setProps(props);
        ReflectNameMatchesDataSourceInitializer dataSourceInitializer = new ReflectNameMatchesDataSourceInitializer();
        DataSource dataSource = dataSourceInitializer.initialize(dataSourceConfig);
        DataSourceTestUtils.executeSqlScript(dataSource, initSqlPath);
        return dataSource;
    }

}