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

package com.github.sqlx.rule;

/**
 * Support routing rules that specify execution priority.
 * Sort by priority from small to large, with smaller numbers indicating higher priority.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public interface PriorityRouteRule extends RouteRule {


    /**
     * specify priority , the smaller the number, the higher the priority.
     * @return Within the minimum and maximum value range of int
     */
    int priority();
}
