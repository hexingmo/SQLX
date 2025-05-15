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
package io.github.sqlx.endpoint.http;

/**
 * Interface defining a handler for HTTP requests.
 * This interface should be implemented by classes that need to process HTTP requests and return a response.
 *
 * @param <R> The type of the response object returned by the handle method.
 * @author He Xing Mo
 * @since 1.0
 */
public interface HttpHandler<R> {

    /**
     * Handles an incoming HTTP request.
     *
     * @param request The HTTP request to handle.
     * @return The result of handling the request, encapsulated in a response object of type R.
     */
    R handle(HttpRequest request);
}
