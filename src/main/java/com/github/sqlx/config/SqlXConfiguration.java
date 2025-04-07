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
import com.github.sqlx.exception.SqlXRuntimeException;
import com.github.sqlx.sql.parser.*;
import com.github.sqlx.util.CollectionUtils;
import com.github.sqlx.util.StringUtils;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Getter
@Setter
@Slf4j
@ConfigurationProperties(prefix = "sqlx.config")
public class SqlXConfiguration implements ConfigurationValidator {

    @Expose
    private boolean enabled = true;

    @Expose
    private String sqlParserClass;

    @Expose
    private SqlParsingFailBehavior sqlParsingFailBehavior = SqlParsingFailBehavior.WARNING;

    private SqlParser sqlParserInstance;

    @Expose
    private List<DataSourceConfiguration> dataSources = new ArrayList<>();

    @Expose
    private List<ClusterConfiguration> clusters = new ArrayList<>();

    @Expose
    private List<PointcutConfiguration> pointcuts = new ArrayList<>();

    @Expose
    private MetricsConfiguration metrics;

    public SqlParser getSqlParserInstance() {
        if (Objects.isNull(this.sqlParserInstance)) {
            SqlParser sqlParser;
            if (StringUtils.isNotBlank(this.sqlParserClass)) {
                sqlParser = Reflect.onClass(this.sqlParserClass).create().get();
            } else {
                log.warn("sqlParserClass is empty , default use {}" , JSqlParser.class.getName());
                sqlParser = new JSqlParser();
            }
            this.sqlParserInstance = new AnnotationSqlParser(new FailBehaviorSqlParser(sqlParser, this.sqlParsingFailBehavior) , new DefaultAnnotationSqlHintParser());
        }
        return this.sqlParserInstance;
    }

    public List<String> getDataSourceNames() {
        return dataSources.stream().map(DataSourceConfiguration::getName).collect(Collectors.toList());
    }

    public DataSourceConfiguration getDataSourceConfByName(final String name) {
        Optional<DataSourceConfiguration> optional = dataSources.stream().filter(dsConf -> Objects.equals(dsConf.getName(), name)).findFirst();
        return optional.orElse(null);
    }

    public NodeAttribute getNodeAttribute(final String name) {
        DataSourceConfiguration dsConf = getDataSourceConfByName(name);
        return dsConf != null ? dsConf.getNodeAttribute() : null;
    }

