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

package com.github.sqlx.util;

import com.github.sqlx.rule.RouteInfo;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public final class RoutingUtils {

    private RoutingUtils() {
        throw new IllegalAccessError("RoutingUtils does not support object instantiation");
    }

    public static void setDefaultDatabase(String database , RouteInfo routeInfo) {
        if (StringUtils.isNotBlank(database) && routeInfo != null && routeInfo.getSqlAttribute() != null) {
            routeInfo.getSqlAttribute().setDefaultDatabase(database);
        }
    }

}
