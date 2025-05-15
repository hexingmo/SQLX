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

import io.github.sqlx.config.PointcutConfiguration;
import io.github.sqlx.util.StringUtils;
import lombok.Getter;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class RouteAttrAspectJExpressionPointcut extends AspectJExpressionPointcut {


    @Getter
    private final PointcutConfiguration pointcutConf;


    public RouteAttrAspectJExpressionPointcut(String expression, PointcutConfiguration pointcutConf) {
        this.pointcutConf = pointcutConf;
        setExpression(expression);
    }

    public RouteAttrAspectJExpressionPointcut(Class<?> declarationScope, String[] paramNames, Class<?>[] paramTypes, String expression, PointcutConfiguration pointcutConf) {
        super(declarationScope, paramNames, paramTypes);
        this.pointcutConf = pointcutConf;
        setExpression(expression);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AspectJExpressionPointcut)) {
            return false;
        }
        AspectJExpressionPointcut otherPc = (AspectJExpressionPointcut) other;
        return StringUtils.equals(this.getExpression(), otherPc.getExpression());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getExpression());
    }
}
