package com.github.sqlx.config;

import com.github.sqlx.metrics.MetricsCollectMode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Author: He Xing Mo
 * Version: 1.0
 */
public class SqlXConfigurationTestUtil {

    private static final String DATA_SOURCE_CLASS = "org.h2.jdbcx.JdbcDataSource";

    private static final String DRIVER_CLASS_NAME = "org.h2.Driver";


    /**
     * 创建一个只有一个数据源配置的 SqlXConfiguration 实例。
     *
     * @return SqlXConfiguration 实例
     */
    public static SqlXConfiguration createSingleDataSourceConfig() {
        SqlXConfiguration config = new SqlXConfiguration();

        DataSourceConfiguration dsConfig = new DataSourceConfiguration();
        dsConfig.setName("DataSource1");
        dsConfig.setDataSourceClass(DATA_SOURCE_CLASS);
        dsConfig.setDefaulted(true);
        HashMap<String, String> props = new HashMap<>();
        props.put("url", "jdbc:h2:mem:~/testdb_1;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;");
        props.put("driverClassName", DRIVER_CLASS_NAME);
        props.put("username", "sa");
        props.put("password", "password");
        dsConfig.setProps(props);

        config.setDataSources(new ArrayList<>(Collections.singletonList(dsConfig)));
        config.setMetrics(createDefaultMetricsConfig());

        config.init();
        return config;
    }

    /**
     * 创建一个包含多个数据源配置的 SqlXConfiguration 实例。
     *
     * @return SqlXConfiguration 实例
     */
    public static SqlXConfiguration createMultipleDataSourcesConfig() {
        SqlXConfiguration config = new SqlXConfiguration();

        DataSourceConfiguration dsConfig1 = new DataSourceConfiguration();
        dsConfig1.setName("DataSource1");
        dsConfig1.setDefaulted(true);
        dsConfig1.setDataSourceClass(DATA_SOURCE_CLASS);
        HashMap<String, String> props1 = new HashMap<>();
        props1.put("url", "jdbc:h2:mem:~/testdb_1;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;");
        props1.put("driverClassName", DRIVER_CLASS_NAME);
        props1.put("username", "sa");
        props1.put("password", "password");
        dsConfig1.setProps(props1);

        DataSourceConfiguration dsConfig2 = new DataSourceConfiguration();
        dsConfig2.setName("DataSource2");
        dsConfig2.setDataSourceClass(DATA_SOURCE_CLASS);
        HashMap<String, String> props2 = new HashMap<>();
        props2.put("url", "jdbc:h2:mem:~/testdb_2;FILE_LOCK=SOCKET;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE;IGNORECASE=TRUE;");
        props2.put("driverClassName", DRIVER_CLASS_NAME);
        props2.put("username", "sa");
        props2.put("password", "password");
        dsConfig2.setProps(props2);

        config.setDataSources(new ArrayList<>(Arrays.asList(dsConfig1, dsConfig2)));
        config.setMetrics(createDefaultMetricsConfig());

        config.init();
        return config;
    }

    /**
     * 创建一个包含多个数据源和多个集群配置的 SqlXConfiguration 实例。
     * 每个集群中的数据源配置不相同。
     *
     * @return SqlXConfiguration 实例
     */
    public static SqlXConfiguration createMultipleDataSourcesAndClustersConfig() {
        SqlXConfiguration config = createMultipleDataSourcesConfig();

        ClusterConfiguration clusterConfig1 = new ClusterConfiguration();
        clusterConfig1.setName("Cluster1");
        clusterConfig1.setWritableNodes(new HashSet<>(Collections.singletonList("DataSource1")));
        clusterConfig1.setReadableNodes(new HashSet<>(Collections.singletonList("DataSource2")));
        clusterConfig1.setDefaulted(true);

        ClusterConfiguration clusterConfig2 = new ClusterConfiguration();
        clusterConfig2.setName("Cluster2");
        clusterConfig2.setWritableNodes(new HashSet<>(Collections.singletonList("DataSource2")));
        clusterConfig2.setReadableNodes(new HashSet<>(Collections.singletonList("DataSource1")));
        clusterConfig2.setDefaulted(false);

        config.setClusters(Arrays.asList(clusterConfig1, clusterConfig2));

        PointcutConfiguration pointcutConfig = new PointcutConfiguration();
        pointcutConfig.setExpression("execution(* com.example..*.*(..))");
        pointcutConfig.setCluster("Cluster1");
        config.setPointcuts(Collections.singletonList(pointcutConfig));

        config.init();
        return config;
    }

    /**
     * 创建默认的 MetricsConfiguration 实例。
     *
     * @return MetricsConfiguration 实例
     */
    private static MetricsConfiguration createDefaultMetricsConfig() {
        MetricsConfiguration metricsConfig = new MetricsConfiguration();
        metricsConfig.setEnabled(true);
        metricsConfig.setUsername("admin");
        metricsConfig.setPassword("password");
        metricsConfig.setSlowSqlMillis(1000L);
        metricsConfig.setSlowTransactionMillis(2000L);
        metricsConfig.setFileDirectory("/var/logs/sqlx");
        metricsConfig.setCollectScope(MetricsCollectScope.SLOW);
        metricsConfig.setCollectMode(MetricsCollectMode.SYNC);
        metricsConfig.setDataRetentionDuration(Duration.ofDays(3));
        metricsConfig.setCollectKeepAliveMillis(3000L);
        metricsConfig.setCollectQueueCapacity(4000);
        metricsConfig.setCollectCorePoolSize(10);
        metricsConfig.setCollectMaxPoolSize(20);
        return metricsConfig;
    }
} 