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
import org.joor.Reflect;
import javax.sql.DataSource;
import java.util.Map;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class ReflectNameMatchesDataSourceInitializer implements GenericDataSourceInitializer<DataSource> {

    @Override
    public DataSource initialize(DataSourceConfiguration dataSourceConf) {
        DataSource dataSource = Reflect.onClass(dataSourceConf.getDataSourceClass().trim()).create().get();
        configDataSource(dataSource , dataSourceConf.getProps());
        return dataSource;
    }

    private void configDataSource(DataSource dataSource , Map<String, String> properties) {
        DataSourceFields dataSourceFields = DataSourceFields.forType(dataSource.getClass());
        dataSourceFields.bindValue(dataSource , properties);
    }
}
