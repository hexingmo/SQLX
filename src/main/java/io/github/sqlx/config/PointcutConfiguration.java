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

package io.github.sqlx.config;

import io.github.sqlx.exception.ConfigurationException;
import io.github.sqlx.util.CollectionUtils;
import io.github.sqlx.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * The RoutingPointcutConfiguration class is used to configure pointcut information for routing.
 * A pointcut represents a set of join points where a particular behavior is applied.
 * This class implements the ConfigurationValidator interface to ensure the correctness and validity of the configuration.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Data
@Slf4j
public class PointcutConfiguration implements ConfigurationValidator {

    /**
     * Expression: Specifies the pointcut expression, used to define the matching rules for join points.
     */
    private String expression;

    /**
     * Cluster: Specifies the cluster to which the pointcut belongs, used for routing decisions.
     */
    private String cluster;

    /**
     * Nodes: Specifies a list of nodes, used for detailed routing within the cluster.
     */
    private List<String> nodes = new ArrayList<>();

    /**
     * Propagation: Indicates whether the routing behavior should be propagated, default is true.
     * When true, the routing behavior will affect subsequent calls in the call chain.
     */
    private Boolean propagation = true;


    /**
     * Validates the correctness of the configuration.
     * This method checks whether the essential configuration items such as expression, cluster, and nodes are correctly set.
     * If any configuration item is missing or invalid, a ConfigurationException will be thrown, indicating the specific configuration error.
     *
     * @throws ConfigurationException Thrown when the configuration validation fails.
     */
    @Override
    public void validate() {
        if (StringUtils.isBlank(expression)) {
            throw new ConfigurationException("pointcuts [expression] attr must not be empty");
        }
        if (StringUtils.isBlank(cluster) && CollectionUtils.isEmpty(nodes)) {
            throw new ConfigurationException("pointcuts [cluster] or [nodes] attr must not be empty");
        }
        if (Objects.isNull(propagation)) {
            throw new ConfigurationException("pointcuts [propagation] attr must not be null");
        }
    }

    public synchronized void removeNode(String nodeName) {
        log.info("pointcut {} remove node {}" , getExpression() , nodeName);
        nodes.remove(nodeName);
    }
}
