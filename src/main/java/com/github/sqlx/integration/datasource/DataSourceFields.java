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

import com.github.sqlx.util.FieldUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;

import javax.sql.DataSource;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a collection of data source fields and their associated setter methods.
 * This class is used to bind values to a data source based on a map of properties.
 * It supports retrieving fields from a class, including those inherited from superclasses.
 * 
 * @author He Xing Mo
 * @since 1.0
 */
@Setter
@Slf4j
public class DataSourceFields {

    private List<DataSourceField> fields;

    /**
     * Binds values to the data source using the provided properties map.
     * 
     * @param dataSource the data source to bind values to
     * @param props a map of property names and their corresponding values
     */
    public void bindValue(DataSource dataSource, Map<String, String> props) {
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            for (DataSourceField field : fields) {
                if (field.matches(name)) {
                    field.setValue(dataSource, value);
                    if (log.isDebugEnabled()) {
                        log.debug("Bound value [{}] to field [{}] on data source [{}]", value, name, dataSource.getClass().getName());
                    }
                    break;
                }
            }
        }
    }

    /**
     * Creates a DataSourceFields instance for the specified data source type.
     * This method retrieves all fields and setter methods from the class and its superclasses.
     * 
     * @param <T> the type of the data source
     * @param type the class of the data source
     * @return a DataSourceFields instance containing all fields and their setters
     */
    public static <T extends DataSource> DataSourceFields forType(Class<T> type) {
        DataSourceFields dataSourceFields = new DataSourceFields();
        List<DataSourceField> fieldList = new ArrayList<>();

        // Get all methods from the class
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set") && method.getParameterCount() == 1) {
                // Infer the field name from the setter method name
                String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                DataSourceField field = new DataSourceField();
                field.setName(fieldName);
                field.setSetter(method);
                field.setType(method.getParameterTypes()[0]);
                fieldList.add(field);
            }
        }

        // Get all fields from the class and its superclasses
        List<Field> fields = FieldUtils.getAllFields(type);
        for (Field field : fields) {
            DataSourceField dataSourceField = new DataSourceField();
            dataSourceField.setName(field.getName());
            dataSourceField.setType(field.getType());
            dataSourceField.setField(field);
            fieldList.add(dataSourceField);
        }

        dataSourceFields.setFields(fieldList);
        return dataSourceFields;
    }

}
