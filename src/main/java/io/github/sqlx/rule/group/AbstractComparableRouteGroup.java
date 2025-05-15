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

package io.github.sqlx.rule.group;

import io.github.sqlx.rule.RouteRule;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractComparableRouteGroup<T extends RouteRule> implements RouteGroup<T> {


    protected final NavigableSet<T> routingRules;

    protected AbstractComparableRouteGroup(Comparator<T> comparator) {
        this.routingRules = new ConcurrentSkipListSet<>(comparator);
    }

    @Override
    public void install(T rule) {
        routingRules.add(rule);
    }

    @Override
    public void install(List<T> rules) {
        if (rules != null && !rules.isEmpty()) {
            install(rules);
        }
    }

    @Override
    public boolean uninstall(final Class<T> type) {
        if (type == null || type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        return routingRules.removeIf(rule -> Objects.equals(type, rule.getClass()));
    }

    @Override
    public Set<T> getRules() {
        return Collections.unmodifiableSet(routingRules);
    }
}
