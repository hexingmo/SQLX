package com.github.sqlx.config;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.exception.SqlXRuntimeException;
import com.github.sqlx.metrics.MetricsCollectMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SqlXConfiguration}.
 * This class tests the configuration and management of SQLX, including data source and cluster management.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class SqlXConfigurationTest {

    private SqlXConfiguration sqlXConfig;

    @BeforeEach
    void setUp() {
        sqlXConfig = new SqlXConfiguration();

        DataSourceConfiguration dsConfig1 = new DataSourceConfiguration();
        dsConfig1.setName("DataSource1");
        dsConfig1.setDataSourceClass("org.h2.jdbcx.JdbcDataSource");
        dsConfig1.setWeight(100.0);
        HashMap<String, String> props1 = new HashMap<>();
        props1.put("url", "jdbc:h2:mem:testdb1");
        props1.put("driverClassName", "org.h2.Driver");
        props1.put("username", "sa");
        props1.put("password", "password");
        dsConfig1.setProps(props1);

        DataSourceConfiguration dsConfig2 = new DataSourceConfiguration();
        dsConfig2.setName("DataSource2");
        dsConfig2.setDataSourceClass("org.h2.jdbcx.JdbcDataSource");
        dsConfig1.setWeight(66.0);
        HashMap<String, String> props2 = new HashMap<>();
        props2.put("url", "jdbc:h2:mem:testdb2");
        props2.put("driverClassName", "org.h2.Driver");
        props2.put("username", "sa");
        props2.put("password", "password");
        dsConfig2.setProps(props2);

        sqlXConfig.setDataSources(new ArrayList<>(Arrays.asList(dsConfig1, dsConfig2)));

        ClusterConfiguration clusterConfig1 = new ClusterConfiguration();
        clusterConfig1.setName("Cluster1");
        clusterConfig1.setWritableNodes(new HashSet<>(Collections.singletonList("DataSource1")));
        clusterConfig1.setReadableNodes(new HashSet<>(Collections.singletonList("DataSource2")));
        clusterConfig1.setDefaulted(true);

        ClusterConfiguration clusterConfig2 = new ClusterConfiguration();
        clusterConfig2.setName("Cluster2");
        clusterConfig2.setWritableNodes(new HashSet<>(Collections.singletonList("DataSource1")));
        clusterConfig2.setReadableNodes(new HashSet<>(Collections.singletonList("DataSource2")));
        clusterConfig2.setDefaulted(false);
        sqlXConfig.setClusters(new ArrayList<>(Arrays.asList(clusterConfig1, clusterConfig2)));

        PointcutConfiguration pointcutConfig = new PointcutConfiguration();
        pointcutConfig.setExpression("execution(* com.example..*.*(..))");
        pointcutConfig.setCluster("Cluster1");
        sqlXConfig.setPointcuts(new ArrayList<>(Collections.singletonList(pointcutConfig)));

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
        sqlXConfig.setMetrics(metricsConfig);

        sqlXConfig.init();
    }

    @Test
    void testValidateSuccess() {
        assertDoesNotThrow(() -> sqlXConfig.validate());
    }

    @Test
    void testValidateMissingDataSources() {
        sqlXConfig.setDataSources(Collections.emptyList());
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> sqlXConfig.validate());
        assertEquals("dataSources Configuration must not be empty", exception.getMessage());
    }

    @Test
    void testGetDataSourceNames() {
        List<String> dataSourceNames = sqlXConfig.getDataSourceNames();
        assertEquals(2, dataSourceNames.size());
        assertTrue(dataSourceNames.contains("DataSource1"));
        assertTrue(dataSourceNames.contains("DataSource2"));
    }

    @Test
    void testGetDataSourceConfiguration() {
        DataSourceConfiguration dsConfig = sqlXConfig.getDataSourceConfiguration("DataSource1");
        assertNotNull(dsConfig);
        assertEquals("DataSource1", dsConfig.getName());
    }

    @Test
    void testGetNodeAttribute() {
        NodeAttribute nodeAttribute = sqlXConfig.getNodeAttribute("DataSource1");
        assertNotNull(nodeAttribute);
        assertEquals("DataSource1", nodeAttribute.getName());
    }

    @Test
    void testRemoveDataSourceConfiguration() {
        boolean removed = sqlXConfig.removeDataSourceConfiguration("DataSource1");
        assertTrue(removed);
        assertNull(sqlXConfig.getDataSourceConfiguration("DataSource1"));
    }

    @Test
    void testRemoveNodeInCluster() {
        boolean removed = sqlXConfig.removeNodeInCluster("Cluster1", "DataSource1");
        assertTrue(removed);
        ClusterConfiguration clusterConfig = sqlXConfig.getCluster("Cluster1");
        assertFalse(clusterConfig.getNodes().contains("DataSource1"));
    }

    @Test
    void testAddNodeInCluster() {
        sqlXConfig.addNodeInCluster("Cluster1", "DataSource1");
        ClusterConfiguration clusterConfig = sqlXConfig.getCluster("Cluster1");
        assertTrue(clusterConfig.getNodes().contains("DataSource1"));
    }

    @Test
    void testContainsDataSource() {
        assertTrue(sqlXConfig.containsDataSource("DataSource1"));
        assertFalse(sqlXConfig.containsDataSource("NonExistentDataSource"));
    }

    @Test
    void testContainsCluster() {
        assertTrue(sqlXConfig.containsCluster("Cluster1"));
        assertFalse(sqlXConfig.containsCluster("NonExistentCluster"));
    }

    @Test
    void testGetCluster() {
        ClusterConfiguration clusterConfig = sqlXConfig.getCluster("Cluster1");
        assertNotNull(clusterConfig);
        assertEquals("Cluster1", clusterConfig.getName());
    }

    @Test
    void testAddDataSourceConfiguration() {
        DataSourceConfiguration newDsConfig = new DataSourceConfiguration();
        newDsConfig.setName("DataSource3");
        newDsConfig.setDataSourceClass("org.h2.jdbcx.JdbcDataSource");
        HashMap<String, String> props3 = new HashMap<>();
        props3.put("url", "jdbc:h2:mem:testdb3");
        props3.put("driverClassName", "org.h2.Driver");
        props3.put("username", "sa");
        props3.put("password", "password");
        newDsConfig.setProps(props3);

        sqlXConfig.addDataSourceConfiguration(newDsConfig);
        assertNotNull(sqlXConfig.getDataSourceConfiguration("DataSource3"));
    }

    @Test
    void testAddClusterConfiguration() {
        ClusterConfiguration newClusterConfig = new ClusterConfiguration();
        newClusterConfig.setName("Cluster3");
        newClusterConfig.setWritableNodes(new HashSet<>(Collections.singletonList("DataSource1")));
        newClusterConfig.setReadableNodes(new HashSet<>(Collections.singletonList("DataSource1")));

        sqlXConfig.addClusterConfiguration(newClusterConfig);
        assertNotNull(sqlXConfig.getCluster("Cluster3"));
    }

    @Test
    void testRemoveClusterConfiguration() {
        sqlXConfig.removeClusterConfiguration("Cluster1");
        assertNull(sqlXConfig.getCluster("Cluster1"));
    }

    @Test
    void testGetDefaultClusterName() {
        String defaultClusterName = sqlXConfig.getDefaultClusterName();
        assertEquals("Cluster1", defaultClusterName);
    }

    @Test
    void testGetDefaultClusterNameNoDefault() {
        sqlXConfig.getClusters().forEach(cluster -> cluster.setDefaulted(false));
        SqlXRuntimeException exception = assertThrows(SqlXRuntimeException.class, () -> sqlXConfig.getDefaultClusterName());
        assertEquals("No default cluster found", exception.getMessage());
    }
}