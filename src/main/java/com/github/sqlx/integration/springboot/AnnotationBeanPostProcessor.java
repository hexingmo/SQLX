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
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.util.CollectionUtils;
import com.github.sqlx.util.StringUtils;
import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Create a proxy object for beans with the  @SqlRouting  annotation
 * @author He Xing Mo
 * @since 1.0
 *
 * @see SqlRouting
 * @see AbstractMethodInterceptor
 */
public class AnnotationBeanPostProcessor extends AbstractAdvisingBeanPostProcessor {

    private final SqlXConfiguration configuration;

    public AnnotationBeanPostProcessor(SqlXConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Use reflection to find methods with @SqlRouting annotation
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            SqlRouting sqlRouting = method.getAnnotation(SqlRouting.class);
            if (sqlRouting != null) {
                // Perform validation on the annotation data
                validate(sqlRouting, method);
            }
        });
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        super.advisor = new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(null , SqlRouting.class , true) , new AnnotationMethodInterceptor(configuration));
        super.setBeforeExistingAdvisors(true);
        return super.postProcessAfterInitialization(bean , beanName);
    }


    private void validate(SqlRouting sqlRouting, Method method) {
        String cluster = sqlRouting.cluster();
        List<String> nodes = Arrays.asList(sqlRouting.nodes());
        if (StringUtils.isBlank(cluster) && CollectionUtils.isEmpty(nodes)) {
            throw new ConfigurationException(String.format("%s Method @SqlRouting Annotation [cluster] or [nodes] attr must not be empty" , method.getName()));
        }
        String pointExpressions = String.format("%s.%s Method @SqlRouting Annotation", method.getDeclaringClass().getName(),method.getName());
        configuration.validatePointcutRoutingAttr(pointExpressions , cluster , nodes , sqlRouting.propagation());
    }
}
