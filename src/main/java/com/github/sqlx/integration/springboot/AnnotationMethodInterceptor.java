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
package com.github.sqlx.integration.springboot;

import com.github.sqlx.annotation.SqlRouting;
import com.github.sqlx.config.ClusterConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
            List<String> nodes = new ArrayList<>();
            Optional<ClusterConfiguration> optional = configuration.getClusters().stream()
                    .filter(cluster -> cluster.getName().equals(clusterName))
                    .findFirst();
            if (ArrayUtils.isEmpty(sqlRouting.nodes())) {
                if (optional.isPresent()) {
                    nodes.addAll(optional.get().getNodes());
                }
            } else {
                if (optional.isPresent()) {
                    Set<String> clusterNodes = optional.get().getNodes();
                    List<String> annotationNodes = Arrays.asList(sqlRouting.nodes());
                    if (clusterNodes.containsAll(annotationNodes)) {
                        nodes.addAll(annotationNodes);
                    } else {
                        nodes.addAll(clusterNodes);
                    }
                } else {
                    nodes = Arrays.asList(sqlRouting.nodes());
                }
            }
            return new RouteAttribute(sqlRouting.cluster(), nodes, sqlRouting.propagation(), null , null , method);
        }
        return null;
    }
}
