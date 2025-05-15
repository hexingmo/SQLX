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

package io.github.sqlx.banner;

/**
 * The Printer interface defines a standard interface for printing operations, allowing for the output of strings.
 * This interface is primarily used for abstracting the printing behavior, enabling different implementations to be used interchangeably.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public interface Printer {

    /**
     * Prints the specified string.
     *
     * @param content The string to be printed. This parameter cannot be null.
     */
    void print(String content);
}
