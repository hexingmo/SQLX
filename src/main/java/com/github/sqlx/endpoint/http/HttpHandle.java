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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark methods that handle HTTP requests.
 * This annotation specifies the path and HTTP method that the annotated method should respond to.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpHandle {

    /**
     * Specifies the URL path that this handler method should respond to.
     * The path can include regular expression patterns for more flexible matching.
     *
     * @return The URL path pattern as a string.
     */
    String path();

    /**
     * Specifies the HTTP method that this handler method should respond to.
     * If not specified, the default value is "GET".
     *
     * @return The HTTP method as a string.
     */
    String method() default "GET";
}
