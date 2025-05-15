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

import io.github.sqlx.exception.ConfigurationException;

import javax.sql.DataSource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public interface GenericDataSourceInitializer<T extends DataSource> extends DataSourceInitializer {

    default boolean supports(String dataSourceClassName) {

        boolean support = false;
        try {
            Class<?> datasourceType = Class.forName(dataSourceClassName);
            Type[] genericInterfaces = this.getClass().getGenericInterfaces();
            ParameterizedType parameterizedType = (ParameterizedType) genericInterfaces[0];
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (Objects.nonNull(typeArguments) && typeArguments.length == 1) {
                Class<?> actualType = (Class<?>) typeArguments[0];
                if (actualType.isAssignableFrom(datasourceType)) {
                    support = true;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
        return support;
    }

}
