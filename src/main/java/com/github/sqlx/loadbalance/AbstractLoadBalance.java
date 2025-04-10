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
package com.github.sqlx.loadbalance;


import com.github.sqlx.NodeAttribute;
import com.github.sqlx.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AbstractLoadBalance provides a base implementation for load balancing strategies.
 * It implements the LoadBalance interface and manages a pool of nodes (options) that can be used
 * for load balancing. Concrete subclasses must implement the abstract `choose` method to define
 * their specific load balancing logic.
 *
 * This class provides common functionality such as adding, removing, and filtering nodes,
 * as well as selecting an available node from the pool.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    /**
     * The list of available nodes managed by this load balancer
     */
    private final Set<NodeAttribute> options = new HashSet<>();

    /**
     * Constructs an AbstractLoadBalance instance with an initial list of nodes.
     * If the provided list is not empty, its elements are added to the internal options list.
     *
     * @param options the initial list of nodes to be managed by the load balancer
     */
    protected AbstractLoadBalance(Set<NodeAttribute> options) {
        if (CollectionUtils.isNotEmpty(options)) {
            this.options.addAll(options);
        }
    }

    /**
     * Adds a node to the pool of available options for load balancing.
     * Null values are ignored.
     *
     * @param option the node to be added to the load balancing pool
     */
    @Override
    public void addOption(NodeAttribute option) {
        if (option != null) {
            options.add(option);
        }
    }

    /**
     * Removes a node from the pool of available options for load balancing.
     *
     * @param option the node to be removed from the load balancing pool
     */
    @Override
    public void removeOption(NodeAttribute option) {
        options.remove(option);
    }

    /**
     * Retrieves the current list of available nodes managed by this load balancer.
     *
     * @return the set of nodes in the load balancing pool
     */
    protected Set<NodeAttribute> getOptions() {
        return options;
    }

    /**
     * Selects a node from the pool of available options using the implemented load balancing strategy.
     * This method filters out unavailable nodes and delegates the final selection to the abstract `choose` method.
     *
     * @return the selected node, or null if no available nodes exist in the pool
     */
    @Override
    public NodeAttribute choose() {
        List<NodeAttribute> availableOptions = getOptions().stream()
                .filter(nodeAttr -> nodeAttr.getNodeState().isAvailable())
                .collect(Collectors.toList());

        if (availableOptions.isEmpty()) {
            return null;
        }

        if (availableOptions.size() == 1) {
            return availableOptions.get(0);
        }

        return choose(availableOptions);
    }

    /**
     * Abstract method to be implemented by subclasses.
     * Defines the specific load balancing strategy for selecting a node from the available options.
     *
     * @param availableOptions the list of available nodes to choose from
     * @return the selected node based on the implemented strategy
     */
    protected abstract NodeAttribute choose(List<NodeAttribute> availableOptions);
}
