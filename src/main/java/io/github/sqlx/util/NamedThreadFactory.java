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
package io.github.sqlx.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final boolean isDaemon;
    private final Thread.UncaughtExceptionHandler handler;


    public NamedThreadFactory(String prefix, boolean isDeamon) {
        this(prefix, null, isDeamon);
    }

    public NamedThreadFactory(String prefix, ThreadGroup threadGroup, boolean isDeamon) {
        this(prefix, threadGroup, isDeamon, null);
    }


    public NamedThreadFactory(String prefix, ThreadGroup threadGroup, boolean isDaemon, Thread.UncaughtExceptionHandler handler) {
        this.prefix = prefix;
        if (null == threadGroup) {
            threadGroup = Thread.currentThread().getThreadGroup();
        }
        this.group = threadGroup;
        this.isDaemon = isDaemon;
        this.handler = handler;
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = new Thread(this.group, r, String.format("%s-%s", prefix, threadNumber.getAndIncrement()));

        if (!t.isDaemon()) {
            if (isDaemon) {
                t.setDaemon(true);
            }
        } else if (!isDaemon) {
            t.setDaemon(false);
        }
        if(null != this.handler) {
            t.setUncaughtExceptionHandler(handler);
        }
        if (Thread.NORM_PRIORITY != t.getPriority()) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
