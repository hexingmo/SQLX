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

import java.sql.CallableStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class CallableStatementInfo extends PreparedStatementInfo {

    private final Map<String, Value> namedParameterValues = new HashMap<>();

    private CallableStatement callableStatement;

    public void setCallableStatement(CallableStatement callableStatement) {
        this.callableStatement = callableStatement;
    }

    @Override
    public CallableStatementInfo getCallableStatementInfo() {
        return this;
    }

    @Override
    public String getSqlWithValues() {

        if (namedParameterValues.isEmpty()) {
            return super.getSqlWithValues();
        }

        /*
          If named parameters were used, it is no longer possible to simply replace the placeholders in the
          original statement with the values of the bind variables.  The only way it could be done is if the names
          could be resolved by to the ordinal positions which is not possible on all databases.

          New log format:  <original statement> name:value, name:value

          Example: {? = call test_proc(?,?)} param1:value1, param2:value2, param3:value3

          In the event that ordinal and named parameters are both used, the original position will be used as the name.
          Example:  {? = call test_proc(?,?)} 1:value1, 3:value3, param2:value2
        */

        final StringBuilder result = new StringBuilder();
        final String statementQuery = getNativeSql();

        // first append the original statement
        result.append(statementQuery);
        result.append(" ");

        StringBuilder parameters = new StringBuilder();

        // add parameters set with ordinal positions
        for (Map.Entry<Integer, Value> entry : getParameterValues().entrySet()) {
            appendParameter(parameters, entry.getKey().toString(), entry.getValue());
        }

        // add named parameters
        for (Map.Entry<String, Value> entry : namedParameterValues.entrySet()) {
            appendParameter(parameters, entry.getKey(), entry.getValue());
        }


        result.append(parameters);

        return result.toString();
    }

    private void appendParameter(StringBuilder parameters, String name, Value value) {
        if (parameters.length() > 0) {
            parameters.append(", ");
        }

        parameters.append(name);
        parameters.append(":");
        parameters.append(value != null ? value.toString() : new Value().toString());
    }

    /**
     * Records the value of a parameter.
     *
     * @param name  the name of the parameter
     * @param value the value of the parameter
     */
    public void setParameterValue(final String name, final Object value) {
        namedParameterValues.put(name, new Value(value));
    }
}
