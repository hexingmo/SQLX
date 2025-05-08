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

package com.github.sqlx.integration.springboot.properties;

import lombok.Data;

import java.util.Map;

/**
 * DataSource configuration properties.
 *
 * @author jing yun
 * @since 1.0
 */
@Data
public class DataSourceProperties {

    /**
     * Name of the data source.
     */
    private String name;

    /**
     * Whether this is the default data source.
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
    private Map<String, String> props;

}
