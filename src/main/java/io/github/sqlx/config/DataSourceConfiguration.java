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

package io.github.sqlx.config;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.NodeState;
import io.github.sqlx.exception.ConfigurationException;
import io.github.sqlx.jdbc.datasource.DataSourceAttribute;
import io.github.sqlx.util.JdbcUtils;
import io.github.sqlx.util.MapUtils;
import io.github.sqlx.util.StringUtils;
import lombok.Data;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration information of a data source.
 * This class implements the ConfigurationValidator interface for configuration validation.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Data
public class DataSourceConfiguration implements ConfigurationValidator {

    /**
     * Represents the unique identifier for the data source.
     */
    private String name;

    /**
     * Indicates whether the data source is the default data source.
     */
    private Boolean defaulted = false;

    /**
     * The fully qualified class name of the data source
     */
    private String dataSourceClass;

    /**
     * load balancing weight
     */
    private Double weight;

    /**
     * Heartbeat detection sql
     */
    private String heartbeatSql = "SELECT 1";

    /**
     * Heartbeat interval cycle in milliseconds
     */
    private long heartbeatInterval = 10000;

    /**
     * Routing node attributes
     */
    private NodeAttribute nodeAttribute;

    /**
     * The name of the method that can be invoked to initialize
     * the data source when it is first created or configured.
     */
    private String initMethod;


    /**
     * A method name that can be invoked to clean up
     * resources when the data source is no longer needed.
     */
    private String destroyMethod;


    /**
     * Path to the SQL script that will be executed to initialize the database schema.
     */
    private String initSqlScript;

    /**
     * Native data source property configuration
     */
    private Map<String , String> props = new HashMap<>();

    /**
     * Gets the routing node attribute instance
     * If the nodeAttribute is not initialized, it will be initialized
     *
     * @return Initialized RoutingNodeAttribute instance
     */
    public synchronized NodeAttribute getNodeAttribute() {
        if (nodeAttribute == null) {
            nodeAttribute = new DataSourceAttribute(getJdbcUrl() , NodeState.UNKNOWN ,name, weight , heartbeatSql , heartbeatInterval , destroyMethod);
        }
        return nodeAttribute;
    }

    /**
     * add a config property to the data source
     *
     * @param key   property key
     * @param value property value
     */
    public void addProperty(String key, String value) {
        this.props.put(key, value);
    }

    public String removeProperty(String key) {
        return this.props.remove(key);
    }


    public String getDriverClass() {
        return props.get("driverClassName");
    }

    /**
     * Gets the JDBC URL of the data source
     * Attempts to obtain the URL in the order of "url", "jdbc-url", and "jdbcUrl"
     *
     * @return Data source JDBC URL
     */
    public String getJdbcUrl() {
        String url = props.get("url");
        if (StringUtils.isBlank(url)) {
            url = props.get("jdbc-url");
        }
        if (StringUtils.isBlank(url)) {
            url = props.get("jdbcUrl");
        }
        return url;
    }

    /**
     * Gets the JDBC username of the data source
     *
     * @return Data source JDBC username
     */
    public String getUsername() {
        return props.get("username");
    }

    public String getPassword() {
        return props.get("password");
    }

    /**
     * Validates the configuration to ensure that essential information is not missing
     * Throws a ConfigurationException if validation fails
     */
    @Override
    public void validate() {
        if (StringUtils.isBlank(name)) {
            throw new ConfigurationException("dataSources [name] attr must not be empty");
        }
        if (StringUtils.isBlank(dataSourceClass)) {
            throw new ConfigurationException("dataSources [dataSourceClass] attr must not be null");
        }
        Class<?> dsClass;
        try {
            dsClass = Class.forName(dataSourceClass);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(String.format("%s Class Not Found" , dataSourceClass));
        }

        if (StringUtils.isNotBlank(destroyMethod)) {
            try {
                dsClass.getDeclaredMethod(destroyMethod);
            } catch (NoSuchMethodException e) {
                throw new ConfigurationException(String.format("Destroy Method [%s] does not exist in %s" , destroyMethod , dataSourceClass));
            }
        }

        if (StringUtils.isNotBlank(initMethod)) {
            try {
                dsClass.getDeclaredMethod(initMethod);
            } catch (NoSuchMethodException e) {
                throw new ConfigurationException(String.format("Init Method [%s] does not exist in %s" , initMethod , dataSourceClass));
            }
        }

        if (MapUtils.isEmpty(props)) {
            throw new ConfigurationException("dataSources [props] attr must not be empty");
        }
        if (StringUtils.isBlank(getDriverClass())) {
            throw new ConfigurationException("dataSources [props] attr must contain driverClassName");
        }
        if (StringUtils.isBlank(getJdbcUrl())) {
            throw new ConfigurationException("dataSources [props] attr must contain jdbcUrl");
        }
        if (StringUtils.isBlank(getUsername())) {
            throw new ConfigurationException("dataSources [props] attr must contain username");
        }
        if (StringUtils.isBlank(getPassword())) {
            throw new ConfigurationException("dataSources [props] attr must contain password");
        }

        Class<? extends Driver> driverClass;
        try {
            driverClass = (Class<? extends Driver>) Class.forName(getDriverClass());
        } catch (Exception e) {
            throw new ConfigurationException(String.format("Driver %s Class Not Found" , getDriverClass()));
        }
        boolean succeed = JdbcUtils.testConnection(driverClass, getJdbcUrl(), getUsername(), getPassword());
        if (!succeed) {
            throw new ConfigurationException(String.format("test connection failed , driver class : %s , url : %s , username : %s , password : **" , getDriverClass() , getJdbcUrl() , getUsername()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSourceConfiguration)) return false;
        DataSourceConfiguration that = (DataSourceConfiguration) o;
        return Objects.equals(this.name, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
