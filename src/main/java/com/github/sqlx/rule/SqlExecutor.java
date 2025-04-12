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
package com.github.sqlx.rule;

import com.github.sqlx.RoutingContext;
import com.github.sqlx.integration.springboot.RouteAttribute;
import com.github.sqlx.util.ArrayUtils;
import com.github.sqlx.util.StringUtils;

import java.util.Arrays;

/**
 * Represents a SQL executor that can execute SQL functions with specified routing attributes.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class SqlExecutor {


    private SqlExecutor() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Executes the given SQL function with the specified cluster and nodes.
     *
     * @param function the SQL function to be executed
     * @param nodes the nodes to route the SQL function to
     * @param <T> the type of the result returned by the function
     * @return the result of the function execution
     * @throws IllegalArgumentException if the SQL function is null or if both cluster and nodes are empty
     */
    public static <T> T execute(SqlFunction<T> function, String... nodes) {
        if (function == null) {
            throw new IllegalArgumentException("SQL function must not be null");
        }
        if (ArrayUtils.isEmpty(nodes)) {
            throw new IllegalArgumentException("cluster or nodes must not be empty");
        }
        RouteAttribute existsRouteAttr = RoutingContext.getRoutingAttribute();
        RouteAttribute ra = new RouteAttribute(null, Arrays.asList(nodes), false, true, null, null);
        try {
            RoutingContext.force(ra);
            return function.run();
        } finally {
            RoutingContext.removeResource(ra);
            if (existsRouteAttr != null) {
                RoutingContext.force(existsRouteAttr);
            }
        }
    }
}