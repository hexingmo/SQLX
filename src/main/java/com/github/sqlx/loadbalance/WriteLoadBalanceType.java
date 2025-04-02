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
public enum WriteLoadBalanceType {

    /**
     * Random weights within writable nodes
     */
    WEIGHT_RANDOM_BALANCE_WRITABLE,

    /**
     * Random weights within write only nodes
     */
    WEIGHT_RANDOM_BALANCE_WRITE_ONLY,

    /**
     * Random weights within read_write nodes
     */
    WEIGHT_RANDOM_BALANCE_READ_WRITE,

    /**
     * Round-Robin weights within writable nodes
     */
    WEIGHT_ROUND_ROBIN_BALANCE_WRITABLE,


    /**
     * Round-Robin weights within write only nodes
     */
    WEIGHT_ROUND_ROBIN_BALANCE_WRITE_ONLY,

    /**
     * Round-Robin weights within write only nodes
     */
    WEIGHT_ROUND_ROBIN_BALANCE_READ_WRITE;

}
