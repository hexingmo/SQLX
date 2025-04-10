package com.github.sqlx.cluster;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.exception.ManagementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ClusterManager}.
 * @author He Xing Mo
 * @since 1.0
 */
class ClusterManagerTest {

    private SqlXConfiguration mockConfig;
    private ClusterManager clusterManager;
    private Cluster mockCluster;

    @BeforeEach
    void setUp() {
        mockConfig = mock(SqlXConfiguration.class);
        mockCluster = mock(Cluster.class);
        clusterManager = new ClusterManager(mockConfig);
    }

    @Test
    void testAddAndGetCluster() {
        clusterManager.addCluster("testCluster", mockCluster);
        Cluster retrievedCluster = clusterManager.getCluster("testCluster");
        assertThat(retrievedCluster).isEqualTo(mockCluster);
    }

    @Test
    void testGetDefaultCluster() {
        when(mockConfig.getDefaultClusterName()).thenReturn("defaultCluster");
        clusterManager.addCluster("defaultCluster", mockCluster);
        Cluster defaultCluster = clusterManager.getDefaultCluster();
        assertThat(defaultCluster).isEqualTo(mockCluster);
    }

    @Test
    void testRemoveNode() {
        when(mockConfig.containsDataSource("node1")).thenReturn(true);
        when(mockCluster.getNodes()).thenReturn(Collections.singleton(mock(NodeAttribute.class)));
        clusterManager.addCluster("testCluster", mockCluster);

        clusterManager.removeNode("node1");

        verify(mockCluster, times(1)).getNodes();
    }

    @Test
    void testRemoveNodeThrowsExceptionForNonExistentNode() {
        when(mockConfig.containsDataSource("node1")).thenReturn(false);

        assertThatThrownBy(() -> clusterManager.removeNode("node1"))
            .isInstanceOf(ManagementException.class)
            .hasMessageContaining("No such datasource: node1");
    }

    @Test
    void testAddNodeInCluster() {
        DataSourceConfiguration mockDataSourceConfig = mock(DataSourceConfiguration.class);
        NodeAttribute mockNodeAttribute = mock(NodeAttribute.class);
        when(mockDataSourceConfig.getNodeAttribute()).thenReturn(mockNodeAttribute);

        when(mockConfig.addNodeInCluster("testCluster", "node1")).thenReturn(true);
        when(mockConfig.getDataSourceConfByName("node1")).thenReturn(mockDataSourceConfig);
        clusterManager.addCluster("testCluster", mockCluster);

        clusterManager.addNodeInCluster("testCluster", "node1");

        verify(mockCluster, times(1)).addNode(mockNodeAttribute);
    }

    @Test
    void testAddNodeInClusterThrowsExceptionForExistingNode() {
        when(mockCluster.containsNode("node1")).thenReturn(true);
        clusterManager.addCluster("testCluster", mockCluster);

        assertThatThrownBy(() -> clusterManager.addNodeInCluster("testCluster", "node1"))
            .isInstanceOf(ManagementException.class)
            .hasMessageContaining("Node:node1 already exists in cluster testCluster");
    }
}