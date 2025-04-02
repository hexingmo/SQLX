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

package com.github.sqlx;


/**
 * @author He Xing Mo
 * @since 1.0
 */
public enum NodeState {

    /**
     * The node is in the startup state
     */
    UP ,

    /**
     * The node is in a faulty downtime state
     */
    DOWN ,

    /**
     * The node is excluded from providing service nodes
     */
    OUT_OF_SERVICE ,

    /**
     * The node is in an unknown state
     */
    UNKNOWN;

    public boolean isAvailable() {
        return this == UP || this == UNKNOWN;
    }
}
