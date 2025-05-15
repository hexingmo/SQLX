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
package io.github.sqlx.rule;

/**
 * Represents a SQL function that can be executed to return a result of type T.
 *
 * @param <T> the type of the result returned by the function
 * @author He Xing Mo
 * @since 1.0
 */
@FunctionalInterface
public interface SqlFunction<T> {

    /**
     * Executes the SQL function and returns the result.
     *
     * @return the result of the function execution
     */
    T run();
}