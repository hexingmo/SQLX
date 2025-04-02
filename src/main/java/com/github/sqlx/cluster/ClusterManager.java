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
package com.github.sqlx.cluster;


import com.github.sqlx.NodeAttribute;
import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.exception.ManagementException;
import com.github.sqlx.util.CollectionUtils;
import com.github.sqlx.util.MapUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Cluster manager class for managing multiple cluster configurations and access.
 * It uses a default cluster and a map of clusters to manage and provide quick access to clusters.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class ClusterManager {

    /**
     * A thread-safe map to store all clusters.
     */
    private final Map<String , Cluster> clusters = new ConcurrentHashMap<>();

    /**
     * The name of the default cluster.
     */
    private final String defaultCluster;

    private final SqlXConfiguration sqlXConfiguration;

    public ClusterManager(SqlXConfiguration sqlXConfiguration) {
        this.sqlXConfiguration = sqlXConfiguration;
        this.defaultCluster = sqlXConfiguration.getDefaultCluster();
    }

    /**
     * Get the default cluster.
     *
     * @return the default cluster
     */
    public Cluster getDefaultCluster() {
        return clusters.get(defaultCluster);
    }


    /**
     * Add a single cluster to the manager.
     *
     * @param name   the name of the cluster
     * @param cluster the cluster instance
     */
    public void addCluster(String name , Cluster cluster) {
        clusters.put(name , cluster);
    }

    /**
     * Add multiple clusters to the manager.
     *
     * @param clusters a map of cluster names to cluster instances
     */
    public void addClusters(Map<String , Cluster> clusters) {
        if (MapUtils.isNotEmpty(clusters)) {
            this.clusters.putAll(clusters);
        }
    }

    /**
     * Retrieves a cluster object by its name.
     *
     * This method looks up the cluster in the `clusters` map using the provided name.
     * It assumes that `clusters` is an existing map where keys are strings and values are cluster objects.
     *
     * @param name The unique identifier of the cluster.
     * @return The cluster object corresponding to the given name, or null if not found.
     */
    public synchronized Cluster getCluster(String name) {
        return clusters.get(name);
    }

    /**
     * Removes a node with the specified name from all clusters.
     * This method iterates over all clusters and attempts to remove the node with the specified name from each cluster.
     * If the node is removed and the cluster becomes empty, the cluster itself is also removed.
     *
     * @param nodeName The name of the node to be removed.
     */
    public synchronized void removeNode(String nodeName) {

        log.info("Starting the removal process for node: {}", nodeName);
        if (!sqlXConfiguration.containsDataSource(nodeName)) {
            throw new ManagementException("No such datasource: " + nodeName);
        }
        for (Map.Entry<String, Cluster> entry : clusters.entrySet()) {
            String clusterName = entry.getKey();
            Cluster cluster = entry.getValue();
            List<String> nodeNames = cluster.getNodes().stream().map(NodeAttribute::getName).collect(Collectors.toList());
            if (CollectionUtils.containsAny(nodeNames , nodeName)) {
                removeNodeFromCluster(cluster , nodeName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Node: {} is not part of cluster: {}", nodeName, clusterName);
                }
            }
        }
    }

    /**
     * Removes a node with the specified name from a specific cluster.
     * This method first finds the cluster with the given name and then removes the node with the specified name from that cluster.
     * If the node is removed and the cluster becomes empty, the cluster itself is also removed.
     *
     * @param clusterName The name of the cluster from which the node should be removed.
     * @param nodeName The name of the node to be removed.
     */
    public synchronized void removeNodeInCluster(String clusterName, String nodeName) {

        if (!clusters.containsKey(clusterName)) {
            throw new ManagementException("No such cluster: " + clusterName);
        }
        Cluster cluster = clusters.get(clusterName);
        if (!cluster.containsNode(nodeName)) {
            throw new ManagementException(String.format("No such node:%s in cluster %s" , nodeName , clusterName));
        }
        removeNodeFromCluster(cluster , nodeName);
    }

    public synchronized void addNodeInCluster(String clusterName, String nodeName) {
        if (!clusters.containsKey(clusterName)) {
            throw new ManagementException("No such cluster: " + clusterName);
        }
        Cluster cluster = clusters.get(clusterName);
        if (cluster.containsNode(nodeName)) {
            throw new ManagementException(String.format("Node:%s already exists in cluster %s" , nodeName , clusterName));
        }
        boolean added = sqlXConfiguration.addNodeInCluster(clusterName, nodeName);
        if (added) {
            DataSourceConfiguration dsConf = sqlXConfiguration.getDataSourceConfByName(nodeName);
            cluster.addNode(dsConf.getNodeAttribute());
        }
    }

    /**
     * Removes a node with the specified name from a specific cluster.
     * This method performs the actual removal of the node and checks if the cluster becomes empty after the removal.
     * If the cluster becomes empty, it is also removed from the list of clusters.
     *
     * @param cluster The cluster from which the node should be removed.
     * @param nodeName The name of the node to be removed.
     */
    private void removeNodeFromCluster(Cluster cluster , String nodeName) {
        log.info("Attempting to remove node: {} from cluster: {}", nodeName, cluster.getName());
        boolean removed = sqlXConfiguration.removeNodeInCluster(cluster.getName(), nodeName);
        if (removed) {
            log.info("Node: {} successfully removed from routing configuration for cluster: {}", nodeName, cluster.getName());
            removed = cluster.getNodes().removeIf(node -> node.getName().equals(nodeName));
            if (removed) {
                log.info("Node: {} successfully removed from cluster: {}", nodeName, cluster.getName());
                if (CollectionUtils.isEmpty(cluster.getNodes())) {
                    log.info("Cluster: {} is now empty. Removing cluster from the list of clusters.", cluster.getName());
                    clusters.remove(cluster.getName());
                    log.info("Cluster: {} has been removed from the list of clusters.", cluster.getName());
                }
            }
        }
    }


}
