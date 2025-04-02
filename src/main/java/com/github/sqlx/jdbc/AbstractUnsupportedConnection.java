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

import com.github.sqlx.exception.UnsupportedJdbcMethodException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractUnsupportedConnection extends WrapperAdapter implements Connection {

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method prepareCall(String sql)");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method prepareCall(String sql, int resultSetType, int resultSetConcurrency)");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)");
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return sql;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method abort(Executor executor)");
    }

    @Override
    public final Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method getTypeMap()");
    }

    @Override
    public final void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method setTypeMap(final Map<String, Class<?>> map)");
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method getNetworkTimeout()");
    }

    @Override
    public final void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        throw new UnsupportedJdbcMethodException("Unsupported jdbc method getNetworkTimeout(final Executor executor, final int milliseconds)");
    }


}
