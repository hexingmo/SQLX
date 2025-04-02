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

/**
 * @author He Xing Mo
 * @since 1.0
 */
public enum ReadLoadBalanceType {

    /**
     * Random weights within all readable nodes
     */
    WEIGHT_RANDOM_BALANCE_READABLE,

    /**
     * Random weights within read only nodes
     */
    WEIGHT_RANDOM_BALANCE_ONLY_READ,

    /**
     * Random weights within read_write nodes
     */
    WEIGHT_RANDOM_BALANCE_READ_WRITE,

    /**
     * Random Round-Robin weights within all readable nodes
     */
    WEIGHT_ROUND_ROBIN_BALANCE_READABLE,

    /**
     * Random Round-Robin weights within read only nodes
     */
    WEIGHT_ROUND_ROBIN_BALANCE_ONLY_READ,

    /**
     * Random Round-Robin weights within read_write nodes
     */
    WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE,

}
