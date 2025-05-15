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

package io.github.sqlx.jdbc.datasource;

import io.github.sqlx.NodeState;
import io.github.sqlx.NodeAttribute;
import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.exception.ManagementException;
import io.github.sqlx.exception.NoSuchDataSourceException;
import io.github.sqlx.exception.SqlXRuntimeException;
import io.github.sqlx.util.MapUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class DatasourceManager {

    private static final DataSourceNodeStateMonitor NODE_STATE_MONITOR = new DataSourceNodeStateMonitor();

    private final Map<String , DataSourceWrapper> dataSources = new ConcurrentHashMap<>();

    private final SqlXConfiguration configuration;

    public DatasourceManager(SqlXConfiguration configuration) {
        this.configuration = configuration;
        registerShutdownHook();
    }

    public synchronized void addDataSource(String name , DataSourceWrapper dataSource) {
        NODE_STATE_MONITOR.monitor(dataSource);
        dataSources.put(name , dataSource);
    }

    public synchronized void addDataSources(Map<String , DataSourceWrapper> dataSources) {
        if (MapUtils.isNotEmpty(dataSources)) {
            dataSources.forEach(this::addDataSource);
        }
    }

    public synchronized void removeDataSource(String name) {
        if (!containsDataSource(name)) {
            throw new ManagementException("No such datasource: " + name);
        }
        boolean removed = configuration.removeDataSourceConfiguration(name);
        if (removed) {
            DataSourceWrapper dataSourceWrapper = dataSources.remove(name);
            if (dataSourceWrapper != null) {
                dataSourceWrapper.destroy();
            }
        }
    }

    public synchronized DataSourceWrapper getDataSource(String name) {
        DataSourceWrapper dataSourceWrapper = dataSources.get(name);
        if (dataSourceWrapper == null) {
            throw new NoSuchDataSourceException(String.format("No DataSource with name [%s] found" , name));
        }
        return dataSourceWrapper;
    }

    public List<DataSourceWrapper> getDataSourceList() {
        return Collections.unmodifiableList(new ArrayList<>(dataSources.values()));
    }

    public DataSourceWrapper getDefaultDataSource() {
        Optional<DataSourceWrapper> optional = dataSources.values().stream().filter(DataSourceWrapper::getDefaulted).findFirst();
        if (!optional.isPresent()) {
            throw new SqlXRuntimeException("No default datasource found");
        }
        return optional.get();
    }


    public synchronized List<DataSourceWrapper> getDataSourceList(NodeState state) {
        List<DataSourceWrapper> dataSourceWrappers = new ArrayList<>();
        dataSources.forEach((k , v) -> {
            if (Objects.equals(v.getNodeAttribute().getNodeState() , state)) {
                dataSourceWrappers.add(v);
            }
        });
        return dataSourceWrappers;
    }


    public boolean containsDataSource(String nodeName) {
        return dataSources.containsKey(nodeName);
    }

    public void setNodeState(String nodeName, NodeState state) {
        log.info("Attempting to set state for node: {} to {}", nodeName, state);
        if (!containsDataSource(nodeName)) {
            throw new ManagementException("No such datasource: " + nodeName);
        }
        DataSourceWrapper dataSourceWrapper = getDataSource(nodeName);
        NodeAttribute rna = dataSourceWrapper.getNodeAttribute();
        rna.setNodeState(state);
        log.info("Successfully set state for node: {} to {}", nodeName, state);
    }

    public void setNodeWeight(String nodeName, Double weight) {
        log.info("Attempting to set weight for node: {} to {}", nodeName, weight);
        if (!containsDataSource(nodeName)) {
            throw new ManagementException("No such datasource: " + nodeName);
        }
        DataSourceWrapper dataSourceWrapper = getDataSource(nodeName);
        NodeAttribute rna = dataSourceWrapper.getNodeAttribute();
        rna.setNodeWeight(weight);
        log.info("Successfully set weight for node: {} to {}", nodeName, weight);
    }

    /**
     * Checks if all data sources use the same database type.
     * 
     * @return true if all data sources are of the same database type, false otherwise
     */
    public boolean isSameDatabaseProduct() {
        if (dataSources.isEmpty()) {
            return true;
        }

        String firstDatabaseType = null;
        for (DataSourceWrapper dataSourceWrapper : dataSources.values()) {
            String currentDatabaseType = dataSourceWrapper.getNodeAttribute().getDatabaseType();
            if (firstDatabaseType == null) {
                firstDatabaseType = currentDatabaseType;
            } else if (!firstDatabaseType.equals(currentDatabaseType)) {
                return false;
            }
        }
        return true;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down datasource manager");
            for (Map.Entry<String, DataSourceWrapper> entry : dataSources.entrySet()) {
                DataSourceWrapper dsw = entry.getValue();
                dsw.destroy();
            }
        } , "DataSource-Close-ShutdownHook"));
    }
}
