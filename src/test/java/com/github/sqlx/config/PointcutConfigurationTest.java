package com.github.sqlx.config;

import com.github.sqlx.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PointcutConfiguration}.
 * This class tests the configuration and management of pointcuts, including validation and node removal.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class PointcutConfigurationTest {

    private PointcutConfiguration pointcutConfig;

    @BeforeEach
    void setUp() {
        pointcutConfig = new PointcutConfiguration();
        pointcutConfig.setExpression("execution(* com.example..*.*(..))");
        pointcutConfig.setCluster("TestCluster");
        pointcutConfig.setNodes(new ArrayList<>(Arrays.asList("node1", "node2")));
        pointcutConfig.setPropagation(true);
    }

    @Test
    void testValidateSuccess() {
        assertDoesNotThrow(() -> pointcutConfig.validate());
    }

    @Test
    void testValidateMissingExpression() {
        pointcutConfig.setExpression(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> pointcutConfig.validate());
        assertEquals("pointcuts [expression] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateMissingClusterAndNodes() {
        pointcutConfig.setCluster(null);
        pointcutConfig.setNodes(Collections.emptyList());
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> pointcutConfig.validate());
        assertEquals("pointcuts [cluster] or [nodes] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateNullPropagation() {
        pointcutConfig.setPropagation(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> pointcutConfig.validate());
        assertEquals("pointcuts [propagation] attr must not be null", exception.getMessage());
    }

    @Test
    void testRemoveNode() {
        pointcutConfig.removeNode("node1");
        assertFalse(pointcutConfig.getNodes().contains("node1"));
    }

    @Test
    void testEqualsAndHashCode() {
        PointcutConfiguration anotherConfig = new PointcutConfiguration();
        anotherConfig.setExpression("execution(* com.example..*.*(..))");
        anotherConfig.setCluster("TestCluster");
        anotherConfig.setNodes(new ArrayList<>(Arrays.asList("node1", "node2")));
        anotherConfig.setPropagation(true);

        assertEquals(pointcutConfig, anotherConfig);
        assertEquals(pointcutConfig.hashCode(), anotherConfig.hashCode());
    }
}