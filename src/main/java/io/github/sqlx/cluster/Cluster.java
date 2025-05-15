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
package io.github.sqlx.cluster;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.rule.RouteRule;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Represents a cluster in a database routing environment.
 * A cluster consists of multiple routing nodes and follows specific routing rules.
 * The main responsibilities of a cluster include managing a set of routing nodes and applying routing rules to determine which node data should be routed to.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Getter
@Setter
public class Cluster {

    /**
     * The name of the cluster, used to uniquely identify a cluster.
     */
    private String name;

    /**
     * A list of routing node attributes, representing all the nodes in the cluster.
     * Each routing node has specific attributes.
     */
    private Set<NodeAttribute> nodes;

    /**
     * The routing rule of the cluster, used to determine how to route data to a specific node.
     * The routing rule is a core component of the cluster, guiding the routing logic.
     */
    private RouteRule rule;

    /**
     * Checks if the given node name exists in the current node collection.
     *
     * @param nodeName The name of the node to check.
     * @return true if a node with the given name exists in the collection; otherwise false.
     */
    public synchronized boolean containsNode(String nodeName) {
        return nodes.stream().anyMatch(node -> node.getName().equals(nodeName));
    }

    public synchronized void addNode(NodeAttribute nodeAttribute) {
        nodes.add(nodeAttribute);
    }
}
