/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.sqlx.config;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.exception.ManagementException;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the configuration of a cluster.
 * A cluster consists of multiple nodes, and this class extends LoadBalanceConfiguration and implements ConfigurationValidator
 * to provide load balancing configuration and validation capabilities.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class ClusterConfiguration implements ConfigurationValidator {

    /**
     * The name of the cluster, used to identify the cluster.
     */
    @Getter
    @Setter
    private String name;

    /**
     * A set of node identifiers, representing all the nodes in the cluster.
     */
    @Getter
    private final Set<String> nodes = new HashSet<>();

    /**
     * A list of attributes for routing nodes, describing the characteristics and roles of each node.
     */
    @Getter
    @Setter
    private Set<NodeAttribute> nodeAttributes;

    @Getter
    @Setter
    private Boolean defaulted = false;


    @Getter
    private Set<String> writableNodes = new HashSet<>();

    @Getter
    private Set<String> readableNodes = new HashSet<>();

    @Getter
    @Setter
    private Class<?> writeLoadBalanceClass;

    @Getter
    @Setter
    private Class<?> readLoadBalanceClass;

    public void setWritableNodes(Set<String> writableNodes) {
        if (CollectionUtils.isNotEmpty(writableNodes)) {
            this.writableNodes.addAll(writableNodes);
            this.nodes.addAll(writableNodes);
        }
    }

    public void setReadableNodes(Set<String> readableNodes) {
        if (CollectionUtils.isNotEmpty(readableNodes)) {
            this.readableNodes.addAll(readableNodes);
            this.nodes.addAll(readableNodes);
        }
    }

    public Set<NodeAttribute> getWritableRoutingNodeAttributes() {
        return nodeAttributes.stream()
                .filter(nodeAttribute -> this.writableNodes.contains(nodeAttribute.getName()))
                .collect(Collectors.toSet());
    }

    public Set<NodeAttribute> getReadableRoutingNodeAttributes() {
        return nodeAttributes.stream()
                .filter(nodeAttribute -> this.readableNodes.contains(nodeAttribute.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * Validates the cluster configuration to ensure it meets the rules.
     * This method checks if there is at least one writable node in the cluster and if there are any independent nodes.
     * If the validation fails, a ConfigurationException is thrown.
     *
     * @throws ConfigurationException If the cluster configuration is invalid.
     */
    @Override
    public void validate() {
        // Check if all nodes in the cluster have the same database type
        Set<String> databaseTypes = nodeAttributes.stream()
                .map(NodeAttribute::getDatabaseType)
                .collect(Collectors.toSet());

        if (databaseTypes.size() > 1) {
            String differentTypes = String.join(", ", databaseTypes);
            throw new ConfigurationException(String.format("All nodes in the cluster must have the same database type. Found different types: %s", differentTypes));
        }

        if (CollectionUtils.isEmpty(this.writableNodes)) {
            throw new ConfigurationException(String.format("At least one writable node is required in the [%s] cluster." , name));
        }

        if (CollectionUtils.isEmpty(this.readableNodes)) {
            throw new ConfigurationException(String.format("At least one readable node is required in the [%s] cluster." , name));
        }
    }


    /**
     * Removes the routing node attribute with the specified name.
     * This method removes a RoutingNodeAttribute object from the node attributes list that matches the given node name.
     * After removing the node, it attempts to validate the current routing table. If validation fails, the node is re-added and an exception is thrown.
     *
     * @param nodeName The name of the node to be removed.
     * @return The RoutingNodeAttribute object of the removed node if found and successfully removed; otherwise, null.
     * @throws ManagementException If an error occurs during the validation of the routing table, this exception is thrown.
     */
    public synchronized NodeAttribute removeNode(String nodeName) {
        NodeAttribute removed = null;
        Iterator<NodeAttribute> iterator = nodeAttributes.iterator();
        while (iterator.hasNext()) {
            NodeAttribute node = iterator.next();
            if (node.getName().equals(nodeName)) {
                iterator.remove();
                this.nodes.remove(nodeName);
                try {
                    validate();
                } catch (Exception e) {
                    nodeAttributes.add(node);
                    this.nodes.add(nodeName);
                    throw new ManagementException(e);
                }
                removed = node;
            }
        }
        return removed;
    }

    public synchronized void removeNode(NodeAttribute node) {
        nodeAttributes.remove(node);
        nodes.remove(node.getName());
    }

    public synchronized void addNode(NodeAttribute node) {
        nodeAttributes.add(node);
        nodes.add(node.getName());
        validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClusterConfiguration)) return false;
        ClusterConfiguration that = (ClusterConfiguration) o;
        return Objects.equals(this.name, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
