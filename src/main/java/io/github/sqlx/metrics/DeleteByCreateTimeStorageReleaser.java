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

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class DeleteByCreateTimeStorageReleaser extends AbstractStorageReleaser {

    private final ScheduledThreadPoolExecutor executor;


    public DeleteByCreateTimeStorageReleaser(MetricsConfiguration metricsConfiguration) {
        super(metricsConfiguration);
        this.executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("metrics-storage-space-release", false));

    }

    @Override
    public void release() {
        long timestamp = System.currentTimeMillis() - metricsConfiguration.getDataRetentionDuration().toMillis();
        for (MetricsRepository<?> repository : repositories) {
            repository.deleteByCreatedTimeLessThan(timestamp);
        }
    }

    @Override
    protected void internalStart() {
        long period = metricsConfiguration.getDataRetentionDuration().toMillis() / 3;
        this.executor.scheduleAtFixedRate(this::release, 10000 , period , TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this.executor::shutdownNow));
    }
}
