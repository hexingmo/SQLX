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

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.jdbc.ConnectionInfo;
import com.github.sqlx.jdbc.StatementInfo;
import com.github.sqlx.jdbc.transaction.TransactionStatus;
import com.github.sqlx.listener.RouteInfo;
import com.github.sqlx.metrics.*;
import com.github.sqlx.rule.group.RouteGroup;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.SqlType;
import com.github.sqlx.sql.Table;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class MetricsUtils {

    private MetricsUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static RoutingMetrics convertToRoutingMetrics(RouteInfo routeInfo, Exception ex) {

        String nativeSql = routeInfo.getSql();
        if (Objects.nonNull(routeInfo.getSqlAttribute())) {
            nativeSql = routeInfo.getSqlAttribute().getNativeSql();
        }
        String clusterName = "N/A";
        if (Objects.nonNull(routeInfo.getCluster())) {
            clusterName = routeInfo.getCluster().getName();
        }
        List<RoutingGroupTypeName> possibleRoutingGroups = new ArrayList<>();
        for (RouteGroup<?> routingGroup : routeInfo.getRoutingGroups()) {
            List<String> ruleClassNames = routingGroup.getRules().stream().map(r -> r.getClass().getName()).collect(Collectors.toList());
            possibleRoutingGroups.add(new RoutingGroupTypeName()
                    .setGroupClassName(routingGroup.getClass().getName())
                    .setRuleClassNames(ruleClassNames));
        }

        RoutingGroupTypeName hitRoutingGroup = new RoutingGroupTypeName();
        if (routeInfo.getHitRoutingGroup() != null) {
            hitRoutingGroup.setGroupClassName(routeInfo.getHitRoutingGroup().getClass().getName());
            List<String> ruleClassNames = routeInfo.getHitRoutingGroup().getRules().stream().map(r -> r.getClass().getName()).collect(Collectors.toList());
            hitRoutingGroup.setRuleClassNames(ruleClassNames);
        }
        String hitRule = "N/A";
        if (Objects.nonNull(routeInfo.getHitRule())) {
            hitRule = routeInfo.getHitRule().getClass().getName();
        }
        boolean succeeded = ex == null;
        RoutingMetrics metrics = new RoutingMetrics();
        metrics.setRoutingId(routeInfo.getRouteId())
                .setSql(routeInfo.getSql())
                .setNativeSql(nativeSql)
                .setClusterName(clusterName)
                .setPossibleRoutingGroups(possibleRoutingGroups)
                .setHitRoutingGroup(hitRoutingGroup)
                .setHitRule(hitRule)
                .setSqlInfo(convertToSqlInfo(routeInfo.getSqlAttribute()))
                .setHitNodeAttr(convertToRoutingHitNode(routeInfo.getHitNodeAttr()))
                .setIsTransactionActive(routeInfo.getIsTransactionActive())
                .setTransactionId(StringUtils.defaultIfBlank(routeInfo.getTransactionId() , "N/A"))
                .setTransactionName(StringUtils.defaultIfBlank(routeInfo.getTransactionName() , "N/A"))
                .setBeforeTimeMillis(routeInfo.getBeforeTimeMillis())
                .setAfterTimeMillis(routeInfo.getAfterTimeMillis())
                .setTimeElapsedMillis(TimeUtils.durationMillis(routeInfo.getTimeElapsedNanos()))
                .setSucceeded(succeeded)
                .setException(ExceptionUtils.getRootCauseMessage(ex))
                .setCreatedTime(System.currentTimeMillis());
        return metrics;
    }


    public static SqlMetrics convertToSqlMetrics(StatementInfo statementInfo, Exception e) {

        ConnectionInfo connectionInfo = statementInfo.getConnectionInfo();
        RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
        SqlAttribute sqlAttribute = routeInfo.getSqlAttribute();
        String clusterName = "N/A";
        if (Objects.nonNull(routeInfo.getCluster())) {
            clusterName = routeInfo.getCluster().getName();
        }

        SqlMetrics metrics = new SqlMetrics();
        metrics.setStatementId(statementInfo.getStatementId())
                .setSql(statementInfo.getSql())
                .setNativeSql(statementInfo.getNativeSql())
                .setSqlType(sqlAttribute.getSqlType().name())
                .setIsWrite(sqlAttribute.isWrite())
                .setIsRead(sqlAttribute.isRead())
                .setDatabases(sqlAttribute.getDatabases())
                .setTables(sqlAttribute.getSimpleTables())
                .setClusterName(clusterName)
                .setNode(convertToRoutingHitNode(routeInfo.getHitNodeAttr()))
                .setTransactionId(StringUtils.defaultIfBlank(routeInfo.getTransactionId() , "N/A"))
                .setTransactionName(StringUtils.defaultIfBlank(routeInfo.getTransactionName() , "N/A"))
                .setExecuteTimeElapsedMillis(TimeUtils.durationMillis(statementInfo.getTimeElapsedExecuteNs()))
                .setSucceeded(e == null)
                .setException(ExceptionUtils.getRootCauseMessage(e))
                .setUpdateRows(statementInfo.getTotalUpdatedRows())
                .setSelectedRows(statementInfo.getSelectedRows())
                .setCreatedTime(TimeUtils.convertToMillis(LocalDateTime.now()))
                .setUpdatedTime(TimeUtils.convertToMillis(LocalDateTime.now()));
        return metrics;
    }

    public static NodeSqlExecuteNumMetrics convertToNodeSqlExecuteNumMetrics(StatementInfo statementInfo) {

        RouteInfo routeInfo = statementInfo.getRouteInfo();
        NodeAttribute nodeAttr = routeInfo.getHitNodeAttr();
        String nodeName = nodeAttr.getName();
        SqlAttribute sqlAttribute = routeInfo.getSqlAttribute();
        SqlType sqlType = sqlAttribute.getSqlType();
        long beforeTimeToExecuteMillis = statementInfo.getBeforeTimeToExecuteMillis();
        if (beforeTimeToExecuteMillis == 0) {
            return null;
        }
        LocalDateTime dateTime = TimeUtils.convertToLocalDateTime(beforeTimeToExecuteMillis)
                .withSecond(0).withNano(0);
        long timestamp = TimeUtils.convertToMillis(dateTime);
        NodeSqlExecuteNumMetrics metrics = new NodeSqlExecuteNumMetrics();
        metrics.setNodeName(nodeName)
                .setTimestamp(timestamp)
                .setSelectCount(sqlType.is(SqlType.SELECT) ? 1 : 0)
                .setInsertCount(sqlType.is(SqlType.INSERT) ? 1 : 0)
                .setUpdateCount(sqlType.is(SqlType.UPDATE) ? 1 : 0)
                .setDeleteCount(sqlType.is(SqlType.DELETE) ? 1 : 0)
                .setOtherCount(sqlType.is(SqlType.OTHER) ? 1 : 0)
                .setCreatedTime(System.currentTimeMillis());
        return metrics;
    }

    public static TransactionMetrics convertToTransactionMetrics(TransactionStatus transactionStatus , ConnectionInfo connectionInfo, Exception e) {
        RouteInfo routeInfo = connectionInfo.getCurrentRouteInfo();
        String clusterName = "N/A";
        if (Objects.nonNull(routeInfo.getCluster())) {
            clusterName = routeInfo.getCluster().getName();
        }
        TransactionMetrics metrics = new TransactionMetrics();
        Set<String> totalDatabases = new HashSet<>();
        Set<String> totalTables = new HashSet<>();
        List<TransactionalSqlInfo> sqlList = new ArrayList<>();
        for (StatementInfo statementInfo : connectionInfo.getStatementInfoList()) {
            RouteInfo ri = statementInfo.getRouteInfo();
            SqlAttribute sqlAttribute = ri.getSqlAttribute();
            TransactionalSqlInfo transactionalSqlInfo = new TransactionalSqlInfo()
                    .setSql(statementInfo.getSql())
                    .setNativeSql(statementInfo.getNativeSql())
                    .setSqlType(sqlAttribute.getSqlType().name())
                    .setDatabases(sqlAttribute.getDatabases())
                    .setTables(sqlAttribute.getSimpleTables())
                    .setSelectedRows(statementInfo.getSelectedRows())
                    .setUpdateRows(statementInfo.getTotalUpdatedRows())
                    .setExecuteTimeElapsedMillis(TimeUtils.durationMillis(statementInfo.getTimeElapsedExecuteNs()));
            List<Exception> exceptions = statementInfo.getExceptions();
            if (CollectionUtils.isNotEmpty(exceptions)) {
                List<String> exceptionMsgList = new ArrayList<>();
                for (Exception ex : exceptions) {
                    exceptionMsgList.add(ExceptionUtils.getRootCauseMessage(ex));
                }
                transactionalSqlInfo.setExceptions(exceptionMsgList);
            }
            sqlList.add(transactionalSqlInfo);

            totalDatabases.addAll(sqlAttribute.getDatabases());
            totalTables.addAll(sqlAttribute.getSimpleTables());
        }

        long sqlExecuteTimeElapsedMillis = TimeUtils.durationMillis(connectionInfo.getSqlExecuteTimeElapsedNs());
        long timeElapsedMillis = 0;
        if (transactionStatus == TransactionStatus.COMMITTED) {
            timeElapsedMillis = TimeUtils.durationMillis(connectionInfo.getTimeElapsedToCommitNs());
        } else if (transactionStatus == TransactionStatus.ROLLBACKED) {
            timeElapsedMillis = TimeUtils.durationMillis(connectionInfo.getTimeElapsedToRollbackNs());
        }
        long totalTimeElapsedMillis = sqlExecuteTimeElapsedMillis + timeElapsedMillis;
        metrics.setTransactionId(StringUtils.defaultIfBlank(connectionInfo.getTransactionId() , "N/A"))
                .setTransactionName(StringUtils.defaultIfBlank(connectionInfo.getTransactionName() , "N/A"))
                .setTransactionStatus(transactionStatus)
                .setNode(convertToRoutingHitNode(connectionInfo.getCurrentRouteInfo().getHitNodeAttr()))
                .setClusterName(clusterName)
                .setSqlList(sqlList)
                .setSqlExecuteTimeElapsedMillis(TimeUtils.durationMillis(connectionInfo.getSqlExecuteTimeElapsedNs()))
                .setTimeElapsedMillis(timeElapsedMillis)
                .setTotalTimeElapsedMillis(totalTimeElapsedMillis)
                .setUpdateRows(connectionInfo.getTotalUpdatedRows())
                .setSelectedRows(connectionInfo.getTotalSelectedRows())
                .setDatabases(totalDatabases)
                .setTables(totalTables)
                .setSucceeded(e == null)
                .setException(ExceptionUtils.getRootCauseMessage(e))
                .setCreatedTime(TimeUtils.convertToMillis(LocalDateTime.now()))
                .setUpdatedTime(TimeUtils.convertToMillis(LocalDateTime.now()));

        return metrics;
    }

    private static SqlInfo convertToSqlInfo(SqlAttribute sqlAttribute) {
        if (Objects.isNull(sqlAttribute)) {
            return new SqlInfo();
        }

        return new SqlInfo()
                .setSql(sqlAttribute.getSql())
                .setNativeSql(sqlAttribute.getNativeSql())
                .setSqlType(sqlAttribute.getSqlType())
                .setWrite(sqlAttribute.isWrite())
                .setRead(sqlAttribute.isRead())
                .setDatabases(sqlAttribute.getDatabases())
                .setTables(sqlAttribute.getSimpleTables())
                .setFromTables(sqlAttribute.getSimpleFromTables())
                .setInsertTables(sqlAttribute.getSimpleInsertTables())
                .setUpdateTables(sqlAttribute.getSimpleUpdateTables())
                .setDeleteTables(sqlAttribute.getSimpleDeleteTables())
                .setJoinTables(sqlAttribute.getSimpleJoinTables())
                .setSubTables(sqlAttribute.getSimpleSubTables());
    }

    private static NodeInfo convertToRoutingHitNode(NodeAttribute nodeAttr) {
        if (Objects.isNull(nodeAttr)) {
            return new NodeInfo().setUrl("N/A").setName("N/A");
        }
        return new NodeInfo()
                .setUrl(nodeAttr.getUrl())
                .setNodeState(nodeAttr.getNodeState())
                .setName(nodeAttr.getName())
                .setWeight(nodeAttr.getWeight());
    }

    public static List<TableAccessMetrics> convertToTableAccessMetricsList(StatementInfo statementInfo) {
        List<TableAccessMetrics> metricsList = new ArrayList<>();
        SqlAttribute sqlAttribute = statementInfo.getRouteInfo().getSqlAttribute();
        if (CollectionUtils.isNotEmpty(sqlAttribute.getTables())) {
            for (Table table : sqlAttribute.getTables()) {
                long queryCount = sqlAttribute.getReadTables().contains(table) ? 1L : 0L;
                long insertCount = sqlAttribute.getInsertTables().contains(table) ? 1L : 0L;
                long updateCount = sqlAttribute.getUpdateTables().contains(table) ? 1L : 0L;
                long deleteCount = sqlAttribute.getDeleteTables().contains(table) ? 1L : 0L;
                long writeCount = insertCount + updateCount + deleteCount;
                long totalCounts = queryCount + writeCount;
                TableAccessMetrics metrics = new TableAccessMetrics()
                        .setFullTableName(table.getFullTableName())
                        .setDatabase(table.getDatabase())
                        .setTable(table.getTable())
                        .setQueryCount(queryCount)
                        .setInsertCount(insertCount)
                        .setUpdateCount(updateCount)
                        .setDeleteCount(deleteCount)
                        .setReadCount(queryCount)
                        .setWriteCount(writeCount)
                        .setTotalCount(totalCounts);
                metricsList.add(metrics);
            }
        }
        return metricsList;
    }
}
