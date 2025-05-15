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
package io.github.sqlx.integration.datasource;

import io.github.sqlx.config.DataSourceConfiguration;
import io.github.sqlx.util.StringUtils;
import org.joor.Reflect;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class SpringDataSourceInitializer implements GenericDataSourceInitializer<DataSource> {

    @SuppressWarnings({"unchecked" , "rawtypes"})
    @Override
    public DataSource initialize(DataSourceConfiguration dataSourceConf) {
        Map<String, String> properties = dataSourceConf.getProps();
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUsername(properties.get("username"));
        dataSourceProperties.setPassword(properties.get("password"));

        String driverClassName = properties.get("driverClassName");
        if (StringUtils.isBlank(driverClassName)) {
            driverClassName = properties.get("driver-class-name");
        }
        dataSourceProperties.setDriverClassName(driverClassName);

        String url = properties.get("url");
        if (StringUtils.isBlank(url)) {
            url = properties.get("jdbc-url");
        }
        if (StringUtils.isBlank(url)) {
            url = properties.get("jdbcUrl");
        }
        dataSourceProperties.setUrl(url);

        Class type = Reflect.onClass(dataSourceConf.getDataSourceClass()).get();
        DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(type).build();
        DataSourceFields dataSourceFields = DataSourceFields.forType(type);
        dataSourceFields.bindValue(dataSource , properties);
        return dataSource;
    }
}
