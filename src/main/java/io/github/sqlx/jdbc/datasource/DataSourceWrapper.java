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

package io.github.sqlx.jdbc.datasource;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * A wrapper class for data sources. This class implements the DataSource interface and wraps another data source,
 * providing additional functionality or attributes.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class DataSourceWrapper implements DataSource {

    /**
     * The name of the data source, used for identification or logging.
     */
    private final String name;

    /**
     * The actual data source being wrapped.
     */
    private final DataSource delegate;

    /**
     * The routing node attributes associated with the data source, used for routing decisions.
     */
    @Getter
    private final NodeAttribute nodeAttribute;

    @Getter
    private final Boolean defaulted;

    /**
     * The future object for monitoring the state of the data source, used to manage asynchronous tasks.
     */
    @Setter
    @Getter
    private Future<?> stateMonitorFuture;

    /**
     * Constructs a DataSourceWrapper instance.
     *
     * @param name             The name of the data source.
     * @param delegate         The actual data source being wrapped.
     * @param attribute        The routing node attributes associated with the data source.
     * @param defaulted        Whether the data source is defaulted or not.
     */
    public DataSourceWrapper(String name , DataSource delegate, NodeAttribute attribute ,Boolean defaulted) {
        this.name = name;
        this.delegate = delegate;
        this.nodeAttribute = attribute;
        this.defaulted = defaulted;
    }

    public String getName() {
        return name;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username , password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    /**
     * Destroys the wrapped data source if necessary, and cancels the state monitoring task.
     */
    public synchronized void destroy() {
        log.info("Starting the destroy process for DataSource: {}", name);
        // If a destroy method is specified in the routing node attributes, invoke it on the delegate.
        String destroyMethod = nodeAttribute.getDestroyMethod();
        if (StringUtils.isNotBlank(destroyMethod)) {
            log.info("Destroy method specified: {}", destroyMethod);
            try {
                log.info("Invoking destroy method on delegate DataSource: {}", delegate.getClass().getName());
                Reflect.on(delegate).call(destroyMethod);
            } catch (Exception e) {
                log.error("Error while invoking destroy method: {}", destroyMethod, e);
            }

            // Cancel the state monitor future if it is not null
            if (stateMonitorFuture != null) {
                log.info("Cancelling state monitor future for DataSource: {}", name);
                stateMonitorFuture.cancel(true);
                log.info("DataSource {} State monitor future cancelled successfully." , name);
            } else {
                log.warn("DataSource {} State monitor future is null, nothing to cancel." , name);
            }
        }
    }
}
