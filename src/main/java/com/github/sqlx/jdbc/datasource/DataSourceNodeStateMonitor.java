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
package com.github.sqlx.jdbc.datasource;

import com.github.sqlx.NodeState;
import com.github.sqlx.NodeAttribute;
import com.github.sqlx.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class DataSourceNodeStateMonitor {

    private static final ScheduledThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(1 , new BasicThreadFactory.Builder()
            .namingPattern("DataSource-NodeState-Monitoring-%d")
            .daemon(true)
            .priority(Thread.MAX_PRIORITY)
            .build());

    public void monitor(DataSourceWrapper dataSourceWrapper) {

        if (dataSourceWrapper.getStateMonitorFuture() != null && !dataSourceWrapper.getStateMonitorFuture().isCancelled()) {
            log.warn("DataSource {} has already started monitoring tasks. No need to start again" , dataSourceWrapper.getName());
            return;
        }
        String heartbeatSql = dataSourceWrapper.getNodeAttribute().getHeartbeatSql();
        if (StringUtils.isBlank(heartbeatSql)) {
            log.warn("DataSource {} is not configured with 'heartbeatSql' and cannot be monitored" , dataSourceWrapper.getName());
            return;
        }
        long heartbeatInterval = dataSourceWrapper.getNodeAttribute().getHeartbeatInterval();
        ScheduledFuture<?> future = THREAD_POOL.scheduleAtFixedRate(new DataSourceNodeStateMonitorTask(dataSourceWrapper), 0, heartbeatInterval, TimeUnit.MILLISECONDS);
        dataSourceWrapper.setStateMonitorFuture(future);
        log.info("DataSource {} has started monitoring , heartbeatSql [{}] , interval [{}] ms", dataSourceWrapper.getName() , heartbeatSql , heartbeatInterval);
    }

    @AllArgsConstructor
    private static class DataSourceNodeStateMonitorTask implements Runnable {

        private final DataSourceWrapper dataSourceWrapper;

        @Override
        public void run() {
            String heartbeatSql = dataSourceWrapper.getNodeAttribute().getHeartbeatSql();
            Connection connection = null;
            PreparedStatement ps = null;
            try {
                connection = dataSourceWrapper.getConnection();
                DatabaseMetaData metaData = connection.getMetaData();
                String url = metaData.getURL();
                if (log.isDebugEnabled()) {
                    log.debug("{} datasource execute heartbeat Sql [{}] URL [{}]" , dataSourceWrapper.getNodeAttribute().getName() , heartbeatSql , url);
                }
                ps = connection.prepareStatement(heartbeatSql);
                ps.execute();
                NodeAttribute rna = dataSourceWrapper.getNodeAttribute();
                if (rna.getNodeState() == NodeState.DOWN || rna.getNodeState() == NodeState.UNKNOWN) {
                    dataSourceWrapper.getNodeAttribute().setNodeState(NodeState.UP);
                }
            } catch (Exception e) {
                dataSourceWrapper.getNodeAttribute().setNodeState(NodeState.DOWN);
                log.error("DataSource {} Health Monitor Error" , dataSourceWrapper.getName() , e);
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        log.error("DataSource Monitor PreparedStatement Close Error" , e);
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        log.error("DataSource Monitor Connection Close Error" , e);
                    }
                }
            }
        }
    }
}
