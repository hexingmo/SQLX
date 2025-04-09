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



import com.github.sqlx.jdbc.datasource.SqlXDataSource;
import com.github.sqlx.listener.EventListener;
import lombok.Getter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.stream.IntStream;

/**
 * @author He Xing Mo
 * @since 1.0
 */

public class ProxyPreparedStatement extends WrapperAdapter implements PreparedStatement {

    @Getter
    private final SqlXDataSource sqlXDataSource;

    @Getter
    private final PreparedStatementInfo preparedStatementInfo;

    @Getter
    private final PreparedStatement delegate;

    protected final EventListener eventListener;

    private final ProxyStatement proxyStatement;

    public ProxyPreparedStatement(SqlXDataSource sqlXDataSource, PreparedStatementInfo preparedStatementInfo, EventListener eventListener) {
        this.sqlXDataSource = sqlXDataSource;
        this.preparedStatementInfo = preparedStatementInfo;
        this.delegate = (PreparedStatement) preparedStatementInfo.getStatement();
        this.eventListener = eventListener;
        this.proxyStatement = new ProxyStatement(sqlXDataSource, eventListener);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        SQLException e = null;
        try {
            preparedStatementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            preparedStatementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            eventListener.onBeforeExecuteQuery(preparedStatementInfo);
            ResultSet resultSet = delegate.executeQuery();
            ResultSetInfo resultSetInfo = new ResultSetInfo();
            resultSetInfo.setResultSet(resultSet);
            resultSetInfo.setStatementInfo(preparedStatementInfo);
            return new ResultSetWrapper(resultSet , resultSetInfo , eventListener);
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            preparedStatementInfo.setAfterTimeToExecuteNs(System.nanoTime());
            preparedStatementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
            preparedStatementInfo.addException(e);
            eventListener.onAfterExecuteQuery(preparedStatementInfo , e);
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        SQLException e = null;
        int rows = 0;
        try {
            preparedStatementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            preparedStatementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            eventListener.onBeforeExecuteUpdate(preparedStatementInfo);
            rows = delegate.executeUpdate();
            return rows;
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            preparedStatementInfo.setAfterTimeToExecuteNs(System.nanoTime());
            preparedStatementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
            preparedStatementInfo.setUpdatedRows(rows);
            preparedStatementInfo.addException(e);
            eventListener.onAfterExecuteUpdate(preparedStatementInfo , e);
        }
    }

    @Override
    public long executeLargeUpdate() throws SQLException {

        SQLException e = null;
        try {
            preparedStatementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            preparedStatementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            eventListener.onBeforeExecuteUpdate(preparedStatementInfo);
            return delegate.executeLargeUpdate();
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            preparedStatementInfo.setAfterTimeToExecuteNs(System.nanoTime());
            preparedStatementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
            preparedStatementInfo.addException(e);
            eventListener.onAfterExecuteUpdate(preparedStatementInfo , e);
        }
    }

    @Override
    public boolean execute() throws SQLException {
        SQLException e = null;
        try {
            preparedStatementInfo.setBeforeTimeToExecuteMillis(System.currentTimeMillis());
            preparedStatementInfo.setBeforeTimeToExecuteNs(System.nanoTime());
            eventListener.onBeforeExecute(preparedStatementInfo);
            return delegate.execute();
        } catch (SQLException ex) {
            e = ex;
            throw ex;
        } finally {
            preparedStatementInfo.setAfterTimeToExecuteNs(System.nanoTime());
            preparedStatementInfo.setAfterTimeToExecuteMillis(System.currentTimeMillis());
            preparedStatementInfo.addException(e);
            if (eventListener != null) {
                eventListener.onAfterExecute(preparedStatementInfo , e);
            }
        }
    }

    @Override
    public void addBatch() throws SQLException {
        SQLException e = null;
        long start = System.nanoTime();
        try {
            if (eventListener != null) {
                eventListener.onBeforeAddBatch(preparedStatementInfo);
            }
            delegate.addBatch();
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            if (eventListener != null) {
                eventListener.onAfterAddBatch(preparedStatementInfo, System.nanoTime() - start, e);
            }
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        SQLException e = null;
        long start = System.nanoTime();
        try {
            if (eventListener != null) {
                eventListener.onBeforeClearBatch(preparedStatementInfo);
            }
            delegate.clearBatch();
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            if (eventListener != null) {
                eventListener.onAfterClearBatch(preparedStatementInfo, System.nanoTime() - start, e);
            }
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        SQLException e = null;
        long start = System.nanoTime();
        int[] counts = null;
        try {
            if (eventListener != null) {
                eventListener.onBeforeExecuteBatch(preparedStatementInfo , start);
            }
            counts = delegate.executeBatch();
            return counts;
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            if (eventListener != null) {
                long afterTimeNs = System.nanoTime();
                preparedStatementInfo.addException(e);
                long[] array = null;
                if (counts != null) {
                    array = IntStream.of(counts).mapToLong(i -> (long) i).toArray();
                }
                eventListener.onAfterExecuteBatch(preparedStatementInfo, afterTimeNs , afterTimeNs - start, array , e);
            }
        }
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {

        SQLException e = null;
        long start = System.nanoTime();
        long[] counts = null;
        try {
            eventListener.onBeforeExecuteBatch(preparedStatementInfo , start);
            counts = delegate.executeLargeBatch();
            return counts;
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            long afterTimeNs = System.nanoTime();
            preparedStatementInfo.addException(e);
            eventListener.onAfterExecuteBatch(preparedStatementInfo, afterTimeNs , afterTimeNs - start, counts , e);
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNull(parameterIndex, sqlType);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, null, e);
        }
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBoolean(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setByte(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setShort(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setInt(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setLong(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setFloat(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setDouble(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBigDecimal(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setString(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBytes(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setDate(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setTime(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setTimestamp(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setUnicodeStream(parameterIndex, x, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void clearParameters() throws SQLException {
        delegate.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        SQLException e = null;
        try {
            delegate.setObject(parameterIndex, x, targetSqlType);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setObject(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, reader, e);
        }
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setRef(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBlob(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setClob(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setArray(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        SQLException e = null;
        try {
            delegate.setDate(parameterIndex, x, cal);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        SQLException e = null;
        try {
            delegate.setTime(parameterIndex, x, cal);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        SQLException e = null;
        try {
            delegate.setTimestamp(parameterIndex, x, cal);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNull(parameterIndex, sqlType, typeName);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, null, e);
        }
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setURL(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return delegate.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setRowId(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNString(parameterIndex, value);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, value, e);
        }
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNCharacterStream(parameterIndex, value, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, value, e);
        }
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNClob(parameterIndex, value);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, value, e);
        }
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setClob(parameterIndex, reader, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, reader, e);
        }
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBlob(parameterIndex, inputStream, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, inputStream, e);
        }
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNClob(parameterIndex, reader, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, reader, e);
        }
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        SQLException e = null;
        try {
            delegate.setSQLXML(parameterIndex, xmlObject);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, xmlObject, e);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        SQLException e = null;
        try {
            delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        SQLException e = null;
        try {
            delegate.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, reader, e);
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setAsciiStream(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBinaryStream(parameterIndex, x);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        SQLException e = null;
        try {
            delegate.setCharacterStream(parameterIndex, reader);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, reader, e);
        }
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNCharacterStream(parameterIndex, value);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, value, e);
        }
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        SQLException e = null;
        try {
            delegate.setClob(parameterIndex, reader);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, reader, e);
        }
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        SQLException e = null;
        try {
            delegate.setBlob(parameterIndex, inputStream);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, inputStream, e);
        }
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        SQLException e = null;
        try {
            delegate.setNClob(parameterIndex, reader);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, reader, e);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        SQLException e = null;
        try {
            delegate.setObject(parameterIndex , x , targetSqlType , scaleOrLength);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        SQLException e = null;
        try {
            delegate.setObject(parameterIndex , x , targetSqlType);
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            eventListener.onAfterPreparedStatementSet(preparedStatementInfo, parameterIndex, x, e);
        }
    }

    @Override
    public void close() throws SQLException {
        SQLException e = null;
        try {
            preparedStatementInfo.setBeforeTimeToCloseNs(System.nanoTime());
            preparedStatementInfo.setBeforeTimeToCloseMillis(System.currentTimeMillis());
            eventListener.onBeforeStatementClose(preparedStatementInfo);
            delegate.close();
        } catch (SQLException sqle) {
            e = sqle;
            throw e;
        } finally {
            preparedStatementInfo.addException(e);
            preparedStatementInfo.setAfterTimeToCloseNs(System.nanoTime());
            preparedStatementInfo.setAfterTimeToCloseMillis(System.currentTimeMillis());
            eventListener.onAfterStatementClose(preparedStatementInfo, e);
            proxyStatement.close();
        }
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        delegate.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }


    @Override
    public ResultSet getResultSet() throws SQLException {
        ResultSetInfo resultSetInfo = new ResultSetInfo();
        resultSetInfo.setResultSet(delegate.getResultSet());
        resultSetInfo.setStatementInfo(preparedStatementInfo);
        return new ResultSetWrapper(delegate.getResultSet() , resultSetInfo , eventListener);
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
    }


    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        delegate.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return delegate.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return delegate.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        delegate.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return delegate.getLargeMaxRows();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return proxyStatement.execute(sql);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return proxyStatement.executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return proxyStatement.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return proxyStatement.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return proxyStatement.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return proxyStatement.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return proxyStatement.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return proxyStatement.execute(sql, columnNames);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return proxyStatement.executeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return proxyStatement.executeLargeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return proxyStatement.executeLargeUpdate(sql , autoGeneratedKeys);
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        proxyStatement.addBatch(sql);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}


