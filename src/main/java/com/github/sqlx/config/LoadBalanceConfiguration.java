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
import com.github.sqlx.exception.SqlXRuntimeException;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.loadbalance.ReadLoadBalanceType;
import com.github.sqlx.loadbalance.WeightRandomLoadBalance;
import com.github.sqlx.loadbalance.WeightRoundRobinLoadBalance;
import com.github.sqlx.loadbalance.WriteLoadBalanceType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
@Slf4j
public abstract class LoadBalanceConfiguration {

    private ReadLoadBalanceType readLoadBalanceType = ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_READABLE;

    private WriteLoadBalanceType writeLoadBalanceType = WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_WRITABLE;

    private LoadBalance<NodeAttribute> readLoadBalance;

    private LoadBalance<NodeAttribute> writeLoadBalance;

    public void initLoadBalance() {
        this.readLoadBalance = createReadLoadBalance();
        this.writeLoadBalance = createWriteLoadBalance();
    }

    /**
     * Creates a load balancer for read operations based on the specified read load balance type.
     *
     * @return A {@link LoadBalance} instance configured for read operations.
     */
    private LoadBalance<NodeAttribute> createReadLoadBalance() {

        LoadBalance<NodeAttribute> lb;
        Set<NodeAttribute> attributes = getRoutingNodeAttribute();
        ReadLoadBalanceType type = readLoadBalanceType;
        List<NodeAttribute> allReads = attributes.stream()
                .filter(attribute -> attribute.getNodeType().canRead())
                .collect(Collectors.toList());

        // If no readable nodes are available and the load balance type requires at least one readable node, throw an exception.
        if (allReads.isEmpty() && (type == ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_READABLE || type == ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_READABLE)) {
            throw new ConfigurationException("Readable node is 0,When using ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_ALL or ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_ALL make sure there is at least one readable node present.");
        }

        List<NodeAttribute> onlyReads = filterByRoutingTargetType(attributes, NodeType.READ);
        // If no only-read nodes are available and the load balance type requires at least one only-read node, throw an exception.
        if (onlyReads.isEmpty() && (type == ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_ONLY_READ || type == ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_ONLY_READ)) {
            throw new ConfigurationException("Only read node is 0,When using ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_ONLY_READ or ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_ONLY_READ make sure there is at least one only read node present.");
        }

        List<NodeAttribute> readWrites = filterByRoutingTargetType(attributes, NodeType.READ_WRITE);
        // If no read-write nodes are available and the load balance type requires at least one read-write node, throw an exception.
        if (readWrites.isEmpty() && (type == ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_READ_WRITE || type == ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE)) {
            throw new ConfigurationException("Readable and writable node is 0,When using ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_READ_WRITE or ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE make sure there is at least one only READ_WRITE node present.");
        }

        // ugly code needs fix
        // Create the appropriate load balancer based on the specified load balance type.
        if (type == ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_READABLE) {
            lb = new WeightRandomLoadBalance(allReads);
        } else if (type == ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_ONLY_READ) {
            lb = new WeightRandomLoadBalance(onlyReads);
        } else if (type == ReadLoadBalanceType.WEIGHT_RANDOM_BALANCE_READ_WRITE) {
            lb = new WeightRandomLoadBalance(readWrites);
        } else if (type == ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_READABLE) {
            lb = new WeightRoundRobinLoadBalance(allReads);
        } else if (type == ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_ONLY_READ) {
            lb = new WeightRoundRobinLoadBalance(onlyReads);
        } else if (type == ReadLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE) {
            lb = new WeightRoundRobinLoadBalance(readWrites);
        } else {
            throw new SqlXRuntimeException(String.format("Unsupported load balancing type %s" , type));
        }
        return lb;
    }

