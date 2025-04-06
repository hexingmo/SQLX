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

import com.github.sqlx.NodeState;
import com.github.sqlx.NodeAttribute;
import com.github.sqlx.util.JdbcUtils;

import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */

public class DataSourceAttribute implements NodeAttribute {

    private final String url;

    private final String database;

    private final String databaseType;

    private NodeState nodeState;

    private final String name;

    private Double weight;

    private String heartbeatSql;

    private long heartbeatInterval;

    private final String destroyMethod;

    public DataSourceAttribute(String url, NodeState nodeState, String name, Double weight , String heartbeatSql , long heartbeatInterval , String destroyMethod) {
        this.url = url;
        this.databaseType = JdbcUtils.getDbType(url);
        this.database = JdbcUtils.getDatabaseName(url);
        this.nodeState = nodeState;
        this.name = name;
        if (weight == null) {
            weight = 0d;
        }
        this.weight = weight;
        this.heartbeatSql = heartbeatSql;
        this.heartbeatInterval = heartbeatInterval;
        this.destroyMethod = destroyMethod;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public String getDatabaseType() {
        return databaseType;
    }

    @Override
    public NodeState getNodeState() {
        return nodeState;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Double getWeight() {
        return weight;
    }

    @Override
    public void setNodeWeight(Double weight) {
        this.weight = weight;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    public String getHeartbeatSql() {
        return heartbeatSql;
    }

    public void setHeartbeatSql(String heartbeatSql) {
        this.heartbeatSql = heartbeatSql;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    @Override
    public String getDestroyMethod() {
        return this.destroyMethod;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSourceAttribute)) return false;
        DataSourceAttribute that = (DataSourceAttribute) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
