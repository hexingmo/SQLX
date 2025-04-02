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

import com.github.sqlx.exception.NoSuchDataSourceException;
import com.github.sqlx.jdbc.ProxyConnection;
import com.github.sqlx.jdbc.WrapperAdapter;
import com.github.sqlx.listener.EventListener;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractSqlXDataSource extends WrapperAdapter implements SqlXDataSource {

    protected final DatasourceManager datasourceManager;

    protected final EventListener eventListener;

    private PrintWriter logWriter = new PrintWriter(System.out);

    private int loginTimeout = 0;

    protected AbstractSqlXDataSource(DatasourceManager datasourceManager , EventListener eventListener) {
        this.datasourceManager = datasourceManager;
        this.eventListener = eventListener;
    }

    protected DataSource getDataSourceWithName(String name) {
        DataSource dataSource = datasourceManager.getDataSource(name);
        if (Objects.isNull(dataSource)) {
            throw new NoSuchDataSourceException(String.format("No DataSource with name [%s] found" , name));
        }
        return dataSource;
    }

    protected List<DataSource> getDataSourceWithName(List<String> names) {
        List<DataSource> dataSources = new ArrayList<>();
        for (String name : names) {
            dataSources.add(datasourceManager.getDataSource(name));
        }
        return dataSources;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ProxyConnection(this , eventListener);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new ProxyConnection(this , eventListener);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
}
