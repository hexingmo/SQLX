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

import io.github.sqlx.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Perform SQL routing based on the name specified in the  `@SqlRouting`  annotation.
 * Note that this is not effective on transactional methods.
 *
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public abstract class AbstractMethodInterceptor implements MethodInterceptor {

    private static final ThreadLocal<LinkedList<RouteAttribute>> ATTRIBUTE_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {

        Method method = mi.getMethod();
        Method specificMethod = AopUtils.getMostSpecificMethod(mi.getMethod(), AopUtils.getTargetClass(mi.getThis()));
        try {
            bindCurrentRoutingAttribute(mi);
            return mi.proceed();
        } finally {
            RouteAttribute ra = RoutingContext.getRoutingAttribute();
            if (ra != null) {
                boolean isRoot = Boolean.TRUE.equals(ra.getRoot()) && (Objects.equals(ra.getMethod(), method) || Objects.equals(ra.getMethod(), specificMethod));
                if (isRoot) {
                    RoutingContext.clear();
                    ATTRIBUTE_THREAD_LOCAL.remove();
                } else {
                    if (ra.getPrev() != null) {
                        RoutingContext.force(ra.getPrev());
                    }
                }
            }
        }
    }

    protected abstract RouteAttribute getRoutingAttribute(MethodInvocation mi);


    private void bindCurrentRoutingAttribute(MethodInvocation mi) {
        RouteAttribute ra = getRoutingAttribute(mi);
        LinkedList<RouteAttribute> ras = ATTRIBUTE_THREAD_LOCAL.get();
        if (ras == null) {
            ras = new LinkedList<>();
            ra.setRoot(true);
            ra.setPrev(null);
            ras.addFirst(ra);
            RoutingContext.force(ra);
        } else {
            RouteAttribute last = ras.getLast();
            if (Boolean.FALSE.equals(ra.getPropagation())) {
                ra.setRoot(false);
                ra.setPrev(last);
                ras.addLast(ra);
                RoutingContext.force(ra);
            } else {
                RoutingContext.force(last);
            }
        }
        ATTRIBUTE_THREAD_LOCAL.set(ras);
    }
}