    public Set<NodeAttribute> getNodeAttributes(final Collection<String> names) {
        return dataSources.stream()
                .map(DataSourceConfiguration::getNodeAttribute)
                .filter(routingNodeAttribute -> names.contains(routingNodeAttribute.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public void validate() {
        if (CollectionUtils.isEmpty(dataSources)) {
            throw new ConfigurationException("dataSources Configuration must not be empty");
        }

        // validate DataSourceConfiguration
        for (DataSourceConfiguration dsConf : dataSources) {
            dsConf.validate();
        }

        List<String> dataSourceNames = dataSources.stream().map(DataSourceConfiguration::getName).collect(Collectors.toList());
        Collection<String> dataSourceNameSubtract = CollectionUtils.subtract(dataSourceNames, new HashSet<>(dataSourceNames));
        if (CollectionUtils.isNotEmpty(dataSourceNameSubtract)) {
            throw new ConfigurationException(String.format("dataSources name duplicate %s" , dataSourceNameSubtract));
        }

        validateCluster();
        validatePointcuts();

        // validate MetricsConfiguration
        metrics.validate();
    }

    /**
     * Validates the routing attributes of a pointcut configuration.
     * This method checks if the specified cluster and nodes exist and are correctly configured.
     *
     * @param pointExpressions the pointcut expression used for logging and error messages
     * @param cluster the name of the cluster to validate
     * @param nodes a list of node names to validate
     * @param propagation a boolean indicating whether propagation is enabled
     * @throws ConfigurationException if the cluster or nodes are not correctly configured
     */
    public void validatePointcutRoutingAttr(String pointExpressions, final String cluster, List<String> nodes, boolean propagation) {
        List<String> dataSourceNames = dataSources.stream().map(DataSourceConfiguration::getName).collect(Collectors.toList());
        if (StringUtils.isNotBlank(cluster)) {
            Optional<ClusterConfiguration> optional = clusters.stream().filter(c -> c.getName().equals(cluster)).findFirst();
            if (!optional.isPresent()) {
                throw new ConfigurationException(String.format("%s pointcut [cluster] attr %s Cluster does not exist" , pointExpressions, cluster));
            }
        }
        if (StringUtils.isBlank(cluster) && CollectionUtils.isNotEmpty(nodes)) {
            for (String node : nodes) {
                if (!dataSourceNames.contains(node)) {
                    throw new ConfigurationException(String.format("%s pointcut [nodes] attr %s Datasource does not exist" , pointExpressions, node));
                }
            }
        } else if (CollectionUtils.isNotEmpty(nodes)) {
            Optional<ClusterConfiguration> optional = clusters.stream().filter(c -> c.getName().equals(cluster)).findFirst();
            if (!optional.isPresent()) {
                throw new ConfigurationException(String.format("%s pointcut [cluster] attr %s Cluster does not exist" , pointExpressions,cluster));
            }
            ClusterConfiguration clusterConf = optional.get();
            Set<String> clusterNodes = clusterConf.getNodes();
            for (String node : nodes) {
                if (!clusterNodes.contains(node)) {
                    throw new ConfigurationException(String.format("%s pointcut - [nodes] attr [%s] Datasource Not belonging to [%s] cluster" , pointExpressions ,node , cluster));
                }
            }
        }
    }

    public String getDefaultClusterName() {
        if (CollectionUtils.isEmpty(clusters)) {
            return null;
        }
        Optional<ClusterConfiguration> clusterConfiguration = this.clusters
                .stream()
                .filter(ClusterConfiguration::getDefaulted)
                .findFirst();
        if (!clusterConfiguration.isPresent()) {
            throw new SqlXRuntimeException("No default cluster found");
        }
        return clusterConfiguration.get().getName();
    }

    private void validatePointcuts() {
        if (CollectionUtils.isEmpty(pointcuts)) {
            return;
        }

        for (PointcutConfiguration pointcut : pointcuts) {
            pointcut.validate();
            String expression = pointcut.getExpression();
            String cluster = pointcut.getCluster();
            List<String> nodes = pointcut.getNodes();
            boolean propagation = pointcut.getPropagation();
            validatePointcutRoutingAttr(expression , cluster , nodes , propagation);
        }
        List<String> expressions = pointcuts.stream().map(PointcutConfiguration::getExpression).collect(Collectors.toList());
        List<String> duplicates = expressions.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new ConfigurationException(String.format("pointcut expression duplicate %s" , duplicates));
        }
    }

    private void validateCluster() {
        List<String> clusterNames = clusters.stream().map(ClusterConfiguration::getName).collect(Collectors.toList());
        Collection<String> clusterNameSubtract = CollectionUtils.subtract(clusterNames, new HashSet<>(clusterNames));
        if (CollectionUtils.isNotEmpty(clusterNameSubtract)) {
            throw new ConfigurationException(String.format("cluster name duplicate %s" , clusterNameSubtract));
        }


        if (CollectionUtils.isNotEmpty(clusters)) {
            if (clusters.size() > 1) {
                // Check if there is at least one defaulted cluster
                boolean hasDefaultedCluster = clusters.stream().anyMatch(ClusterConfiguration::getDefaulted);
                if (!hasDefaultedCluster) {
                    throw new ConfigurationException("When there are multiple clusters, at least one cluster must be set as defaulted");
                }
            }

            // Set the default cluster if there is only one cluster
            if (clusters.size() == 1) {
                ClusterConfiguration singleCluster = clusters.get(0);
                singleCluster.setDefaulted(true);
            }

            for (ClusterConfiguration cluster : clusters) {
                cluster.validate();
            }
        }
    }

    public void init() {
        if (CollectionUtils.isNotEmpty(clusters)) {
            for (ClusterConfiguration cluster : clusters) {
                cluster.setNodeAttributes(getNodeAttributes(cluster.getNodes()));
            }
        }
    }

    /**
     * Synchronized method to remove a data source configuration by its name.
     * This method iterates over the data source configurations, finds the one with the specified name,
     * removes it, and attempts to validate the remaining configurations.
     * If validation fails, the removed configuration is re-added and an exception is thrown.
     *
     * @param name The name of the data source configuration to be removed.
     * @return true if the data source configuration was successfully removed; otherwise, false
     */
    public synchronized boolean removeDataSourceConfiguration(String name) {
        boolean removed = false;
        Iterator<DataSourceConfiguration> iterator = dataSources.iterator();
        while (iterator.hasNext()) {
            DataSourceConfiguration dsConf = iterator.next();
            if (dsConf.getName().equals(name)) {
                iterator.remove();
                try {
                    validate();
                } catch (Exception e) {
                    dataSources.add(dsConf);
                    throw new ManagementException(e);
                }
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Synchronized method to remove a node from a specified cluster.
     * This method iterates over the cluster configurations, finds the one with the specified cluster name,
     * and removes the specified node from that cluster configuration. It then performs validation.
     *
     * @param clusterName The name of the cluster.
     * @param nodeName The name of the node to be removed.
     * @return true if the node was successfully removed; otherwise, false
     */
    public synchronized boolean removeNodeInCluster(String clusterName, String nodeName) {
        boolean removed = false;
        for (ClusterConfiguration conf : clusters) {
            if (conf.getName().equals(clusterName) && conf.getNodes().contains(nodeName)) {
                NodeAttribute rna = conf.removeNode(nodeName);
                if (rna != null) {
                    PointcutConfiguration pointcutConfiguration = pointcuts.stream().filter(pointcut -> StringUtils.equals(pointcut.getCluster(), clusterName)).findFirst().orElse(null);
                    if (pointcutConfiguration != null) {
                        pointcutConfiguration.removeNode(nodeName);
                    }
                }
                validate();
                removed = true;
            }
        }
        return removed;
    }

    public synchronized boolean addNodeInCluster(String clusterName, String nodeName) {
        ClusterConfiguration clusterConf = getCluster(clusterName);
        if (clusterConf == null) {
            throw new IllegalArgumentException("No such cluster configuration: " + clusterName);
        }
        DataSourceConfiguration dsConf = getDataSourceConfByName(nodeName);
        if (dsConf == null) {
            throw new IllegalArgumentException("No such datasource configuration: " + nodeName);
        }
        NodeAttribute node = dsConf.getNodeAttribute();
        try {
            clusterConf.addNode(node);
            validate();
        } catch (Exception e) {
            clusterConf.removeNode(node);
            throw e;
        }
        return true;
    }

    /**
     * Checks if the specified data source is present in the collection of data sources.
     *
     * @param nodeName the name of the data source to search for
     * @return true if a data source with the specified name exists; false otherwise
     */
    public boolean containsDataSource(String nodeName) {
        return dataSources.stream().anyMatch(node -> node.getName().equals(nodeName));
    }

    /**
     * Checks if the specified cluster is present in the collection of clusters.
     *
     * @param cluster the name of the cluster to search for
     * @return true if the cluster is found; false otherwise
     */
    public boolean containsCluster(String cluster) {
        return clusters.stream().anyMatch(c -> c.getName().equals(cluster));
    }

    /**
     * Retrieves a ClusterConfiguration object from the list of clusters based on the specified name.
     *
     * @param name The name of the cluster to be retrieved.
     * @return The ClusterConfiguration object that matches the given name, or null if no such cluster exists.
     */
    public ClusterConfiguration getCluster(String name) {
        return clusters.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Adds a new DataSourceConfiguration to the collection of data sources.
     *
     * This method is synchronized to ensure thread safety when adding
     * configurations. It first validates the provided DataSourceConfiguration
     * object. If the validation is successful, it adds the configuration to the
     * dataSources list and attempts to add a load balancing option based on
     * the node attribute of the configuration.
     *
     * If adding the load balancing option or the subsequent validation fails,
     * the method will remove the added DataSourceConfiguration and the load
     * balancing option to maintain consistency. Any exception thrown during
     * this process will be propagated up the call stack.
     *
     * @param dataSourceConf The DataSourceConfiguration to be added.
     * @throws ManagementException If validation fails or if there's an error adding
     *                   the load balancing option.
     */
    public synchronized void addDataSourceConfiguration(DataSourceConfiguration dataSourceConf) {
        log.info("Starting to add DataSourceConfiguration: {}", dataSourceConf);
        dataSourceConf.validate();
        if (log.isDebugEnabled()) {
            log.debug("DataSourceConfiguration validated successfully: {}", dataSourceConf);
        }

        dataSources.add(dataSourceConf);
        log.info("DataSourceConfiguration added to dataSources: {}", dataSourceConf);

        try {
            validate();
        } catch (Exception e) {
            log.error("Error occurred while adding DataSourceConfiguration: {}", e.getMessage());
            dataSources.remove(dataSourceConf);
            log.info("DataSourceConfiguration removed from dataSources due to error: {}", dataSourceConf);
            throw new ManagementException(e);
        }
    }


    public synchronized void addClusterConfiguration(ClusterConfiguration configuration) {
        log.info("Starting to add ClusterConfiguration: {}", configuration);
        Set<NodeAttribute> nodeAttributes = new HashSet<>();
        for (String node : configuration.getNodes()) {
            if (!containsDataSource(node)) {
                throw new IllegalArgumentException("No such datasource configuration: " + node);
            }
            DataSourceConfiguration dsConf = getDataSourceConfByName(node);
            nodeAttributes.add(dsConf.getNodeAttribute());
        }
        configuration.setNodeAttributes(nodeAttributes);
        configuration.validate();
        clusters.add(configuration);
        try {
            validate();
            if (log.isDebugEnabled()) {
                log.debug("ClusterConfiguration validated successfully: {}", configuration);
            }
        } catch (ConfigurationException e) {
            log.error("Error occurred while adding ClusterConfiguration: {}",configuration,e);
            clusters.remove(configuration);
            throw e;
        }


    }

    /**
     * Removes the cluster configuration with the specified name.
     *
     * @param name the name of the cluster configuration to be removed
     */
    public synchronized void removeClusterConfiguration(String name) {
        log.info("Removing cluster configuration with name: {}", name);
        boolean isRemoved = clusters.removeIf(c -> c.getName().equals(name));
        if (isRemoved) {
            log.info("Cluster configuration with name {} has been successfully removed.", name);
        } else {
            log.warn("No cluster configuration found with name: {}", name);
        }
    }
}
