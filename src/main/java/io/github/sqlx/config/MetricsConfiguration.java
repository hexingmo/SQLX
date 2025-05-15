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

package io.github.sqlx.config;

import io.github.sqlx.exception.ConfigurationException;
import io.github.sqlx.metrics.MetricsCollectMode;
import io.github.sqlx.util.StringUtils;
import com.google.gson.annotations.Expose;
import lombok.Data;

import java.time.Duration;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Data
public class MetricsConfiguration implements ConfigurationValidator {

    @Expose
    private Boolean enabled;

    @Expose
    private String username;

    @Expose
    private String password;

    @Expose
    private Long slowSqlMillis;

    @Expose
    private Long slowTransactionMillis;

    @Expose
    private Boolean enableRoutingMetrics = true;

    @Expose
    private Boolean enableSqlMetrics = true;

    @Expose
    private Boolean enableTransactionMetrics = true;

    @Expose
    private MetricsCollectScope collectScope = MetricsCollectScope.SLOW;

    @Expose
    private MetricsCollectMode collectMode = MetricsCollectMode.SYNC;

    @Expose
    private String fileDirectory;

    @Expose
    private Duration dataRetentionDuration = Duration.ofDays(3);

    @Expose
    private Integer collectCorePoolSize;

    @Expose
    private Integer collectMaxPoolSize;

    @Expose
    private Long collectKeepAliveMillis;

    @Expose
    private Integer collectQueueCapacity;

    @Override
    public void validate() {
        if (StringUtils.isBlank(username)) {
            throw new ConfigurationException("metrics [username] attr must not be empty");
        }
        if (StringUtils.isBlank(password)) {
            throw new ConfigurationException("metrics [password] attr must not be empty");
        }
        if (StringUtils.isBlank(fileDirectory)) {
            throw new ConfigurationException("metrics [fileDirectory] attr must not be empty");
        }
        if (Objects.nonNull(collectScope) && Objects.equals(collectScope , MetricsCollectScope.SLOW)) {
            if (Objects.isNull(slowSqlMillis)) {
                throw new ConfigurationException("when collectScope is SLOW metrics [slowSqlMillis] attr must not be empty");
            }
            if (Objects.isNull(slowTransactionMillis)) {
                throw new ConfigurationException("when collectScope is SLOW metrics [slowTransactionMillis] attr must not be empty");
            }
        }
        if (Objects.isNull(collectMode)) {
            throw new ConfigurationException("metrics [collectMode] attr must not be empty");
        }
        if (Objects.equals(collectMode , MetricsCollectMode.ASYNC)) {
            if (Objects.isNull(collectCorePoolSize)) {
                throw new ConfigurationException("when collectMode is ASYNC metrics [collectCorePoolSize] attr must not be empty");
            }
            if (Objects.isNull(collectMaxPoolSize)) {
                throw new ConfigurationException("when collectMode is ASYNC metrics [collectMaxPoolSize] attr must not be empty");
            }
            if (Objects.isNull(collectKeepAliveMillis)) {
                throw new ConfigurationException("when collectMode is ASYNC metrics [collectKeepAliveMillis] attr must not be empty");
            }
            if (Objects.isNull(collectQueueCapacity)) {
                throw new ConfigurationException("when collectMode is ASYNC metrics [collectQueueCapacity] attr must not be empty");
            }
        }
    }
}
