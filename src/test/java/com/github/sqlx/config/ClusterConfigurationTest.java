package com.github.sqlx.config;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.NodeState;
import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.exception.ManagementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ClusterConfiguration}.
 * This class tests the configuration and management of clusters, including node addition, removal, and validation.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class ClusterConfigurationTest {

    private ClusterConfiguration clusterConfig;

    @BeforeEach
    void setUp() {
        clusterConfig = new ClusterConfiguration();
        clusterConfig.setName("TestCluster");

        Set<NodeAttribute> nodeAttributes = new HashSet<>();
        nodeAttributes.add(createNodeAttribute("node1", "H2"));
        nodeAttributes.add(createNodeAttribute("node2", "H2"));
        clusterConfig.setNodeAttributes(nodeAttributes);

        Set<String> writableNodes = new HashSet<>();
        writableNodes.add("node1");
        clusterConfig.setWritableNodes(writableNodes);

        Set<String> readableNodes = new HashSet<>();
        readableNodes.add("node2");
        clusterConfig.setReadableNodes(readableNodes);
    }

    @Test
    void testValidateSuccess() {
        assertDoesNotThrow(() -> clusterConfig.validate());
    }

    @Test
    void testValidateDifferentDatabaseTypes() {
        clusterConfig.getNodeAttributes().add(createNodeAttribute("node3", "MySQL"));
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> clusterConfig.validate());
        assertTrue(exception.getMessage().contains("All nodes in the cluster must have the same database type"));
    }

    @Test
    void testValidateMissingWritableNodes() {
        clusterConfig.getWritableNodes().clear();
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> clusterConfig.validate());
        assertTrue(exception.getMessage().contains("At least one writable node is required"));
    }

    @Test
    void testValidateMissingReadableNodes() {
        clusterConfig.getReadableNodes().clear();
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> clusterConfig.validate());
        assertTrue(exception.getMessage().contains("At least one readable node is required"));
    }

    @Test
    void testRemoveNode() {
        NodeAttribute removedNode = clusterConfig.removeNode("node1");
        assertNotNull(removedNode);
        assertEquals("node1", removedNode.getName());
        assertFalse(clusterConfig.getNodes().contains("node1"));
    }

    @Test
    void testRemoveNodeValidationFailure() {
        clusterConfig.getWritableNodes().clear();
        ManagementException exception = assertThrows(ManagementException.class, () -> clusterConfig.removeNode("node1"));
        assertNotNull(exception);
    }

    @Test
    void testAddNode() {
        NodeAttribute newNode = createNodeAttribute("node3", "H2");
        clusterConfig.addNode(newNode);
        assertTrue(clusterConfig.getNodes().contains("node3"));
    }

    @Test
    void testGetWritableRoutingNodeAttributes() {
        Set<NodeAttribute> writableAttributes = clusterConfig.getWritableRoutingNodeAttributes();
        assertEquals(1, writableAttributes.size());
        assertTrue(writableAttributes.stream().anyMatch(attr -> attr.getName().equals("node1")));
    }

    @Test
    void testGetReadableRoutingNodeAttributes() {
        Set<NodeAttribute> readableAttributes = clusterConfig.getReadableRoutingNodeAttributes();
        assertEquals(1, readableAttributes.size());
        assertTrue(readableAttributes.stream().anyMatch(attr -> attr.getName().equals("node2")));
    }

    @Test
    void testEqualsAndHashCode() {
        ClusterConfiguration anotherConfig = new ClusterConfiguration();
        anotherConfig.setName("TestCluster");
        assertEquals(clusterConfig, anotherConfig);
        assertEquals(clusterConfig.hashCode(), anotherConfig.hashCode());
    }

    private NodeAttribute createNodeAttribute(String name, String databaseType) {
        return new NodeAttribute() {
            @Override
            public String getUrl() {
                return "jdbc:h2:mem:" + name;
            }

            @Override
            public String getDatabase() {
                return "testdb";
            }

            @Override
            public String getDatabaseType() {
                return databaseType;
            }

            @Override
            public NodeState getNodeState() {
                return NodeState.UNKNOWN;
            }

            @Override
            public void setNodeState(NodeState nodeState) {
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Double getWeight() {
                return 1.0;
            }

            @Override
            public void setNodeWeight(Double weight) {
            }

            @Override
            public String getHeartbeatSql() {
                return "SELECT 1";
            }

            @Override
            public long getHeartbeatInterval() {
                return 10000;
            }

            @Override
            public String getDestroyMethod() {
                return "close";
            }
        };
    }

}