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

/**
 * The LoadBalance interface defines the contract for implementing load balancing strategies.
 * It provides methods to manage a pool of nodes and select a node based on the implemented strategy.
 * This interface can be implemented to support various load balancing algorithms such as round-robin,
 * random selection, or weighted distribution.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public interface LoadBalance {

    /**
     * Adds a node to the pool of available options for load balancing.
     * This method allows the load balancer to include the specified node in its selection process.
     *
     * @param node the node to be added to the load balancing pool
     */
    void addOption(NodeAttribute node);


    /**
     * Removes a node from the pool of available options for load balancing.
     * This method ensures that the specified node is no longer considered for selection.
     *
     * @param node the node to be removed from the load balancing pool
     */
    void removeOption(NodeAttribute node);

    /**
     * Selects a node from the pool of available options using the implemented load balancing strategy.
     * The selection logic depends on the specific algorithm used by the implementation.
     *
     * @return the selected node, or null if no nodes are available in the pool
     */
    NodeAttribute choose();


}
