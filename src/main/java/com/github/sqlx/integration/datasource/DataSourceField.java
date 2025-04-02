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

import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.util.StringUtils;
import lombok.Data;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represents a field in a data source, including its name, type, and associated setter method.
 * This class provides functionality to match field names and set values on a target object.
 * 
 * @author He Xing Mo
 * @since 1.0
 */
@Data
public class DataSourceField {

    private static final DefaultFormattingConversionService CONVERSION_SERVICE = new DefaultFormattingConversionService();

    private String name;

    private Class<?> type;

    private Field field;

    private Method setter;

    /**
     * Checks if the field name matches the given name.
     * 
     * @param name the name to match against
     * @return true if the names match, false otherwise
     */
    public boolean matches(String name) {
        return StringUtils.equals(this.name, name);
    }

    /**
     * Sets the value of this field on the given target object.
     * The value is converted to the appropriate type before being set.
     * 
     * @param target the target object on which to set the value
     * @param value the value to set
     * @throws ConfigurationException if setting the value fails
     */
    public void setValue(Object target, String value) {
        try {
            Object convertedValue = CONVERSION_SERVICE.convert(value, type);
            if (setter != null) {
                setter.setAccessible(true);
                setter.invoke(target, convertedValue);
            } else if (field != null) {
                field.setAccessible(true);
                field.set(target, convertedValue);
            }
        } catch (Exception e) {
            throw new ConfigurationException(String.format("Failed to set %s field [%s] with value [%s]", target.getClass(), name, value), e);
        }
    }
}
