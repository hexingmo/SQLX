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

package com.github.sqlx.integration.datasource;

import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.exception.SqlXRuntimeException;
import com.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class CompositeDataSourceInitializer implements DataSourceInitializer {

    private final LinkedList<GenericDataSourceInitializer<?>> initializers = new LinkedList<>();

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    public CompositeDataSourceInitializer(Set<GenericDataSourceInitializer<?>> initializers) {
        if (Objects.nonNull(initializers)) {
            this.initializers.addAll(0 , initializers);
        }
        this.initializers.addLast(new SpringDataSourceInitializer());
        this.initializers.addLast(new ReflectNameMatchesDataSourceInitializer());
    }

    @Override
    public DataSource initialize(DataSourceConfiguration dataSourceConf) {
        String dataSourceClassName = dataSourceConf.getDataSourceClass();
        DataSource dataSource = null;
        for (GenericDataSourceInitializer<?> initializer : initializers) {
            try {
                if (initializer.supports(dataSourceClassName)) {
                    dataSource = initializer.initialize(dataSourceConf);
                    if (StringUtils.isNotBlank(dataSourceConf.getInitMethod())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Call DataSource initMethod [{}]" , dataSourceConf.getInitMethod());
                        }
                        Reflect.on(dataSource).call(dataSourceConf.getInitMethod());
                    }
                    if (StringUtils.isNotBlank(dataSourceConf.getInitSqlScript())) {
                        Resource resource = resourceLoader.getResource(dataSourceConf.getInitSqlScript());
                        try (Connection connection = dataSource.getConnection()) {
                            ScriptUtils.executeSqlScript(connection, resource);
                        } catch (Exception e) {
                            throw new SqlXRuntimeException("Failed to execute SQL script: " + dataSourceConf.getInitSqlScript(), e);
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                log.error("{} DataSource Initialization Exception" ,dataSourceConf.getName() , e);
            }
        }

        if (Objects.isNull(dataSource)) {
            throw new ConfigurationException(String.format("No such DataSourceInitializer found that supports [%s] DataSource" , dataSourceClassName));
        }

        return dataSource;
    }
}
