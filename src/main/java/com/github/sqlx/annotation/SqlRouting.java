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

package com.github.sqlx.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code SqlRouting} annotation is used to specify routing information
 * for SQL operations in a distributed database environment. It can be
 * applied to methods to dictate how SQL queries should be directed
 * to specific database clusters and nodes.
 *
 * <p>Attributes:</p>
 *
 * <ul>
 *   <li>{@code cluster}: (default is an empty string) Specifies the
 *       name of the database cluster to which the SQL operation
 *       should be routed.</li>
 *   <li>{@code nodes}: (default is an empty array) An array of node
 *       identifiers that indicates specific database nodes within
 *       the cluster to route the SQL operation to.</li>
 *   <li>{@code propagation}: (default is true) Indicates whether
 *       the routing behavior should propagate to any nested SQL
 *       operations or transactions.</li>
 * </ul>
 *
 * <p>This annotation is retained at runtime, allowing for dynamic
 * routing decisions based on the specified parameters.</p>
 *
 * Usage Example:
 * @SqlRouting(cluster = "user-db", nodes = {"node1", "node2"}, propagation = true)
 * public void performDatabaseOperation() {
 *     // Implementation of the database operation
 * }
 * Note: Proper usage of this annotation can enhance performance and reliability in applications
 * that rely on multiple database clusters and nodes.
 *
 * @author He Xing Mo
 * @since 1.0
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SqlRouting {

    /**
     * The name of the target database cluster. If specified, SQL operations will be routed to this cluster.
     * If not specified (empty or null), routing will fall back to checking the 'nodes' attribute to determine
     * the destination for SQL operations.
     */
    String cluster() default "";

    /**
     * when a cluster is specified, only nodes within that cluster can be specified in 'nodes'.
     * when no cluster is specified, any nodes can be specified in 'nodes'.
     */
    String[] nodes() default {};

    /**
     * when there are multiple levels of method nesting, control the propagation behavior of the routing.
     * 'true' indicates that the outer routing is allowed to propagate to the current method,
     * while 'false' indicates that the upper-level routing is not allowed to propagate to the current method.
     * when both routing and transactions are present, it is essential to ensure that the propagation behavior
     * of the transaction is consistent with the propagation behavior of the routing.
     */
    boolean propagation() default true;
}
