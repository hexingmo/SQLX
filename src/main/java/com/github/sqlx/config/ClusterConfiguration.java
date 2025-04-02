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
import com.github.sqlx.NodeType;
import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.exception.ManagementException;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
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
@ConfigurationProperties(prefix = "sqlx.config.clusters")
@Getter
@Setter
public class ClusterConfiguration extends LoadBalanceConfiguration implements ConfigurationValidator {

    /**
     * The name of the cluster, used to identify the cluster.
     */
    @Expose
    private String name;

    /**
     * A set of node identifiers, representing all the nodes in the cluster.
     */
    @Expose
    private Set<String> nodes;

    /**
     * A list of attributes for routing nodes, describing the characteristics and roles of each node.
     */
    private Set<NodeAttribute> nodeAttributes;

    @Expose
    private Boolean defaulted = false;


    /**
     * Validates the cluster configuration to ensure it meets the rules.
     * This method checks if there is at least one writable node in the cluster and if there are any independent nodes.
     * If the validation fails, a ConfigurationException is thrown.
     *
     * @throws ConfigurationException If the cluster configuration is invalid.
     */
    @Override
    public void validate() {
        // Check if there is at least one writable node in the cluster
        Optional<NodeAttribute> anyCanWrite = nodeAttributes.stream().filter(n -> n.getNodeType().canWrite()).findAny();
        if (!anyCanWrite.isPresent()) {
            throw new ConfigurationException(String.format("At least one writable node is included in %s cluster" , name));
        }

        // Check if there are any independent nodes, which are not allowed in the cluster
        Optional<NodeAttribute> anyIndependent = nodeAttributes.stream().filter(n -> Objects.equals(NodeType.INDEPENDENT, n.getNodeType())).findAny();
        if (anyIndependent.isPresent()) {
            throw new ConfigurationException("Independent nodes are not allowed in the cluster");
        }

        // Check if all nodes in the cluster have the same database type
        Set<String> databaseTypes = nodeAttributes.stream()
                .map(NodeAttribute::getDatabaseType)
                .collect(Collectors.toSet());

        if (databaseTypes.size() > 1) {
            String differentTypes = String.join(", ", databaseTypes);
            throw new ConfigurationException(String.format("All nodes in the cluster must have the same database type. Found different types: %s", differentTypes));
        }
    }


    /**
     * Returns the list of routing node attributes.
     * This method is used to get the list of node attributes for routing decisions.
     *
     * @return The list of routing node attributes.
     */
    @Override
    protected synchronized Set<NodeAttribute> getRoutingNodeAttribute() {
        return nodeAttributes;
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
        if (!super.equals(o)) return false;
        ClusterConfiguration that = (ClusterConfiguration) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName());
    }
}
