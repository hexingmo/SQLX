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

package com.github.sqlx.rule.group;

import com.github.sqlx.exception.SqlRouteException;
import com.github.sqlx.exception.SqlXRuntimeException;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.rule.RoutingKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class CompositeRouteGroup implements RouteGroup<RouteGroup<?>> {

    private final LinkedList<RouteGroup<?>> routingGroups = new LinkedList<>();

    private final EventListener eventListener;

    private final Transaction transaction;

    public CompositeRouteGroup(EventListener eventListener , Transaction transaction) {
        this.eventListener = eventListener;
        this.transaction = transaction;
    }

    @Override
    public RouteInfo route(RoutingKey key) {
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setSql(key.getSql());
        routeInfo.setCluster(key.getCluster());
        routeInfo.setRoutingGroups(routingGroups);
        routeInfo.setBeforeNanoTime(System.nanoTime());
        routeInfo.setBeforeTimeMillis(System.currentTimeMillis());
        if (eventListener != null) {
            eventListener.onBeforeRouting(routeInfo);
        }
        Exception e = null;
        try {
            internalRouting(key , routeInfo);
            return routeInfo;
        } catch (Exception ex) {
            e = ex;
            throw ex;
        } finally {
            routeInfo.setAfterNanoTime(System.nanoTime());
            routeInfo.setAfterTimeMillis(System.currentTimeMillis());
            if (eventListener != null) {
                eventListener.onAfterRouting(routeInfo, e);
            }
        }
    }





    public synchronized void installFirst(RouteGroup<?> rule) {
        if (rule != null && isEnableGroup(rule)) {
            routingGroups.addFirst(rule);
        }
    }

    public synchronized void installFirst(List<RouteGroup<?>> rules) {
        if (rules != null && !rules.isEmpty()) {
            routingGroups.addAll(0 , rules);
            for (RouteGroup group : rules) {
                installFirst(group);
            }
        }
    }

    public synchronized void installLast(RouteGroup<?> rule) {
        if (rule != null && isEnableGroup(rule)) {
            routingGroups.addLast(rule);
        }
    }

    public synchronized void installLast(List<RouteGroup<?>> rules) {
        if (rules != null && !rules.isEmpty()) {
            for (RouteGroup<?> group : rules) {
                installLast(group);
            }
        }
    }

    @Override
    public void install(RouteGroup<?> rule) {
        throw new SqlXRuntimeException("Unsupported method install");
    }

    @Override
    public void install(List<RouteGroup<?>> rules) {
        throw new SqlXRuntimeException("Unsupported method install");
    }

    @Override
    public boolean uninstall(Class<RouteGroup<?>> type) {
        return routingGroups.removeIf(rule -> Objects.equals(type , rule.getClass()));
    }

    @Override
    public Set<RouteGroup<?>> getRules() {
        return Collections.unmodifiableSet(new HashSet<>(routingGroups));
    }

    private void internalRouting(RoutingKey key , RouteInfo routeInfo) {
        RouteGroup<?> rg = null;
        for (RouteGroup<?> routingGroup : routingGroups) {
            rg = routingGroup;
            RouteInfo ri = routingGroup.route(key);
            if (Objects.nonNull(ri)) {
                routeInfo.setHitRoutingGroup(routingGroup);
                routeInfo.setIsTransactionActive(transaction.isActive());
                if (transaction.isActive()) {
                    routeInfo.setTransactionName(transaction.getName());
                    routeInfo.setTransactionId(transaction.getTransactionId());
                }
                copyRoutingInfo(ri , routeInfo);
                break;
            }
        }
        if (routeInfo == null) {
            throw new SqlRouteException(String.format("SQL [%s] Not routed to any available node, please check the health status of the nodes" , key.getSql()));
        }
    }

    private void copyRoutingInfo(RouteInfo source , RouteInfo target) {
        target.setSqlAttribute(source.getSqlAttribute());
        target.setHitNodeAttr(source.getHitNodeAttr());
        target.setHitRule(source.getHitRule());
    }

    private boolean isEnableGroup(RouteGroup<?> group) {
        // TODO Configuration of routing rules is temporarily not supported
        return true;
    }

}
