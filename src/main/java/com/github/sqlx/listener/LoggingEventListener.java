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
package com.github.sqlx.listener;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.jdbc.CallableStatementInfo;
import com.github.sqlx.jdbc.ConnectionInfo;
import com.github.sqlx.jdbc.PreparedStatementInfo;
import com.github.sqlx.jdbc.ResultSetInfo;
import com.github.sqlx.jdbc.ProxyConnection;
import com.github.sqlx.jdbc.StatementInfo;
import com.github.sqlx.rule.RouteInfo;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.util.StringUtils;
import com.github.sqlx.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class LoggingEventListener implements EventListener {


    @Override
    public void onBeforeRouting(RouteInfo routeInfo) {
        if (log.isDebugEnabled()) {
            StringBuilder msgBuilder = new StringBuilder();
            msgBuilder.append("On Before Routing")
                    .append(System.lineSeparator())
                    .append("RoutingId: ").append(routeInfo.getRouteId()).append(System.lineSeparator())
                    .append("SQL: ").append(routeInfo.getSql()).append(System.lineSeparator())
                    .append("Time: ").append(formatMillisTime(routeInfo.getBeforeTimeMillis())).append(System.lineSeparator())
                    .append("TransactionId: ").append(StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A")).append(System.lineSeparator())
                    .append("TransactionName: ").append(StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A")).append(System.lineSeparator())
                    .append("RoutingGroups: ").append(System.lineSeparator());
            routeInfo.getRoutingGroups().forEach(rg -> msgBuilder.append(" - ").append(rg.getClass().getSimpleName()).append(": ").append("[").append(rg.getRules().stream().map(r -> r.getClass().getSimpleName()).collect(Collectors.joining(", "))).append("]").append(System.lineSeparator()));
            log.debug(msgBuilder.toString());
        }
    }

    @Override
    public void onAfterRouting(RouteInfo routeInfo, Exception e) {
        if (e != null) {
            log.error("Routing Error", e);
        }
        if (log.isDebugEnabled()) {
            String ruleName = "N/A";
            if (Objects.nonNull(routeInfo.getHitRule())) {
                ruleName = routeInfo.getHitRule().getClass().getName();
            }
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String hitGroup = "N/A";
            if (Objects.nonNull(routeInfo.getHitRoutingGroup())) {
                hitGroup = routeInfo.getHitRoutingGroup().getClass().getName();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Routing" +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(routeInfo.getAfterTimeMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Hit Group: " + hitGroup +
                    System.lineSeparator() +
                    "Hit Rule: " + ruleName +
                    System.lineSeparator() +
                    "Hit Cluster: " + cluster +
                    System.lineSeparator() +
                    "Hit Node: " + node +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(routeInfo.getTimeElapsedNanos());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeGetConnection(ConnectionInfo connectionInfo) {
        if (log.isDebugEnabled()) {
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Get Connection" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator();

            log.debug(msg);
        }
    }

    @Override
    public void onAfterGetConnection(ConnectionInfo connectionInfo, SQLException e) {
        if (e != null) {
            log.error("Get Connection Error", e);
        }
        if (log.isDebugEnabled()) {
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Get Connection" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(connectionInfo.getTimeElapsedToGetConnectionNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeConnectionClose(ConnectionInfo connectionInfo) {
        if (log.isDebugEnabled()) {
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Connection Close" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToCloseConnectionMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator();
            log.debug(msg);
        }
    }

    @Override
    public void onAfterConnectionClose(ConnectionInfo connectionInfo, SQLException e) {
        if (e != null) {
            log.error("Connection Close Error", e);
        }
        if (log.isDebugEnabled()) {
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Connection Close" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToCloseConnectionMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(connectionInfo.getTimeElapsedToCloseConnectionNs()) +
                    System.lineSeparator() +
                    "Connection Alive Time Elapsed (ms): " + TimeUtils.durationMillis(connectionInfo.getAliveTimeElapsedNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeSetAutoCommit(ConnectionInfo connectionInfo, boolean autoCommit, boolean oldAutoCommit) {

    }

    @Override
    public void onAfterSetAutoCommit(ConnectionInfo connectionInfo, boolean autoCommit, boolean oldAutoCommit, SQLException e) {

    }

    @Override
    public void onBeforeRollback(ConnectionInfo connectionInfo) {
        if (log.isDebugEnabled()) {
            RouteInfo currentRouteInfo = connectionInfo.getCurrentRouteInfo();
            List<RouteInfo> routeInfoList = connectionInfo.getRouteInfoList();

            StringBuilder sqlMsg = new StringBuilder("[");
            StringBuilder nativeSqlMsg = new StringBuilder("[");
            for (int i = 0; i < routeInfoList.size(); i++) {
                RouteInfo routeInfo = routeInfoList.get(i);
                SqlAttribute sqlAttr = routeInfo.getSqlAttribute();
                String sql = sqlAttr.getSql();
                String nativeSql = sqlAttr.getNativeSql();
                sqlMsg.append(System.lineSeparator()).append(i).append(". ").append(sql);
                nativeSqlMsg.append(System.lineSeparator()).append(i).append(". ").append(nativeSql);
            }
            sqlMsg.append("]");
            nativeSqlMsg.append("]");

            String cluster = "N/A";
            if (Objects.nonNull(currentRouteInfo.getCluster())) {
                cluster = currentRouteInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = currentRouteInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Rollback" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToRollbackMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + currentRouteInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + sqlMsg +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSqlMsg +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator();
            log.debug(msg);
        }
    }

    @Override
    public void onAfterRollback(ConnectionInfo connectionInfo, SQLException e) {
        if (e != null) {
            log.error("Rollback Error", e);
        }
        if (log.isDebugEnabled()) {
            RouteInfo currentRouteInfo = connectionInfo.getCurrentRouteInfo();
            List<RouteInfo> routeInfoList = connectionInfo.getRouteInfoList();

            StringBuilder sqlMsg = new StringBuilder("[");
            StringBuilder nativeSqlMsg = new StringBuilder("[");
            for (int i = 0; i < routeInfoList.size(); i++) {
                RouteInfo routeInfo = routeInfoList.get(i);
                SqlAttribute sqlAttr = routeInfo.getSqlAttribute();
                String sql = sqlAttr.getSql();
                String nativeSql = sqlAttr.getNativeSql();
                sqlMsg.append(System.lineSeparator()).append(i).append(". ").append(sql);
                nativeSqlMsg.append(System.lineSeparator()).append(i).append(". ").append(nativeSql);
            }
            sqlMsg.append("]");
            nativeSqlMsg.append("]");

            String cluster = "N/A";
            if (Objects.nonNull(currentRouteInfo.getCluster())) {
                cluster = currentRouteInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = currentRouteInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Rollback" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getAfterTimeToRollbackMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + currentRouteInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + sqlMsg +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSqlMsg +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(connectionInfo.getTimeElapsedToRollbackNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint) {
        if (log.isDebugEnabled()) {
            RouteInfo currentRouteInfo = connectionInfo.getCurrentRouteInfo();
            List<RouteInfo> routeInfoList = connectionInfo.getRouteInfoList();

            StringBuilder sqlMsg = new StringBuilder("[");
            StringBuilder nativeSqlMsg = new StringBuilder("[");
            for (int i = 0; i < routeInfoList.size(); i++) {
                RouteInfo routeInfo = routeInfoList.get(i);
                SqlAttribute sqlAttr = routeInfo.getSqlAttribute();
                String sql = sqlAttr.getSql();
                String nativeSql = sqlAttr.getNativeSql();
                sqlMsg.append(System.lineSeparator()).append(i).append(". ").append(sql);
                nativeSqlMsg.append(System.lineSeparator()).append(i).append(". ").append(nativeSql);
            }
            sqlMsg.append("]");
            nativeSqlMsg.append("]");

            String cluster = "N/A";
            if (Objects.nonNull(currentRouteInfo.getCluster())) {
                cluster = currentRouteInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = currentRouteInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String spt = "N/A";
            if (savepoint != null) {
                try {
                    spt = "id[" + savepoint.getSavepointId() + "] , name[" + savepoint.getSavepointName() + "]";
                } catch (Exception e) {
                    spt = "Savepoint Exception:" + e.getMessage();
                }
            }
            String msg = "On After Savepoint Rollback" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToRollbackMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + currentRouteInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + sqlMsg +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSqlMsg +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Savepoint: " + spt;
            log.debug(msg);
        }
    }

    @Override
    public void onAfterSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint, SQLException e) {
        if (e != null) {
            log.error("Savepoint Rollback Error", e);
        }
        if (log.isDebugEnabled()) {
            RouteInfo currentRouteInfo = connectionInfo.getCurrentRouteInfo();
            List<RouteInfo> routeInfoList = connectionInfo.getRouteInfoList();

            StringBuilder sqlMsg = new StringBuilder("[");
            StringBuilder nativeSqlMsg = new StringBuilder("[");
            for (int i = 0; i < routeInfoList.size(); i++) {
                RouteInfo routeInfo = routeInfoList.get(i);
                SqlAttribute sqlAttr = routeInfo.getSqlAttribute();
                String sql = sqlAttr.getSql();
                String nativeSql = sqlAttr.getNativeSql();
                sqlMsg.append(System.lineSeparator()).append(i).append(". ").append(sql);
                nativeSqlMsg.append(System.lineSeparator()).append(i).append(". ").append(nativeSql);
            }
            sqlMsg.append("]");
            nativeSqlMsg.append("]");

            String cluster = "N/A";
            if (Objects.nonNull(currentRouteInfo.getCluster())) {
                cluster = currentRouteInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = currentRouteInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String spt = "N/A";
            if (savepoint != null) {
                try {
                    spt = "id[" + savepoint.getSavepointId() + "] , name[" + savepoint.getSavepointName() + "]";
                } catch (Exception ex) {
                    spt = "Savepoint Exception:" + ex.getMessage();
                }
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On Before Savepoint Rollback" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToRollbackMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + currentRouteInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + sqlMsg +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSqlMsg +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Savepoint: " + spt +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(connectionInfo.getTimeElapsedToRollbackNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeCommit(ConnectionInfo connectionInfo) {
        if (log.isDebugEnabled()) {
            RouteInfo currentRouteInfo = connectionInfo.getCurrentRouteInfo();
            List<RouteInfo> routeInfoList = connectionInfo.getRouteInfoList();

            StringBuilder sqlMsg = new StringBuilder("[");
            StringBuilder nativeSqlMsg = new StringBuilder("[");
            for (int i = 0; i < routeInfoList.size(); i++) {
                RouteInfo routeInfo = routeInfoList.get(i);
                SqlAttribute sqlAttr = routeInfo.getSqlAttribute();
                String sql = sqlAttr.getSql();
                String nativeSql = sqlAttr.getNativeSql();
                sqlMsg.append(System.lineSeparator()).append(i).append(". ").append(sql);
                nativeSqlMsg.append(System.lineSeparator()).append(i).append(". ").append(nativeSql);
            }
            sqlMsg.append("]");
            nativeSqlMsg.append("]");

            String cluster = "N/A";
            if (Objects.nonNull(currentRouteInfo.getCluster())) {
                cluster = currentRouteInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = currentRouteInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Commit" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToCommitMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + currentRouteInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + sqlMsg +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSqlMsg +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator();
            log.debug(msg);
        }
    }

    @Override
    public void onAfterCommit(ConnectionInfo connectionInfo, SQLException e) {
        if (e != null) {
            log.error("Commit Error", e);
        }
        if (log.isDebugEnabled()) {
            RouteInfo currentRouteInfo = connectionInfo.getCurrentRouteInfo();
            List<RouteInfo> routeInfoList = connectionInfo.getRouteInfoList();

            StringBuilder sqlMsg = new StringBuilder("[");
            StringBuilder nativeSqlMsg = new StringBuilder("[");
            for (int i = 0; i < routeInfoList.size(); i++) {
                RouteInfo routeInfo = routeInfoList.get(i);
                SqlAttribute sqlAttr = routeInfo.getSqlAttribute();
                String sql = sqlAttr.getSql();
                String nativeSql = sqlAttr.getNativeSql();
                sqlMsg.append(System.lineSeparator()).append(i).append(". ").append(sql);
                nativeSqlMsg.append(System.lineSeparator()).append(i).append(". ").append(nativeSql);
            }
            sqlMsg.append("]");
            nativeSqlMsg.append("]");

            String cluster = "N/A";
            if (Objects.nonNull(currentRouteInfo.getCluster())) {
                cluster = currentRouteInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = currentRouteInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Commit" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(connectionInfo.getBeforeTimeToCommitMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + currentRouteInfo.getRouteId() +
                    System.lineSeparator() +
                    "SQL: " + sqlMsg +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSqlMsg +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(currentRouteInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(connectionInfo.getTimeElapsedToCommitNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeCreateStatement(StatementInfo statementInfo) {
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Create Statement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual Statement: N/A";
            log.debug(msg);
        }
    }

    @Override
    public void onAfterCreateStatement(StatementInfo statementInfo, SQLException e) {
        if (e != null) {
            log.error("Create Statement Error", e);
        }
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Create Statement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual Statement: " + statementInfo.getStatement() +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getTimeElapsedToCreateStatementNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeCloseStatement(StatementInfo statementInfo) {
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Close Statement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCloseMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "Got Statement Time: " + formatMillisTime(statementInfo.getAfterTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual Statement: " + statementInfo.getStatement();
            log.debug(msg);
        }
    }

    @Override
    public void onAfterCloseStatement(StatementInfo statementInfo, SQLException e) {
        if (e != null) {
            log.error("Close Statement Error", e);
        }
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Close Statement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCloseMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "Got Statement Time: " + formatMillisTime(statementInfo.getAfterTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual Statement: " + statementInfo.getStatement() +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getTimeElapsedToCloseStatementNs()) +
                    System.lineSeparator() +
                    "Statement Alive Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getAliveTimeElapsedNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeExecuteQuery(StatementInfo statementInfo) {

    }

    @Override
    public void onAfterExecuteQuery(StatementInfo statementInfo, SQLException e) {

    }

    @Override
    public void onBeforeExecute(StatementInfo statementInfo) {

    }

    @Override
    public void onAfterExecute(StatementInfo statementInfo, SQLException e) {

    }

    @Override
    public void onBeforeExecuteUpdate(StatementInfo statementInfo) {

    }

    @Override
    public void onAfterExecuteUpdate(StatementInfo statementInfo, SQLException e) {

    }

    @Override
    public void onAfterGetResultSet(StatementInfo currentStatementInfo, long timeElapsedNanos, SQLException e) {

    }

    @Override
    public void onBeforePrepareStatement(PreparedStatementInfo statementInfo) {
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Create PrepareStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual PrepareStatement: N/A";
            log.debug(msg);
        }
    }

    @Override
    public void onAfterPrepareStatement(PreparedStatementInfo statementInfo, Exception e) {
        if (e != null) {
            log.error("Create PrepareStatement Error", e);
        }
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            if (Objects.isNull(routeInfo)) {
                return;
            }
            String nativeSql = statementInfo.getNativeSql();
            if (Objects.nonNull(routeInfo) && Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo) && Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            String node = "N/A";
            if (Objects.nonNull(routeInfo) && Objects.nonNull(routeInfo.getHitNodeAttr())) {
                NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Create PrepareStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual PrepareStatement: " + statementInfo.getStatement() +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getTimeElapsedToCreateStatementNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeExecuteQuery(PreparedStatementInfo preparedStatementInfo) {

    }

    @Override
    public void onAfterExecuteQuery(PreparedStatementInfo preparedStatementInfo, SQLException e) {

    }

    @Override
    public void onBeforeExecuteUpdate(PreparedStatementInfo preparedStatementInfo) {

    }

    @Override
    public void onAfterExecuteUpdate(PreparedStatementInfo preparedStatementInfo, SQLException e) {

    }

    @Override
    public void onBeforeExecute(PreparedStatementInfo preparedStatementInfo) {

    }

    @Override
    public void onAfterExecute(PreparedStatementInfo preparedStatementInfo, SQLException e) {

    }

    @Override
    public void onBeforeAddBatch(PreparedStatementInfo preparedStatementInfo) {

    }

    @Override
    public void onAfterAddBatch(PreparedStatementInfo preparedStatementInfo, long timeElapsedNanos, SQLException e) {

    }

    @Override
    public void onBeforeClearBatch(PreparedStatementInfo preparedStatementInfo) {

    }

    @Override
    public void onAfterClearBatch(PreparedStatementInfo preparedStatementInfo, long timeElapsedNanos, SQLException e) {

    }

    @Override
    public void onBeforeExecuteBatch(PreparedStatementInfo preparedStatementInfo, long beforeTimeNs) {

    }

    @Override
    public void onAfterExecuteBatch(PreparedStatementInfo preparedStatementInfo, long afterTimeNs, long timeElapsedNanos, long[] counts, SQLException e) {

    }

    @Override
    public void onAfterPreparedStatementSet(PreparedStatementInfo statementInformation, int parameterIndex, Object value, SQLException e) {

    }

    @Override
    public void onBeforeStatementClose(PreparedStatementInfo statementInfo) {
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Close PreparedStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCloseMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "Got Statement Time: " + formatMillisTime(statementInfo.getAfterTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual PrepareStatement: " + statementInfo.getStatement();
            log.debug(msg);
        }
    }

    @Override
    public void onAfterStatementClose(PreparedStatementInfo statementInfo, SQLException e) {
        if (e != null) {
            log.error("Close PreparedStatement Error", e);
        }
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Close PreparedStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCloseMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "Got Statement Time: " + formatMillisTime(statementInfo.getAfterTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual Statement: " + statementInfo.getStatement() +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getTimeElapsedToCloseStatementNs()) +
                    System.lineSeparator() +
                    "Statement Alive Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getAliveTimeElapsedNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeResultSetNext(ResultSetInfo resultSetInfo) {
    }

    @Override
    public void onAfterResultSetNext(ResultSetInfo resultSetInfo, long timeElapsedNanos, boolean next, SQLException e) {
    }

    @Override
    public void onAfterResultSetClose(ResultSetInfo resultSetInfo, SQLException e) {
    }

    @Override
    public void onAfterResultSetGet(ResultSetInfo resultSetInfo, String columnLabel, Object value, SQLException e) {
    }

    @Override
    public void onAfterResultSetGet(ResultSetInfo resultSetInfo, int columnIndex, Object value, SQLException e) {
    }

    @Override
    public void onBeforeCallableStatement(CallableStatementInfo statementInfo) {
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Create CallableStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual CallableStatement: N/A";
            log.debug(msg);
        }
    }

    @Override
    public void onAfterCallStatement(CallableStatementInfo statementInfo, SQLException e) {
        if (e != null) {
            log.error("Create CallStatement Error", e);
        }
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Create CallStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual CallStatement: " + statementInfo.getStatement() +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getTimeElapsedToCreateStatementNs());
            log.debug(msg);
        }
    }

    @Override
    public void onBeforeStatementClose(CallableStatementInfo statementInfo) {
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String msg = "On Before Close CallableStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCloseMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "Got Statement Time: " + formatMillisTime(statementInfo.getAfterTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual CallableStatement: " + statementInfo.getStatement();
            log.debug(msg);
        }
    }

    @Override
    public void onAfterStatementClose(CallableStatementInfo statementInfo, SQLException e) {
        if (e != null) {
            log.error("Close CallableStatement Error", e);
        }
        if (log.isDebugEnabled()) {
            ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
            RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
            String nativeSql = routeInfo.getSql();
            if (Objects.nonNull(routeInfo.getSqlAttribute())) {
                nativeSql = routeInfo.getSqlAttribute().getNativeSql();
            }
            String cluster = "N/A";
            if (Objects.nonNull(routeInfo.getCluster())) {
                cluster = routeInfo.getCluster().getName();
            }
            NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
            String node = "N/A";
            if (Objects.nonNull(nodeAttr)) {
                node = "Name[" + nodeAttr.getName() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "] , URL[" + nodeAttr.getUrl() + "]";
            }
            String actualConnection = "N/A";
            Connection physicalConnection = ((ProxyConnection) connectionInfo.getConnection()).getPhysicalConnection();
            if (physicalConnection != null) {
                actualConnection = physicalConnection.toString();
            }
            String errMsg = "N/A";
            if (e != null) {
                errMsg = e.getClass().getName() + ": " + e.getMessage();
            }
            String msg = "On After Close CallableStatement" +
                    System.lineSeparator() +
                    "Time: " + formatMillisTime(statementInfo.getBeforeTimeToCloseMillis()) +
                    System.lineSeparator() +
                    "RoutingId: " + routeInfo.getRouteId() +
                    System.lineSeparator() +
                    "Got Connection Time: " + formatMillisTime(connectionInfo.getAfterTimeToGetConnectionMillis()) +
                    System.lineSeparator() +
                    "Got Statement Time: " + formatMillisTime(statementInfo.getAfterTimeToCreateStatementMillis()) +
                    System.lineSeparator() +
                    "SQL: " + routeInfo.getSql() +
                    System.lineSeparator() +
                    "Native SQL: " + nativeSql +
                    System.lineSeparator() +
                    "TransactionId: " + StringUtils.defaultIfBlank(routeInfo.getTransactionId(), "N/A") +
                    System.lineSeparator() +
                    "TransactionName: " + StringUtils.defaultIfBlank(routeInfo.getTransactionName(), "N/A") +
                    System.lineSeparator() +
                    "Cluster: " + cluster +
                    System.lineSeparator() +
                    "Node: " + node +
                    System.lineSeparator() +
                    "Routing Connection: " + connectionInfo.getConnection().toString() +
                    System.lineSeparator() +
                    "Actual Connection: " + actualConnection +
                    System.lineSeparator() +
                    "Actual CallableStatement: " + statementInfo.getStatement() +
                    System.lineSeparator() +
                    "Exception: " + errMsg +
                    System.lineSeparator() +
                    "Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getTimeElapsedToCloseStatementNs()) +
                    System.lineSeparator() +
                    "Statement Alive Time Elapsed (ms): " + TimeUtils.durationMillis(statementInfo.getAliveTimeElapsedNs());
            log.debug(msg);
        }
    }

    @Override
    public void onAfterCallableStatementSet(CallableStatementInfo callableStatementInfo, String parameterName, Object x, SQLException e) {
    }

    private String formatMillisTime(long millis) {
        return TimeUtils.formatMillisTime(millis, "yyyy-MM-dd HH:mm:ss.SSSSSS");
    }
}
