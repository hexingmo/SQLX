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

package com.github.sqlx;


import com.github.sqlx.integration.springboot.RouteAttribute;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class RoutingContext {

    private static final String ROUTED_DATA_SOURCE_NAME_KEY = "ROUTED_DATA_SOURCE_NAME";

    private static final String FORCE_ROUTING_ATTR = "FORCE_ROUTING_ATTR";


    private static final InheritableThreadLocal<Map<Object, Object>> RESOURCES = new InheritableThreadLocal<Map<Object, Object>>() {
        @Override
        protected Map<Object, Object> initialValue() {
            return new HashMap<>(2 << 3);
        }
    };


    private RoutingContext() {
        throw new IllegalStateException("Instantiating RoutingContext is not allowed");
    }


    public static void clear() {
        RESOURCES.remove();
    }

    public static void addResource(Object key , Object val) {
        if (key == null) {
            throw new IllegalArgumentException("Invalid key parameter cannot be null");
        }
        if (val == null) {
            throw new IllegalArgumentException("Invalid Value parameter cannot be null");
        }
        Map<Object, Object> map = RESOURCES.get();
        if (Objects.isNull(map)) {
            map = new HashMap<>(2 << 3);
            RESOURCES.set(map);
        }
        map.put(key , val);
    }

    public static void removeResource(Object key) {
        Map<Object, Object> map = RESOURCES.get();
        if (Objects.nonNull(map)) {
            map.remove(key);
        }
    }

    public static Object getResource(Object key) {
        return RESOURCES.get().get(key);
    }

    public static Map<Object, Object> getResources() {
        return RESOURCES.get();
    }

    public static void force(RouteAttribute ra) {
        addResource(FORCE_ROUTING_ATTR, ra);
    }

    public static RouteAttribute getRoutingAttribute() {
        return (RouteAttribute) getResource(FORCE_ROUTING_ATTR);
    }
}
