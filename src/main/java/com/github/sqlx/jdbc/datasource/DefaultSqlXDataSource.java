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
package com.github.sqlx.jdbc.datasource;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.RoutingContext;
import com.github.sqlx.cluster.Cluster;
import com.github.sqlx.cluster.ClusterManager;
import com.github.sqlx.integration.springboot.RouteAttribute;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.rule.RouteRule;
import com.github.sqlx.rule.RoutingKey;
import com.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default implementation of SqlXDataSource interface.
 * This class provides routing capabilities based on SQL statements and cluster configurations.
 * It supports read/write splitting and cluster-based routing strategies.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class DefaultSqlXDataSource extends AbstractSqlXDataSource {

    /**
     * Cluster management object, responsible for providing cluster information
     */
    private final ClusterManager clusterManager;

    /**
     * The default routing rule to be used when no cluster-specific rule is available
     */
    private final RouteRule routeRule;

    /**
     * Creates a new DefaultSqlXDataSource with the specified components.
     *
     * @param clusterManager    manager for cluster configurations
     * @param datasourceManager manager for data sources
     * @param eventListener     listener for routing events
     * @param routeRule         default routing rule
     */
    public DefaultSqlXDataSource(ClusterManager clusterManager, DatasourceManager datasourceManager, EventListener eventListener, RouteRule routeRule) {
        super(datasourceManager, eventListener);
        this.clusterManager = clusterManager;
        this.routeRule = routeRule;
        log.debug("Initialized DefaultSqlXDataSource with clusterManager: {}, routeRule: {}", 
                clusterManager.getClass().getSimpleName(), routeRule.getClass().getSimpleName());
    }

    /**
     * Routes the SQL statement to an appropriate data source based on routing rules.
     * The method first determines which cluster and rule to use, then applies the rule
     * to select a specific data source node.
     *
     * @param sql the SQL statement to be routed
     * @return a RoutedDataSource containing the selected data source and routing information
     */
    @Override
    public RoutedDataSource getDataSource(String sql) {
        Optional<Cluster> clusterOptional = lookingForCluster();
        Cluster cluster = null;
        RouteRule rule;
        if (clusterOptional.isPresent()) {
            cluster = clusterOptional.get();
            rule = cluster.getRule();
            log.debug("Using cluster-specific rule from cluster: {}", cluster.getName());
        } else {
            rule = routeRule;
            log.debug("Using default route rule");
        }
        RouteInfo routeInfo = rule.route(new RoutingKey().setSql(sql).setCluster(cluster));
        DataSource dataSource = getDataSourceWithName(routeInfo.getHitNodeAttr().getName());
        return new RoutedDataSource(dataSource, routeInfo);
    }

    /**
     * Retrieves a data source that is suitable for obtaining database metadata.
     * This method selects an appropriate data source based on database product compatibility
     * and cluster configuration.
     *
     * @return a RoutedDataSource that can be used to access database metadata
     */
    @Override
    public RoutedDataSource getDataSourceForDatabaseMetaData() {
        Optional<Cluster> clusterOptional = lookingForCluster();
        Cluster cluster = clusterOptional.orElse(null);
        DataSource dataSource;
        
        if (datasourceManager.isSameDatabaseProduct()) {
            dataSource = datasourceManager.getDataSourceList().get(0);
        } else if(Objects.nonNull(cluster)) {
            List<String> names = cluster.getNodes().stream().map(NodeAttribute::getName).collect(Collectors.toList());
            List<DataSource> dataSourceList = getDataSourceWithName(names);
            dataSource = dataSourceList.get(0);
        } else {
            dataSource = datasourceManager.getDefaultDataSource().get();
        }
        
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setCluster(cluster);
        return new RoutedDataSource(dataSource, routeInfo);
    }

    /**
     * Determines which cluster to use for routing based on the current routing context.
     * If a specific cluster is specified in the routing attributes, that cluster is used;
     * otherwise, the default cluster is selected.
     *
     * @return an Optional containing the selected Cluster, or empty if no suitable cluster is found
     */
    private Optional<Cluster> lookingForCluster() {
        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        Cluster cluster;
        
        if (ra != null && StringUtils.isNotBlank(ra.getCluster())) {
            cluster = clusterManager.getCluster(ra.getCluster());
        } else {
            cluster = clusterManager.getDefaultCluster();
        }
        
        return Optional.of(cluster);
    }
}
