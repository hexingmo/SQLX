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
package com.github.sqlx.jdbc;

import com.github.sqlx.rule.RouteInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
public class PreparedStatementInfo extends StatementInfo {

    private final Map<Integer, Value> parameterValues = new HashMap<>();

    private ConnectionInfo connectionInfo;

    @Override
    public List<RouteInfo> getRouteInfoList() {
        if (connectionInfo != null) {
            return connectionInfo.getRouteInfoList();
        }
        return new ArrayList<>();
    }

    @Override
    public StatementInfo getStatementInfo() {
        return this;
    }

    @Override
    public PreparedStatementInfo getPreparedStatementInfo() {
        return this;
    }

    @Override
    public CallableStatementInfo getCallableStatementInfo() {
        throw new UnsupportedOperationException("PreparedStatementInfo Unsupported getCallableStatementInfo method");
    }

    public String getSqlWithValues() {
        final StringBuilder sb = new StringBuilder();
        final String statementQuery = getNativeSql();

        // iterate over the characters in the query replacing the parameter placeholders
        // with the actual values
        int currentParameter = 0;
        for( int pos = 0; pos < statementQuery.length(); pos ++) {
            char character = statementQuery.charAt(pos);
            if( statementQuery.charAt(pos) == '?' && currentParameter <= parameterValues.size()) {
                // replace with parameter value
                Value value = parameterValues.get(currentParameter);
                sb.append(value != null ? value.toString() : new Value().toString());
                currentParameter++;
            } else {
                sb.append(character);
            }
        }

        return sb.toString();
    }

    /**
     * Records the value of a parameter.
     * @param position the position of the parameter (starts with 1 not 0)
     * @param value the value of the parameter
     */
    public void setParameterValue(final int position, final Object value) {
        parameterValues.put(position - 1, new Value(value));
    }

    protected Map<Integer, Value> getParameterValues() {
        return parameterValues;
    }


}
