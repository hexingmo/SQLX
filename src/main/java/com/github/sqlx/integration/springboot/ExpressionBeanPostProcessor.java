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

import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.config.PointcutConfiguration;
import com.github.sqlx.util.CollectionUtils;
import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class ExpressionBeanPostProcessor extends AbstractAdvisingBeanPostProcessor {

    private final SqlXConfiguration sqlXConfiguration;

    public ExpressionBeanPostProcessor(SqlXConfiguration sqlXConfiguration) {
        this.sqlXConfiguration = sqlXConfiguration;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Advised) {
            addAdvisor((Advised) bean);
            return bean;
        }

        if (isEligible(bean, beanName)) {
            ProxyFactory proxyFactory = prepareProxyFactory(bean, beanName);
            if (!proxyFactory.isProxyTargetClass()) {
                evaluateProxyInterfaces(bean.getClass(), proxyFactory);
            }
            addAdvisor(proxyFactory);
            customizeProxyFactory(proxyFactory);
            return proxyFactory.getProxy(getProxyClassLoader());
        }

        return bean;
    }


    @Override
    protected boolean isEligible(Class<?> targetClass) {
        boolean isEligible = false;
        if (CollectionUtils.isNotEmpty(sqlXConfiguration.getPointcuts())) {
            ExpressionMethodInterceptor interceptor = new ExpressionMethodInterceptor();
            for (PointcutConfiguration pointcutConf : sqlXConfiguration.getPointcuts()) {
                RouteAttrAspectJExpressionPointcut pointcut = new RouteAttrAspectJExpressionPointcut(pointcutConf.getExpression() , pointcutConf);
                DefaultPointcutAdvisor expressionAdvisor = new DefaultPointcutAdvisor(pointcut, interceptor);
                if (AopUtils.canApply(expressionAdvisor, targetClass)) {
                    isEligible = true;
                    break;
                }
            }
        }
        return super.isEligible(targetClass) || isEligible;
    }

    private void addAdvisor(Advised advised) {
        if (advised.isFrozen()) {
            return;
        }

        List<PointcutConfiguration> pointcuts = sqlXConfiguration.getPointcuts();
        if (CollectionUtils.isNotEmpty(pointcuts)) {
            ExpressionMethodInterceptor interceptor = new ExpressionMethodInterceptor();
            for (PointcutConfiguration pointcutConf : pointcuts) {
                RouteAttrAspectJExpressionPointcut pointcut = new RouteAttrAspectJExpressionPointcut(pointcutConf.getExpression() , pointcutConf);
                interceptor.addPointcut(pointcut);
                DefaultPointcutAdvisor expressionAdvisor = new DefaultPointcutAdvisor(pointcut, interceptor);
                if (AopUtils.canApply(expressionAdvisor, AopUtils.getTargetClass(advised))) {
                    advised.addAdvisor(0 , expressionAdvisor);
                }
            }
        }
    }

}
