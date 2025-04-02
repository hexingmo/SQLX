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
package com.github.sqlx.loadbalance;


import com.github.sqlx.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractLoadBalance<T> implements LoadBalance<T> {

    private final List<T> options = new ArrayList<>();

    protected AbstractLoadBalance(List<T> options) {
        if (CollectionUtils.isNotEmpty(options)) {
            this.options.addAll(options);
        }
    }

    @Override
    public void addOption(T option) {
        if (option != null) {
            options.add(option);
        }
    }

    @Override
    public void removeOption(T option) {
        options.remove(option);
    }

    protected List<T> getOptions() {
        return options;
    }
}
