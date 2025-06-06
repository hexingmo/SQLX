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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
public class ResultSetInfo implements Measurable {

    private final Map<String, Value> resultMap = new LinkedHashMap<>();

    private StatementInfo statementInfo;

    private ResultSet resultSet;

    public Map<String, Value> getResultMap() {
        return Collections.unmodifiableMap(resultMap);
    }

    public void setColumnValue(String columnName, Object value) {
        resultMap.put(columnName, new Value(value));
    }

    @Override
    public ConnectionInfo getConnectionInfo() {
        return statementInfo.getConnectionInfo();
    }

    @Override
    public List<RouteInfo> getRouteInfoList() {
        ConnectionInfo connectionInfo = getConnectionInfo();
        if (connectionInfo != null) {
            return connectionInfo.getRouteInfoList();
        }
        return new ArrayList<>();
    }

    @Override
    public String getSql() {
        return statementInfo.getSql();
    }

    @Override
    public String getNativeSql() {
        return statementInfo.getNativeSql();
    }

    @Override
    public String getSqlWithValues() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Value> entry : resultMap.entrySet()) {
          if (sb.length() > 0) {
            sb.append(", ");
          }
          sb.append(entry.getKey());
          sb.append(" = ");
          sb.append(entry.getValue() != null ? entry.getValue().toString() : new Value().toString());
        }

        return sb.toString();
    }

    @Override
    public StatementInfo getStatementInfo() {
        return statementInfo;
    }

    @Override
    public PreparedStatementInfo getPreparedStatementInfo() {
        return (PreparedStatementInfo) statementInfo;
    }

    @Override
    public CallableStatementInfo getCallableStatementInfo() {
        return (CallableStatementInfo) statementInfo;
    }
}