    /**
     * Creates a load balancer for write operations based on the specified write load balance type.
     *
     * @return A {@link LoadBalance} instance configured for write operations.
     */
    private LoadBalance<NodeAttribute> createWriteLoadBalance() {

        LoadBalance<NodeAttribute> lb;
        Set<NodeAttribute> attributes = getRoutingNodeAttribute();
        WriteLoadBalanceType type = writeLoadBalanceType;
        List<NodeAttribute> allWrites = attributes.stream()
                .filter(attribute -> attribute.getNodeType().canWrite())
                .collect(Collectors.toList());

        List<NodeAttribute> onlyWrites = filterByRoutingTargetType(attributes, NodeType.WRITE);
        // If no only-write nodes are available and the load balance type requires at least one only-write node, throw an exception.
        if (onlyWrites.isEmpty() && (type == WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_WRITE_ONLY || type == WriteLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_WRITE_ONLY)) {
            throw new ConfigurationException("Only write node is 0,When using WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_WRITE_ONLY or WriteLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_WRITE_ONLY make sure there is at least one only write node present.");
        }

        List<NodeAttribute> readWrites = filterByRoutingTargetType(attributes, NodeType.READ_WRITE);
        // If no read-write nodes are available and the load balance type requires at least one read-write node, throw an exception.
        if (readWrites.isEmpty() && (type == WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_READ_WRITE || type == WriteLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE)) {
            throw new ConfigurationException("Readable and writable node is 0,When using WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_WRITE_ONLY or WriteLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_WRITE_ONLY make sure there is at least one READ_WRITE node present.");
        }

        // ugly code needs fix
        // Create the appropriate load balancer based on the specified load balance type.
        if (type == WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_WRITABLE) {
            lb = new WeightRandomLoadBalance(allWrites);
        } else if (type == WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_WRITE_ONLY) {
            lb = new WeightRandomLoadBalance(onlyWrites);
        } else if (type == WriteLoadBalanceType.WEIGHT_RANDOM_BALANCE_READ_WRITE) {
            lb = new WeightRandomLoadBalance(readWrites);
        } else if (type == WriteLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_WRITABLE) {
            lb = new WeightRoundRobinLoadBalance(allWrites);
        } else if (type == WriteLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_WRITE_ONLY) {
            lb = new WeightRoundRobinLoadBalance(onlyWrites);
        } else if (type == WriteLoadBalanceType.WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE) {
            lb = new WeightRoundRobinLoadBalance(readWrites);
        } else {
            throw new SqlXRuntimeException(String.format("Unsupported load balancing type %s" , type));
        }

        return lb;
    }

    /**
     * Removes the load balance option
     * This method removes the specified routing node attribute option from both read and write load balancing configurations.
     * It ensures that if either the read or write load balancing configuration exists, the specified option is removed.
     * This is crucial for maintaining the accuracy of load balancing policies, ensuring that outdated or unavailable routes are not considered.
     *
     * @param option The routing node attribute option to be removed from the load balancing configurations
     */
    protected void removeLoadBalanceOption(NodeAttribute option) {

        log.info("Attempting to remove load balance option for RoutingNodeAttribute: {}", option);
        // Remove the option from the read load balancing configuration, if it exists
        if (this.readLoadBalance != null) {
            log.info("Removing option from read load balancer: {}", option);
            this.readLoadBalance.removeOption(option);
        }

        // Remove the option from the write load balancing configuration, if it exists
        if (this.writeLoadBalance != null) {
            log.info("Removing option from write load balancer: {}", option);
            this.writeLoadBalance.removeOption(option);
        }
    }

    /**
     * Adds a load balancing option to the appropriate load balancer based on the node type.
     * This method checks if the provided RoutingNodeAttribute option is classified as
     * INDEPENDENT. If it is not independent and the option's node type supports reading,
     * the option is added to the read load balancer. Similarly, if the node type supports
     * writing, the option is added to the write load balancer.
     *
     * @param option The RoutingNodeAttribute option to be added to the load balancers.
     */
    protected void addLoadBalanceOption(NodeAttribute option) {
        log.info("Attempting to add load balance option for RoutingNodeAttribute: {}", option);

        boolean isIndependent = option.getNodeType() == NodeType.INDEPENDENT;
        if (this.readLoadBalance != null && !isIndependent && option.getNodeType().canRead()) {
            log.info("Adding option to read load balancer: {}", option);
            this.readLoadBalance.addOption(option);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Option not added to read load balancer. Independent: {}, Can Read: {}", isIndependent, option.getNodeType().canRead());
            }
        }
        if (this.writeLoadBalance != null && !isIndependent && option.getNodeType().canWrite()) {
            log.info("Adding option to write load balancer: {}", option);
            this.writeLoadBalance.addOption(option);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Option not added to write load balancer. Independent: {}, Can Write: {}", isIndependent, option.getNodeType().canWrite());
            }
        }
    }

    /**
     * Filters the list of routing node attributes by the specified target node type.
     *
     * @param attributes The list of routing node attributes to filter.
     * @param target The target node type to filter by.
     * @return A filtered list of routing node attributes that match the target node type.
     */
    private List<NodeAttribute> filterByRoutingTargetType(Collection<NodeAttribute> attributes , NodeType target) {
        return attributes.stream().filter(attribute -> Objects.equals(attribute.getNodeType() , target))
                .collect(Collectors.toList());
    }

    /**
     * Abstract method to retrieve the list of routing node attributes.
     *
     * @return A list of routing node attributes.
     */
    protected abstract Set<NodeAttribute> getRoutingNodeAttribute();
}
