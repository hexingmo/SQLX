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

import com.github.sqlx.config.MetricsCollectScope;
import com.github.sqlx.config.MetricsConfiguration;
import com.github.sqlx.jdbc.ConnectionInfo;
import com.github.sqlx.jdbc.PreparedStatementInfo;
import com.github.sqlx.jdbc.ResultSetInfo;
import com.github.sqlx.jdbc.StatementInfo;
import com.github.sqlx.jdbc.transaction.TransactionStatus;
import com.github.sqlx.metrics.*;
import com.github.sqlx.rule.RouteInfo;
import com.github.sqlx.util.CollectionUtils;
import com.github.sqlx.util.JsonUtils;
import com.github.sqlx.util.MetricsUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class MetricsCollectEventListener implements EventListener {

    private final MetricsCollector metricsCollector;

    private final MetricsConfiguration metricsConfiguration;

    public MetricsCollectEventListener(MetricsConfiguration metricsConfiguration , MetricsCollector metricsCollector) {
        this.metricsConfiguration = metricsConfiguration;
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void onAfterRouting(RouteInfo routeInfo, Exception e) {
        if (Boolean.TRUE.equals(metricsConfiguration.getEnableRoutingMetrics())) {
            if (Objects.equals(metricsConfiguration.getCollectScope() , MetricsCollectScope.ALL)) {
                collectRoutingMetrics(routeInfo , e);
            } else if (Objects.nonNull(e)) {
                collectRoutingMetrics(routeInfo , e);
            }
        }
    }

    @Override
    public void onAfterCreateStatement(StatementInfo statementInfo, SQLException e) {
        collectSqlMetrics(statementInfo , e);
    }

    @Override
    public void onAfterPrepareStatement(PreparedStatementInfo statementInfo, Exception e) {
        collectSqlMetrics(statementInfo , e);
    }

    @Override
    public void onAfterExecuteQuery(PreparedStatementInfo statementInfo, SQLException e) {
        collectSqlMetrics(statementInfo , e);
        doCollectNodeSqlExecuteNumMetrics(statementInfo);
        collectTableAccessMetrics(statementInfo);
    }

    @Override
    public void onAfterExecuteQuery(StatementInfo statementInfo, SQLException e) {
        collectSqlMetrics(statementInfo , e);
        doCollectNodeSqlExecuteNumMetrics(statementInfo);
        collectTableAccessMetrics(statementInfo);
    }

    @Override
    public void onAfterExecute(StatementInfo statementInfo, SQLException e) {
        collectSqlMetrics(statementInfo , e);
        doCollectNodeSqlExecuteNumMetrics(statementInfo);
        collectTableAccessMetrics(statementInfo);
    }

    @Override
    public void onAfterExecute(PreparedStatementInfo statementInfo, SQLException e) {
        collectSqlMetrics(statementInfo , e);
        doCollectNodeSqlExecuteNumMetrics(statementInfo);
        collectTableAccessMetrics(statementInfo);
    }

    @Override
    public void onAfterExecuteUpdate(StatementInfo statementInfo, SQLException e) {
        collectSqlMetrics(statementInfo , e);
        doCollectNodeSqlExecuteNumMetrics(statementInfo);
        collectTableAccessMetrics(statementInfo);
    }

    @Override
    public void onAfterExecuteUpdate(PreparedStatementInfo statementInfo, SQLException e) {
        collectSqlMetrics(statementInfo , e);
        doCollectNodeSqlExecuteNumMetrics(statementInfo);
        collectTableAccessMetrics(statementInfo);
    }

    @Override
    public void onAfterResultSetClose(ResultSetInfo resultSetInfo, SQLException e) {
        collectSqlMetrics(resultSetInfo.getStatementInfo() , e);
    }

    @Override
    public void onAfterExecuteBatch(PreparedStatementInfo statementInfo, long afterTimeNs, long timeElapsedNanos, long[] counts, SQLException e) {
        // TODO 可能会有问题？
        collectSqlMetrics(statementInfo , e);
        doCollectNodeSqlExecuteNumMetrics(statementInfo);
        collectTableAccessMetrics(statementInfo);
    }

    @Override
    public void onBeforeCommit(ConnectionInfo connectionInfo) {
        collectTransactionMetrics(TransactionStatus.COMMITTING ,connectionInfo, null);
    }

    @Override
    public void onAfterCommit(ConnectionInfo connectionInfo, SQLException e) {
        collectTransactionMetrics(TransactionStatus.COMMITTED ,connectionInfo, e);
    }

    @Override
    public void onBeforeRollback(ConnectionInfo connectionInfo) {
        collectTransactionMetrics(TransactionStatus.ROLLBACKING ,connectionInfo, null);
    }

    @Override
    public void onAfterRollback(ConnectionInfo connectionInfo, SQLException e) {
        collectTransactionMetrics(TransactionStatus.ROLLBACKED ,connectionInfo, e);
    }

    @Override
    public void onBeforeSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint) {
        collectTransactionMetrics(TransactionStatus.ROLLBACKING ,connectionInfo, null);
    }

    @Override
    public void onAfterSavepointRollback(ConnectionInfo connectionInfo, Savepoint savepoint, SQLException e) {
        collectTransactionMetrics(TransactionStatus.ROLLBACKED ,connectionInfo, e);
    }

    private void collectRoutingMetrics(RouteInfo routeInfo , Exception e) {
        RoutingMetrics metrics = MetricsUtils.convertToRoutingMetrics(routeInfo, e);
        if (log.isDebugEnabled()) {
            log.debug("Collect Routing Metrics: {}", JsonUtils.toJson(metrics));
        }
        try {
            metricsCollector.collect(metrics);
        } catch (Exception ex) {
            log.error("Collect Routing Metrics Error" , ex);
        }
    }

    private void collectTransactionMetrics(TransactionStatus transactionStatus, ConnectionInfo connectionInfo, SQLException e) {
        if (Boolean.TRUE.equals(metricsConfiguration.getEnableTransactionMetrics())) {
            MetricsCollectScope collectScope = metricsConfiguration.getCollectScope();
            Long slowTransactionMillis = metricsConfiguration.getSlowTransactionMillis();
            if (Objects.equals(metricsConfiguration.getCollectScope() , MetricsCollectScope.ALL)) {
                TransactionMetrics metrics = MetricsUtils.convertToTransactionMetrics(transactionStatus ,connectionInfo, e);
                doCollectTransactionMetrics(metrics);
                return;
            }
            TransactionMetrics metrics = MetricsUtils.convertToTransactionMetrics(transactionStatus ,connectionInfo, e);
            long totalTimeElapsedMillis = metrics.getTotalTimeElapsedMillis();
            if (Objects.equals(collectScope , MetricsCollectScope.SLOW) && totalTimeElapsedMillis > slowTransactionMillis) {
                doCollectTransactionMetrics(metrics);
                return;
            }
            if (Objects.nonNull(e)) {
                doCollectTransactionMetrics(metrics);
            }

        }
   }

   private void doCollectTransactionMetrics(TransactionMetrics metrics) {
       if (log.isDebugEnabled()) {
           log.debug("Collect Transaction Metrics: {}", JsonUtils.toJson(metrics));
       }
       try {
           metricsCollector.collect(metrics);
       } catch (Exception ex) {
           log.error("Collect Transaction Metrics Error" , ex);
       }
   }

    private void collectSqlMetrics(StatementInfo statementInfo, Exception e) {
        RouteInfo routeInfo = statementInfo.getRouteInfo();
        if (Objects.isNull(routeInfo)) {
            if (log.isDebugEnabled()) {
                log.debug("RouteInfo is null, skip collect sql metrics");
            }
            return;
        }
        if (Boolean.TRUE.equals(metricsConfiguration.getEnableSqlMetrics())) {
            MetricsCollectScope collectScope = metricsConfiguration.getCollectScope();
            Long slowSqlMillis = metricsConfiguration.getSlowSqlMillis();
            if (Objects.equals(metricsConfiguration.getCollectScope() , MetricsCollectScope.ALL)) {
                SqlMetrics metrics = MetricsUtils.convertToSqlMetrics(statementInfo, e);
                doCollectSqlMetrics(metrics);
                return;
            }
            SqlMetrics metrics = MetricsUtils.convertToSqlMetrics(statementInfo, e);
            long executeTimeElapsedMillis = metrics.getExecuteTimeElapsedMillis();
            if (Objects.equals(collectScope , MetricsCollectScope.SLOW) && executeTimeElapsedMillis > slowSqlMillis) {
                doCollectSqlMetrics(metrics);
                return;
            }
            if (Objects.nonNull(e)) {
                doCollectSqlMetrics(metrics);
            }
        }
    }

    private void doCollectSqlMetrics(SqlMetrics metrics) {
        if (log.isDebugEnabled()) {
            log.debug("Collect SQL Metrics: {}", JsonUtils.toJson(metrics));
        }
        try {
            metricsCollector.collect(metrics);
        } catch (Exception ex) {
            log.error("Collect SQL Metrics Error" , ex);
        }
    }

    private void doCollectNodeSqlExecuteNumMetrics(StatementInfo statementInfo) {
        NodeSqlExecuteNumMetrics metrics = MetricsUtils.convertToNodeSqlExecuteNumMetrics(statementInfo);
        try {
            metricsCollector.collect(metrics);
        } catch (Exception ex) {
            log.error("Collect NodeSqlExecuteNum Metrics Error" , ex);
        }
    }

    private void collectTableAccessMetrics(StatementInfo statementInfo) {
        List<TableAccessMetrics> metricsList = MetricsUtils.convertToTableAccessMetricsList(statementInfo);
        if (CollectionUtils.isEmpty(metricsList)) {
            return;
        }
        for (TableAccessMetrics metrics : metricsList) {
            if (log.isDebugEnabled()) {
                log.debug("Collect TableAccess Metrics: {}", JsonUtils.toJson(metrics));
            }
            try {
                metricsCollector.collect(metrics);
            } catch (Exception ex) {
                log.error("Collect TableAccess Metrics Error" , ex);
            }
        }
    }

}
