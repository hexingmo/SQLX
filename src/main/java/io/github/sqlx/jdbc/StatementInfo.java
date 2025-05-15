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
import io.github.sqlx.util.UUIDUtils;
import lombok.Data;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
public class StatementInfo implements Measurable {

    private final String statementId;

    private RouteInfo routeInfo;

    private ConnectionInfo connectionInfo;

    private String sql;

    private String nativeSql;

    private Statement statement;

    private long beforeTimeToCreateStatementNs;

    private long afterTimeToCreateStatementNs;

    private long beforeTimeToCreateStatementMillis;

    private long afterTimeToCreateStatementMillis;

    private long beforeTimeToCloseNs;

    private long afterTimeToCloseNs;

    private long beforeTimeToCloseMillis;

    private long afterTimeToCloseMillis;

    private long beforeTimeToExecuteNs;

    private long afterTimeToExecuteNs;

    private long beforeTimeToExecuteMillis;

    private long afterTimeToExecuteMillis;

    private long totalTimeElapsed;

    private long updatedRows;

    private long totalUpdatedRows;

    private long selectedRows;

    private List<Exception> exceptions = new LinkedList<>();

    public void addException(Exception ex) {
        if (Objects.nonNull(ex)) {
            exceptions.add(ex);
        }
    }

    public StatementInfo() {
        this.statementId = UUIDUtils.getSimpleUUID();
    }

    public void incrementTimeElapsed(long timeElapsedNanos) {
        totalTimeElapsed += timeElapsedNanos;
    }

    public void incrementUpdatedRows(long updatedRows) {
        this.totalUpdatedRows += updatedRows;
    }

    public long getTimeElapsedToCreateStatementNs() {
        return afterTimeToCreateStatementNs - beforeTimeToCreateStatementNs;
    }

    public long getAliveTimeElapsedNs() {
        return afterTimeToCloseNs - afterTimeToCreateStatementNs;
    }

    public long getTimeElapsedExecuteNs() {
        return afterTimeToExecuteNs - beforeTimeToExecuteNs;
    }

    public long getTimeElapsedToCloseStatementNs() {
        return afterTimeToCloseNs - beforeTimeToCloseNs;
    }

    @Override
    public List<RouteInfo> getRouteInfoList() {
        if (connectionInfo != null) {
            return connectionInfo.getRouteInfoList();
        }
        return new ArrayList<>();
    }

    @Override
    public String getSqlWithValues() {
        return getSql();
    }

    @Override
    public StatementInfo getStatementInfo() {
        return this;
    }

    @Override
    public PreparedStatementInfo getPreparedStatementInfo() {
        throw new UnsupportedOperationException("StatementInfo Unsupported getPreparedStatementInfo method");
    }

    @Override
    public CallableStatementInfo getCallableStatementInfo() {
        throw new UnsupportedOperationException("StatementInfo Unsupported getCallableStatementInfo method");
    }


}
