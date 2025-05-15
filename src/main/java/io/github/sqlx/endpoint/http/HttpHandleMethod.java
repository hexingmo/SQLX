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

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * A class representing a method that can handle HTTP requests.
 * It encapsulates both the handler object and the method to be invoked.
 * This class provides a convenient way to invoke the handler method with the given arguments.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class HttpHandleMethod {

    /**
     * The handler object that contains the method to be invoked.
     */
    private Object handler;

    /**
     * The method to be invoked on the handler object.
     */
    private Method method;

    /**
     * Invokes the handler method with the provided arguments and returns the result as an {@link HttpResponse}.
     *
     * @param args The arguments to pass to the handler method.
     * @return An instance of {@link HttpResponse} containing the result of the method invocation.
     * @throws Exception If the method invocation fails.
     */
    public HttpResponse invoke(Object... args) throws Exception {
        // Invoke the method on the handler object with the provided arguments
        Object obj = method.invoke(handler, args);

        if (obj == null) {
            return null;
        }

        return (HttpResponse) obj;
    }
}
