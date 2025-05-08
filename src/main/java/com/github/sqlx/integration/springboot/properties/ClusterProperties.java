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

package com.github.sqlx.integration.springboot.properties;

import lombok.Data;

import java.util.Set;

/**
 * Cluster configuration properties.
 *
 * @author jing yun
 * @since 1.0
 */
@Data
public class ClusterProperties {

    /**
     * The name of the cluster, used to identify the cluster.
     */
    private String name;

    /**
     * Whether the cluster is defaulted.
     */
    private Boolean defaulted = false;

    /**
     * The write nodes in the cluster.
     */
    private Set<String> writableNodes;

    /**
     * The read nodes in the cluster.
     */
    private Set<String> readableNodes;

    /**
     * The write load balance class.
     */
    private String writeLoadBalanceClass;

    /**
     * The read load balance class.
     */
    private String readLoadBalanceClass;
}
