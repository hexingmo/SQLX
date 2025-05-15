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
package io.github.sqlx.metrics;

import io.github.sqlx.config.MetricsConfiguration;
import io.github.sqlx.util.NamedThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class AsyncMetricsCollector extends SyncMetricsCollector {

    private final ThreadPoolExecutor collectThreadPool;

    public AsyncMetricsCollector(MetricsRepository metricsRepository, MetricsConfiguration metricsConfiguration) {
        super(metricsRepository);
        this.collectThreadPool = new ThreadPoolExecutor(metricsConfiguration.getCollectCorePoolSize(),
                metricsConfiguration.getCollectMaxPoolSize(),
                metricsConfiguration.getCollectKeepAliveMillis(), TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(metricsConfiguration.getCollectQueueCapacity()),
                new NamedThreadFactory("sql-routing-metrics-collect", true),
                new ThreadPoolExecutor.CallerRunsPolicy());
        Runtime.getRuntime().addShutdownHook(new Thread(collectThreadPool::shutdownNow));
    }

    @Override
    public void collect(final Object target) {
        this.collectThreadPool.submit(() -> super.metricsRepository.save(target));
    }
}
