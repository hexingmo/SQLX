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
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.rule.RoutingKey;
import com.github.sqlx.rule.RouteRule;
import com.github.sqlx.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A data source class for cluster routing, extending {@link AbstractSqlXDataSource}.
 * This class is responsible for selecting the appropriate cluster based on the routing attributes,
 * and then selecting the data source within that cluster based on the routing rules.
 *
 * @author He Xing Mo
 * @since 1.0
 */

public class ClusterDataSource extends AbstractSqlXDataSource {

    /**
     * Cluster management object, responsible for providing cluster information
     */
    private final ClusterManager clusterManager;

    /**
     * Constructor, initializing the data source manager and cluster manager.
     *
     * @param datasourceManager Data source manager, managing multiple data sources
     * @param clusterManager Cluster manager, managing multiple clusters
     */
    public ClusterDataSource(DatasourceManager datasourceManager, ClusterManager clusterManager , EventListener eventListener) {
        super(datasourceManager , eventListener);
        this.clusterManager = clusterManager;
    }


    /**
     * Gets the data source for routing based on the SQL statement.
     * First, it tries to find a cluster based on the routing attributes.
     * If no cluster is found, it uses the default cluster.
     * Then, routing is performed within the cluster based on the routing rules to determine the final data source.
     *
     * @param sql SQL statement, used for routing judgment
     * @return Returns the selected data source wrapped in a {@link RoutedDataSource} object
     */
    @Override
    public RoutedDataSource getDataSource(String sql) {
        // TODO 注解 sql 优先级？
        Cluster cluster = lookingForCluster();
        RouteRule rule = cluster.getRule();
        RouteInfo routeInfo = rule.route(new RoutingKey().setSql(sql).setCluster(cluster));
        DataSource dataSource = getDataSourceWithName(routeInfo.getHitNodeAttr().getName());
        return new RoutedDataSource(dataSource , routeInfo);
    }

    /**
     * Retrieves a data source that is suitable for obtaining database metadata
     * within a cluster context. This method is typically used when metadata
     * information is required, such as during the initialization phase or when
     * setting up connections.
     *
     * The method first determines the appropriate cluster using the
     * `lookingForCluster` method. If all data sources managed by the
     * `datasourceManager` are of the same database product, it returns the first
     * data source from the list. Otherwise, it collects the data sources from
     * the nodes within the identified cluster and returns the first one.
     *
     * @return a RoutedDataSource that can be used to access database metadata,
     *         with routing information including the cluster context.
     */
    @Override
    public RoutedDataSource getDataSourceForDatabaseMetaData() {
        Cluster cluster = lookingForCluster();
        DataSource dataSource;
        if (datasourceManager.isSameDatabaseProduct()) {
            dataSource = datasourceManager.getDataSourceList().get(0);
        } else {
            List<String> names = cluster.getNodes().stream().map(NodeAttribute::getName).collect(Collectors.toList());
            List<DataSource> dataSourceList = getDataSourceWithName(names);
            dataSource = dataSourceList.get(0);
        }
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setCluster(cluster);
        return new RoutedDataSource(dataSource , routeInfo);
    }

    private Cluster lookingForCluster() {
        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        if (ra != null && StringUtils.isNotBlank(ra.getCluster())) {
            return clusterManager.getCluster(ra.getCluster());
        }
        return clusterManager.getDefaultCluster();
    }
}
