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
package com.github.sqlx.endpoint.http;

import com.github.sqlx.exception.ManagementException;
import com.github.sqlx.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A class that dispatches HTTP requests to appropriate handler methods based on URL path and HTTP method.
 * It implements the {@link HttpHandler} interface and processes incoming HTTP requests by matching them to
 * annotated methods in registered handlers.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class DispatcherHttpHandler implements HttpHandler<HttpResponse> {

    /**
     * A list of objects containing methods annotated with {@link HttpHandle} to handle HTTP requests.
     */
    private List<Object> handlers = new ArrayList<>();

    /**
     * Constructor to initialize the dispatcher with a list of handler objects.
     *
     * @param handlers The list of handler objects to register.
     */
    public DispatcherHttpHandler(List<Object> handlers) {
        this.handlers.addAll(handlers);
    }

    /**
     * Handles an incoming HTTP request by finding and invoking the appropriate handler method.
     *
     * @param request The incoming HTTP request.
     * @return An HTTP response object.
     */
    @Override
    public HttpResponse handle(HttpRequest request) {
        // Match the request to a handler method
        HttpHandleMethod httpHandleMethod = matchHandle(request);

        // If no matching handler method is found, return a 404 response
        if (httpHandleMethod == null) {
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setStatus(404);
            return httpResponse;
        }

        // Invoke the matched handler method and return its response
        HttpResponse httpResponse;
        try {
            httpResponse = httpHandleMethod.invoke(request);
        } catch (Exception e) {
            throw new ManagementException(e);
        }

        return httpResponse;
    }

    /**
     * Matches an incoming HTTP request to a handler method based on the request's path and HTTP method.
     *
     * @param request The incoming HTTP request.
     * @return An instance of {@link HttpHandleMethod} representing the matched handler method, or null if no match is found.
     */
    private HttpHandleMethod matchHandle(HttpRequest request) {
        // Iterate over all registered handlers
        for (Object handler : handlers) {
            // Iterate over all methods in the current handler object
            for (Method method : handler.getClass().getDeclaredMethods()) {
                // Check if the method has the HttpHandle annotation
                HttpHandle anno = method.getDeclaredAnnotation(HttpHandle.class);
                if (anno == null) {
                    continue;
                }

                // Check if the request path matches the annotated path pattern
                if (!Pattern.matches(anno.path(), request.getPath())) {
                    continue;
                }

                // Check if the request method matches the annotated HTTP method
                if (!StringUtils.equalsIgnoreCase(anno.method(), request.getMethod())) {
                    continue;
                }

                // Return a new HttpHandleMethod instance if all checks pass
                return new HttpHandleMethod(handler, method);
            }
        }

        // Return null if no matching handler method is found
        return null;
    }
}
