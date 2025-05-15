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
package io.github.sqlx.endpoint.jmx;

import io.github.sqlx.NodeState;
import io.github.sqlx.NodeAttribute;
import io.github.sqlx.Version;
import io.github.sqlx.cluster.Cluster;
import io.github.sqlx.cluster.ClusterManager;
import io.github.sqlx.config.ClusterConfiguration;
import io.github.sqlx.config.DataSourceConfiguration;
import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.exception.ManagementException;
import io.github.sqlx.exception.SqlXRuntimeException;
import io.github.sqlx.integration.datasource.DataSourceInitializer;
import io.github.sqlx.jdbc.datasource.DataSourceWrapper;
import io.github.sqlx.jdbc.datasource.DatasourceManager;
import io.github.sqlx.jdbc.transaction.Transaction;
import io.github.sqlx.listener.EventListener;
import io.github.sqlx.rule.group.CompositeRouteGroup;
import io.github.sqlx.rule.group.DefaultRouteGroup;
import io.github.sqlx.rule.group.NoneClusterRouteGroupBuilder;
import io.github.sqlx.rule.group.RouteGroup;
import io.github.sqlx.util.CollectionUtils;
import io.github.sqlx.util.JsonUtils;
import io.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.management.JMException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class StatManager implements StatManagerMBean {

    private static CompositeType DATASOURCE_COMPOSITE_TYPE;

    private static CompositeType CLUSTER_COMPOSITE_TYPE;

    private final SqlXConfiguration sqlXConfiguration;

    private final DataSourceInitializer dataSourceInitializer;

    private final DatasourceManager datasourceManager;

    private final ClusterManager clusterManager;

    private final List<RouteGroup<?>> routingGroups;

    private final EventListener eventListener;

    private final Transaction transaction;

    static {
        initDataSourceCompositeType();
        initClusterCompositeType();
    }

    public StatManager(SqlXConfiguration sqlXConfiguration, DataSourceInitializer dataSourceInitializer, DatasourceManager datasourceManager, ClusterManager clusterManager, List<RouteGroup<?>> routingGroups , EventListener eventListener , Transaction transaction) {
        this.sqlXConfiguration = sqlXConfiguration;
        this.dataSourceInitializer = dataSourceInitializer;
        this.datasourceManager = datasourceManager;
        this.clusterManager = clusterManager;
        this.routingGroups = routingGroups;
        this.eventListener = eventListener;
        this.transaction = transaction;
    }

    @Override
    public synchronized void removeDatasource(String name) {

        log.info("Starting the removal process for datasource: {}", name);
        if (clusterManager != null) {
            clusterManager.removeNode(name);
        } else {
            log.warn("Cluster manager is null. Skipping removal from cluster manager for datasource: {}", name);
        }
        datasourceManager.removeDataSource(name);
    }

    @Override
    public void removeNodeInCluster(String clusterName, String nodeName) {
        log.info("Attempting to remove node: {} from cluster: {}", nodeName, clusterName);
        if (clusterManager == null) {
            throw new ManagementException("clusterManager is null. Please check if the cluster mode is enabled in the configuration. Cannot perform the operation to remove a node from the cluster.");
        }
        clusterManager.removeNodeInCluster(clusterName , nodeName);
    }

    @Override
    public void addNodeInCluster(String clusterName, String nodeName) {
        log.info("Attempting to add node: {} to cluster: {}", nodeName, clusterName);
        if (clusterManager == null) {
            throw new ManagementException("clusterManager is null. Please check if the cluster mode is enabled in the configuration. Cannot perform the operation to remove a node from the cluster.");
        }
        if (!sqlXConfiguration.containsDataSource(nodeName)) {
            throw new IllegalArgumentException("No such datasource configuration: " + nodeName);
        }
        clusterManager.addNodeInCluster(clusterName , nodeName);
    }

    @Override
    public String getVersion() {
        return Version.getVersion();
    }

    @Override
    public TabularData getDataSourceList() throws JMException {
        CompositeType rowType = DATASOURCE_COMPOSITE_TYPE;
        String[] indexNames = rowType.keySet().toArray(new String[0]);

        TabularType tabularType = new TabularType("DataSourceStat", "DataSourceStat", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        List<DataSourceConfiguration> dataSources = sqlXConfiguration.getDataSources();
        for (DataSourceConfiguration dsConf : dataSources) {
            data.put(getDataSourceCompositeData(dsConf));
        }
        return data;
    }

    @Override
    public String getDefaultCluster() {
        return null;
    }

    @Override
    public TabularData getClusterList() throws JMException {
        CompositeType rowType = CLUSTER_COMPOSITE_TYPE;
        String[] indexNames = rowType.keySet().toArray(new String[0]);

        TabularType tabularType = new TabularType("ClusterStat", "ClusterStat", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        if (CollectionUtils.isNotEmpty(sqlXConfiguration.getClusters())) {
            for (ClusterConfiguration conf : sqlXConfiguration.getClusters()) {
                data.put(getClusterCompositeData(conf));
            }
        }
        return data;
    }

    @Override
    public void setNodeState(String nodeName, String nodeState) {
        NodeState state = null;
        for (NodeState ns : NodeState.values()) {
            if (StringUtils.equalsIgnoreCase(ns.name() , nodeState)) {
                state = ns;
                break;
            }
        }
        if (state == null) {
            throw new ManagementException(String.format("Invalid node state: %s , The valid NodeState enum includes %s", nodeState , Arrays.toString(NodeState.values())));
        }
        datasourceManager.setNodeState(nodeName , state);
    }

    @Override
    public void setNodeWeight(String nodeName, Double weight) {
        datasourceManager.setNodeWeight(nodeName , weight);
    }

    @Override
    public synchronized void addDataSource(String datasourceConfJson) {
        if (StringUtils.isBlank(datasourceConfJson)) {
            throw new ManagementException("datasourceConfJson cannot be empty");
        }
        log.info("Received datasource configuration json: {}", datasourceConfJson);
        DataSourceConfiguration dsConf = JsonUtils.fromJson(datasourceConfJson , DataSourceConfiguration.class);
        if (log.isDebugEnabled()) {
            log.debug("Parsed DataSourceConfiguration: {}", dsConf);
        }
        // TODO 任意一步失败了都要回滚上一步
        sqlXConfiguration.addDataSourceConfiguration(dsConf);
        DataSource dataSource = dataSourceInitializer.initialize(dsConf);
        DataSourceWrapper dataSourceWrapper = new DataSourceWrapper(dsConf.getName(), dataSource, dsConf.getNodeAttribute() , dsConf.getDefaulted());
        datasourceManager.addDataSource(dsConf.getName() , dataSourceWrapper);
    }

    @Override
    public synchronized void addCluster(String clusterConfJson) {
        if (StringUtils.isBlank(clusterConfJson)) {
            throw new ManagementException("clusterConfJson cannot be empty");
        }
        log.info("Received cluster configuration json: {}", clusterConfJson);
        ClusterConfiguration configuration = JsonUtils.fromJson(clusterConfJson , ClusterConfiguration.class);
        if (log.isDebugEnabled()) {
            log.debug("Parsed ClusterConfiguration: {}", configuration);
        }
        sqlXConfiguration.addClusterConfiguration(configuration);
        Cluster cluster = new Cluster();
        cluster.setName(configuration.getName());
        cluster.setNodes(configuration.getNodeAttributes());
        CompositeRouteGroup compositeRoutingGroup = new CompositeRouteGroup(eventListener , transaction);
        compositeRoutingGroup.installFirst(routingGroups);
        DefaultRouteGroup defaultRoutingGroup = NoneClusterRouteGroupBuilder.builder()
                .sqlXConfiguration(sqlXConfiguration)
                .sqlParser(sqlXConfiguration.getSqlParser())
                .transaction(transaction)
                // TODO
                .datasourceManager(datasourceManager)
                .build();
        compositeRoutingGroup.installLast(defaultRoutingGroup);
        cluster.setRule(compositeRoutingGroup);
        try {
            clusterManager.addCluster(configuration.getName() , cluster);
        } catch (Exception e) {
            log.error("Failed to add cluster: {}", configuration.getName(), e);
            sqlXConfiguration.removeClusterConfiguration(configuration.getName());
            throw new ManagementException("Failed to add cluster: " + e.getMessage(), e);
        }
    }

    private CompositeDataSupport getDataSourceCompositeData(DataSourceConfiguration dsConf) throws JMException {
        Map<String, Object> map = new HashMap<>();
        map.put("Name" , dsConf.getName());
        map.put("State" , dsConf.getNodeAttribute().getNodeState().name());
        map.put("Available" , dsConf.getNodeAttribute().getNodeState().isAvailable());
        map.put("DataSourceClass" , dsConf.getDataSourceClass());
        map.put("Url" , dsConf.getJdbcUrl());
        map.put("Username" , dsConf.getUsername());
        map.put("Weight" , dsConf.getWeight());
        map.put("HeartbeatSql" , dsConf.getHeartbeatSql());
        map.put("HeartbeatInterval" , dsConf.getHeartbeatInterval());
        map.put("DestroyMethod" , dsConf.getDestroyMethod());

        return new CompositeDataSupport(DATASOURCE_COMPOSITE_TYPE, map);
    }

    private CompositeDataSupport getClusterCompositeData(ClusterConfiguration conf) throws JMException {
        Map<String, Object> map = new HashMap<>();
        map.put("Name" , conf.getName());
        map.put("ReadLoadBalanceClass" , conf.getReadLoadBalanceClass());
        map.put("WriteLoadBalanceClass" , conf.getWriteLoadBalanceClass());


        Set<NodeAttribute> nodes = conf.getNodeAttributes();
        List<String> nodesStr = new ArrayList<>();
        for (NodeAttribute node : nodes) {
            String nodeStr = "Name:" + node.getName() + " ,State: " + node.getNodeState().name() + " ,Weight: " + node.getWeight();
            nodesStr.add(nodeStr);
        }
        map.put("Nodes" , nodesStr.toArray(new String[0]));
        return new CompositeDataSupport(CLUSTER_COMPOSITE_TYPE, map);
    }

    private static void initDataSourceCompositeType() {

        if (DATASOURCE_COMPOSITE_TYPE != null) {
            return;
        }

        OpenType<?>[] indexTypes = new OpenType<?>[] {
                SimpleType.STRING,
                SimpleType.STRING,
                SimpleType.BOOLEAN,
                SimpleType.STRING,
                SimpleType.STRING,
                SimpleType.STRING,
                SimpleType.DOUBLE,
                SimpleType.STRING,
                SimpleType.STRING,
                SimpleType.LONG,
                SimpleType.STRING

        };

        String[] indexNames = {
                "Name",
                "State",
                "Available",
                "DataSourceClass",
                "Url",
                "Username",
                "Weight",
                "Type",
                "HeartbeatSql",
                "HeartbeatInterval",
                "DestroyMethod"
        };

        try {
            DATASOURCE_COMPOSITE_TYPE = new CompositeType("DataSourceStatistic",
                    "DataSourceStatistic",
                    indexNames,
                    indexNames,
                    indexTypes);
        } catch (OpenDataException e) {
            throw new SqlXRuntimeException(e);
        }
    }

    private static void initClusterCompositeType() {
        if (CLUSTER_COMPOSITE_TYPE != null) {
            return;
        }

        try {
            OpenType<?>[] indexTypes = new OpenType<?>[] {
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    ArrayType.getArrayType(SimpleType.STRING),
            };

            String[] indexNames = {
                    "Name",
                    "ReadLoadBalanceType",
                    "WriteLoadBalanceType",
                    "ReadLoadBalanceClass",
                    "WriteLoadBalanceClass",
                    "Nodes"
            };

            CLUSTER_COMPOSITE_TYPE = new CompositeType("ClusterStatistic",
                    "ClusterStatistic",
                    indexNames,
                    indexNames,
                    indexTypes);
        } catch (Exception e) {
            throw new SqlXRuntimeException(e);
        }
    }
}
