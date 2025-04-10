package com.github.sqlx.loadbalance;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.NodeState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WeightRoundRobinLoadBalance}.
 * This class tests the behavior of WeightRoundRobinLoadBalance when choosing nodes based on weights and node states.
 */
class WeightRoundRobinLoadBalanceTest {

    private Set<NodeAttribute> nodes;
    private WeightRoundRobinLoadBalance loadBalancer;

    @BeforeEach
    void setUp() {
        nodes = new HashSet<>();

        NodeAttribute node1 = mock(NodeAttribute.class);
        when(node1.getName()).thenReturn("node1");
        when(node1.getWeight()).thenReturn(1.0);
        when(node1.getNodeState()).thenReturn(NodeState.UP);

        NodeAttribute node2 = mock(NodeAttribute.class);
        when(node2.getName()).thenReturn("node2");
        when(node2.getWeight()).thenReturn(2.0);
        when(node2.getNodeState()).thenReturn(NodeState.DOWN);

        NodeAttribute node3 = mock(NodeAttribute.class);
        when(node3.getName()).thenReturn("node3");
        when(node3.getWeight()).thenReturn(3.0);
        when(node3.getNodeState()).thenReturn(NodeState.UNKNOWN);

        NodeAttribute node4 = mock(NodeAttribute.class);
        when(node4.getName()).thenReturn("node4");
        when(node4.getWeight()).thenReturn(4.0);
        when(node4.getNodeState()).thenReturn(NodeState.OUT_OF_SERVICE);

        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);

        loadBalancer = new WeightRoundRobinLoadBalance(nodes);
    }

    @Test
    void testChooseNode() {
        NodeAttribute chosenNode = loadBalancer.choose();
        assertThat(chosenNode).isNotNull();
        assertThat(nodes).contains(chosenNode);
    }

    @Test
    void testChooseNodeMultipleTimes() {
        int node1Count = 0;
        int node2Count = 0;
        int node3Count = 0;
        int node4Count = 0;
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            NodeAttribute chosenNode = loadBalancer.choose();
            if ("node1".equals(chosenNode.getName())) {
                node1Count++;
            } else if ("node2".equals(chosenNode.getName())) {
                node2Count++;
            } else if ("node3".equals(chosenNode.getName())) {
                node3Count++;
            } else if ("node4".equals(chosenNode.getName())) {
                node4Count++;
            }
        }

        System.out.println("Node1 chosen: " + node1Count + " times");
        System.out.println("Node2 chosen: " + node2Count + " times");
        System.out.println("Node3 chosen: " + node3Count + " times");
        System.out.println("Node4 chosen: " + node4Count + " times");

        assertThat(node2Count).isEqualTo(0); // Node2 should never be chosen
        assertThat(node4Count).isEqualTo(0); // Node4 should never be chosen
        assertThat(node1Count).isGreaterThan(0);
        assertThat(node3Count).isGreaterThan(0);
        assertThat(node1Count).isLessThan(node3Count);
    }

    @Test
    void testNodeStateAvailability() {
        NodeAttribute node1 = mock(NodeAttribute.class);
        when(node1.getNodeState()).thenReturn(NodeState.UP);
        assertThat(node1.getNodeState().isAvailable()).isTrue();

        NodeAttribute node2 = mock(NodeAttribute.class);
        when(node2.getNodeState()).thenReturn(NodeState.DOWN);
        assertThat(node2.getNodeState().isAvailable()).isFalse();

        NodeAttribute node3 = mock(NodeAttribute.class);
        when(node3.getNodeState()).thenReturn(NodeState.OUT_OF_SERVICE);
        assertThat(node3.getNodeState().isAvailable()).isFalse();

        NodeAttribute node4 = mock(NodeAttribute.class);
        when(node4.getNodeState()).thenReturn(NodeState.UNKNOWN);
        assertThat(node4.getNodeState().isAvailable()).isTrue();
    }
}