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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractStorageReleaser implements StorageReleaser {

    protected final List<MetricsRepository<?>> repositories = new ArrayList<>();

    private final AtomicBoolean started = new AtomicBoolean(false);

    protected final MetricsConfiguration metricsConfiguration;

    protected AbstractStorageReleaser(MetricsConfiguration metricsConfiguration) {
        this.metricsConfiguration = metricsConfiguration;
    }

    @Override
    public void registerRepository(MetricsRepository<?> repository) {
        repositories.add(repository);
    }

    @Override
    public void start() {
        if (started.compareAndSet(false , true)) {
            internalStart();
        }
    }
    protected abstract void internalStart();
}
