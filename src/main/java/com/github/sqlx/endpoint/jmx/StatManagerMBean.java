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
package com.github.sqlx.endpoint.jmx;

import com.github.sqlx.NodeState;
import com.github.sqlx.config.ClusterConfiguration;
import com.github.sqlx.config.DataSourceConfiguration;

import javax.management.JMException;
import javax.management.openmbean.TabularData;

/**
 * The javax.management MBean for SQLRouting.
 * This interface defines methods for managing and monitoring SQLRouting statistics through JMX.
 * It allows retrieval of version information, data source lists, cluster status, and performing operations such as removing data sources or nodes in a cluster.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public interface StatManagerMBean {

    /**
     * Returns the version information of SQLRouting.
     *
     * @return The version string of SQLRouting.
     */
    String getVersion();

    /**
     * Gets the load balance type for read operations.
     * This method returns the load balancing strategy type configured for read operations in the system.
     * It allows dynamically retrieving information on how the system distributes load across multiple read sources.
     *
     * @return The load balance type for read operations as a string.
     */
    String getReadLoadBalanceType();

    /**
     * Gets the load balance type for write operations.
     * This method returns the load balancing strategy type configured for write operations in the system.
     * It allows dynamically retrieving information on how the system distributes load across multiple write targets.
     *
     * @return The load balance type for write operations as a string.
     */
    String getWriteLoadBalanceType();

    /**
     * Retrieves the list of configured data sources.
     *
     * @return TabularData containing information about all configured data sources.
     * @throws JMException If an error occurs while retrieving data source information.
     */
    TabularData getDataSourceList() throws JMException;

    /**
     * Checks if the cluster feature is enabled.
     *
     * @return true if the cluster feature is enabled, false otherwise.
     */
    boolean getClusterEnable();

    /**
     * Returns the default cluster name.
     *
     * @return The name of the default cluster.
     */
    String getDefaultCluster();

    /**
     * Retrieves the list of all clusters.
     *
     * @return TabularData containing information about all clusters.
     * @throws JMException If an error occurs while retrieving cluster information.
     */
    TabularData getClusterList() throws JMException;

    /**
     * Removes a data source by name.
     *
     * @param name The name of the data source to remove.
     */
    void removeDatasource(String name);

    /**
     * Removes a specified node from a cluster.
     *
     * @param clusterName The name of the cluster from which to remove the node.
     * @param nodeName The name of the node to remove.
     */
    void removeNodeInCluster(String clusterName, String nodeName);

    /**
     * Adds a new node to the specified cluster.
     * This method will ensure that the node is successfully integrated into the
     * specified cluster and may perform necessary checks to validate the
     * cluster and node names before proceeding with the addition.
     *
     * @param nodeName The name of the node to be added to the cluster.
     * @param clusterName The name of the cluster to which the node will be added.
     */
    void addNodeInCluster(String clusterName, String nodeName);


    /**
     * Sets the state of a node.
     * This method updates the current state of the specified node.
     * @see NodeState
     *
     * @param nodeName  The name of the node, used to identify a specific node.
     * @param nodeState The state of the node, representing the current operational status of the node.
     */
    void setNodeState(String nodeName , String nodeState);

    /**
     * Sets the weight of a node.
     * This method is used to set the weight of a specific node identified by its name.
     * The weight is a numerical value that represents the relative importance or influence of the node.
     * In many algorithms and data structures, the weight of a node can affect its processing order or certain computation results.
     *
     * @param nodeName The unique name of the node, used to identify a specific node.
     * @param weight The weight value of the node, where a higher number indicates greater importance or influence.
     */
    void setNodeWeight(String nodeName , Double weight);

    /**
     * Adds a new data source.
     * @param datasourceConfJson A JSON string containing the configuration settings for the data source.
     *                           This should include all necessary parameters required for the data source
     *                           to be properly initialized and connected.
     * @see DataSourceConfiguration
     */
    void addDataSource(String datasourceConfJson);

    /**
     * Adds a new cluster.
     * @param clusterConfJson A JSON string containing the configuration settings for the cluster.
     *                        This should include all necessary parameters required for the cluster
     *                        to be properly initialized and connected.
     * @see ClusterConfiguration
     */
    void addCluster(String clusterConfJson);
}
