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

import java.util.Objects;

/**
 * Enum representing different types of routing nodes in a data access system.
 * Each type indicates the permissions for reading and writing data.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public enum NodeType {

    /**
     * only allows read operations
     */
    READ ,

    /**
     * only allows write operations
     */
    WRITE ,

    /**
     * allows both read and write operations
     */
    READ_WRITE,

    /**
     * does not differentiate between read and write modes, readable and writable
     */
    INDEPENDENT;

    public boolean canWrite() {
        return Objects.equals(this , INDEPENDENT) || Objects.equals(this , READ_WRITE) || Objects.equals(this , WRITE);
    }

    public boolean canRead() {
        return Objects.equals(this , INDEPENDENT) || Objects.equals(this , READ_WRITE) || Objects.equals(this , READ);
    }
}
