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
package io.github.sqlx.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for field-related operations.
 * Provides methods to retrieve all fields from a class, including those inherited from superclasses.
 * 
 * @author He Xing Mo
 * @since 1.0
 */
public class FieldUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FieldUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Retrieves all fields from the specified class, including fields from its superclasses.
     * 
     * @param type the class from which to retrieve fields
     * @return a list of fields from the class and its superclasses
     */
    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null) {
            // Add all fields from the current class
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            // Move to the superclass
            type = type.getSuperclass();
        }
        return fields;
    }
}
