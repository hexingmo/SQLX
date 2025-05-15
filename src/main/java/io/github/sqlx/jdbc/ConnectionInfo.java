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
package io.github.sqlx.jdbc;

import io.github.sqlx.rule.RouteInfo;
import lombok.Data;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
public class ConnectionInfo implements Measurable {

    private String transactionId;

    private String transactionName;

    private RouteInfo currentRouteInfo;

    private List<RouteInfo> routeInfoList;

    private LinkedList<StatementInfo> statementInfoList = new LinkedList<>();

    private DataSource dataSource;

    private Connection connection;

    private String url;

    private long beforeTimeToGetConnectionNs;

    private long afterTimeToGetConnectionNs;

    private long beforeTimeToGetConnectionMillis;

    private long afterTimeToGetConnectionMillis;

    private long beforeTimeToRollbackNs;

    private long afterTimeToRollbackNs;

    private long beforeTimeToRollbackMillis;

    private long afterTimeToRollbackMillis;

    private long beforeTimeToCommitNs;

    private long afterTimeToCommitNs;

    private long beforeTimeToCommitMillis;

    private long afterTimeToCommitMillis;

    private long beforeTimeToCloseConnectionNs;

    private long afterTimeToCloseConnectionNs;

    private long beforeTimeToCloseConnectionMillis;

    private long afterTimeToCloseConnectionMillis;


    public void addStatementInfo(StatementInfo statementInfo) {
        statementInfoList.addLast(statementInfo);
    }

    public long getTimeElapsedToGetConnectionNs() {
        return afterTimeToGetConnectionNs - beforeTimeToGetConnectionNs;
    }

    public long getTimeElapsedToCloseConnectionNs() {
        return afterTimeToCloseConnectionNs - beforeTimeToCloseConnectionNs;
    }

    public long getTimeElapsedToRollbackNs() {
        return afterTimeToRollbackNs - beforeTimeToRollbackNs;
    }

    public long getTimeElapsedToCommitNs() {
        return afterTimeToCommitNs - beforeTimeToCommitNs;
    }

    public long getAliveTimeElapsedNs() {
        return afterTimeToCloseConnectionNs - afterTimeToGetConnectionNs;
    }

    public long getSqlExecuteTimeElapsedNs() {
        return statementInfoList.stream().mapToLong(StatementInfo::getTimeElapsedExecuteNs).sum();
    }

    public long getTotalSelectedRows() {
        return statementInfoList.stream().mapToLong(StatementInfo::getSelectedRows).sum();
    }

    public long getTotalUpdatedRows() {
        return statementInfoList.stream().mapToLong(StatementInfo::getUpdatedRows).sum();
    }

    @Override
    public String getSql() {
        return "";
    }

    @Override
    public String getNativeSql() {
        return "";
    }

    @Override
    public String getSqlWithValues() {
        return "";
    }

    @Override
    public ConnectionInfo getConnectionInfo() {
        return this;
    }

    @Override
    public StatementInfo getStatementInfo() {
        throw new UnsupportedOperationException("ConnectionInfo Unsupported getStatementInfo method");
    }

    @Override
    public PreparedStatementInfo getPreparedStatementInfo() {
        throw new UnsupportedOperationException("ConnectionInfo Unsupported getPreparedStatementInfo method");
    }

    @Override
    public CallableStatementInfo getCallableStatementInfo() {
        throw new UnsupportedOperationException("ConnectionInfo Unsupported getCallableStatementInfo method");
    }

    public long getBeforeTimeToGetConnectionMillis() {
        return beforeTimeToGetConnectionMillis;
    }

}
