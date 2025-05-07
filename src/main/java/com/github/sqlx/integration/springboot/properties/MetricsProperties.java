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

package com.github.sqlx.integration.springboot.properties;

import com.github.sqlx.config.MetricsCollectScope;
import com.github.sqlx.metrics.MetricsCollectMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Metrics configuration properties.
 *
 * @author jing yun
 * @since 1.0
 */
@ConfigurationProperties(prefix = "sqlx.metrics")
@Data
public class MetricsProperties {

    /**
     * Whether metrics collection is enabled.
     */
    private Boolean enabled;

    /**
     * Username for metrics reporting or authentication.
     */
    private String username;

    /**
     * Password for metrics reporting or authentication.
     */
    private String password;

    /**
     * Threshold in milliseconds for a SQL statement to be considered slow.
     */
    private Long slowSqlMillis;

    /**
     * Threshold in milliseconds for a transaction to be considered slow.
     */
    private Long slowTransactionMillis;

    /**
     * Whether to enable routing metrics collection.
     */
    private Boolean enableRoutingMetrics = true;

    /**
     * Whether to enable SQL execution metrics collection.
     */
    private Boolean enableSqlMetrics = true;

    /**
     * Whether to enable transaction metrics collection.
     */
    private Boolean enableTransactionMetrics = true;

    /**
     * Scope of metrics collection (e.g., only slow queries, all queries).
     */
    private MetricsCollectScope collectScope = MetricsCollectScope.SLOW;

    /**
     * Mode of metrics collection (e.g., synchronous or asynchronous).
     */
    private MetricsCollectMode collectMode = MetricsCollectMode.SYNC;

    /**
     * Directory path where metrics files will be stored (if applicable).
     */
    private String fileDirectory;

    /**
     * Duration for which collected metrics should be retained.
     */
    private Duration dataRetentionDuration = Duration.ofDays(3);

    /**
     * Core thread pool size for metrics collection tasks.
     */
    private Integer collectCorePoolSize;

    /**
     * Maximum thread pool size for metrics collection tasks.
     */
    private Integer collectMaxPoolSize;

    /**
     * Keep-alive time in milliseconds for idle threads in the metrics thread pool.
     */
    private Long collectKeepAliveMillis;

    /**
     * Capacity of the task queue used by the metrics thread pool.
     */
    private Integer collectQueueCapacity;

}
