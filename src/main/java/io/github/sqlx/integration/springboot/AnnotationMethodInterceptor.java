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
package io.github.sqlx.integration.springboot;

import io.github.sqlx.annotation.SqlRouting;
import io.github.sqlx.config.ClusterConfiguration;
import io.github.sqlx.config.SqlXConfiguration;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class AnnotationMethodInterceptor extends AbstractMethodInterceptor {

    private final SqlXConfiguration configuration;

    public AnnotationMethodInterceptor(SqlXConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected RouteAttribute getRoutingAttribute(MethodInvocation mi) {
        Method method = mi.getMethod();
        SqlRouting sqlRouting = AnnotationUtils.findAnnotation(method, SqlRouting.class);
        if (sqlRouting == null) {
            Class<?> targetClass = AopUtils.getTargetClass(mi.getThis());
            method = AopUtils.getMostSpecificMethod(mi.getMethod(), targetClass);
            sqlRouting = AnnotationUtils.findAnnotation(method, SqlRouting.class);
        }
        if (sqlRouting != null) {
            String clusterName = sqlRouting.cluster();
            List<String> specificNodes = Arrays.asList(sqlRouting.nodes());
            List<String> nodes = new ArrayList<>();
            Optional<ClusterConfiguration> clusterConfOptional = configuration.getClusters().stream()
                    .filter(cluster -> cluster.getName().equals(clusterName))
                    .findFirst();

            // a cluster is configured
            if (clusterConfOptional.isPresent()) {
                Set<String> clusterNodes = clusterConfOptional.get().getNodes();
                if (ArrayUtils.isEmpty(sqlRouting.nodes())) {
                    nodes.addAll(clusterNodes);
                } else if (clusterNodes.containsAll(specificNodes)) {
                    nodes.addAll(specificNodes);
                } else {
                    throw new IllegalArgumentException("Cluster node: " + clusterName + " does not contain nodes: " + Arrays.toString(sqlRouting.nodes()));
                }
            } else {
                nodes.addAll(specificNodes);
            }
            return new RouteAttribute(sqlRouting.cluster(), nodes, sqlRouting.propagation(), null, null, method);
        }
        return null;
    }
}
