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

import com.github.sqlx.config.PointcutConfiguration;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class ExpressionMethodInterceptor extends AbstractMethodInterceptor {

    private static final Set<RouteAttrAspectJExpressionPointcut> POINTCUTS = new HashSet<>();

    private static final Map<Method , PointcutConfiguration> CACHE = new ConcurrentHashMap<>();

    public void addPointcut(RouteAttrAspectJExpressionPointcut pointcut) {
        POINTCUTS.add(pointcut);
    }

    @Override
    protected RouteAttribute getRoutingAttribute(MethodInvocation mi) {
        Method method = mi.getMethod();
        PointcutConfiguration pointcutConf = CACHE.get(method);
        if (pointcutConf != null) {
            return new RouteAttribute(pointcutConf.getCluster(), pointcutConf.getNodes(), pointcutConf.getPropagation(), null , null , method);
        }

        Class<?> targetClass = AopUtils.getTargetClass(mi.getThis());
        Object[] args = mi.getArguments();
        for (RouteAttrAspectJExpressionPointcut pointcut : POINTCUTS) {
            boolean matches = pointcut.matches(method, targetClass, args);
            if (matches) {
                PointcutConfiguration pcf = pointcut.getPointcutConf();
                CACHE.put(method , pcf);
                return new RouteAttribute(pcf.getCluster(), pcf.getNodes(), pcf.getPropagation(), null , null , method);
            }
        }
        return null;
    }
}
