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

package com.github.sqlx.loadbalance;

import com.github.sqlx.NodeAttribute;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Random load balancing.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class WeightRandomLoadBalance extends AbstractLoadBalance {

    private final Random random = new Random();

    public WeightRandomLoadBalance() {

    }

    public WeightRandomLoadBalance(Set<NodeAttribute> options) {
        super(options);
    }


    @Override
    protected NodeAttribute choose(List<NodeAttribute> availableOptions) {
        double totalWeight = availableOptions.stream().mapToDouble(NodeAttribute::getWeight).sum();
        double randomWeight = random.nextDouble() * totalWeight;

        double cumulativeWeight = 0;
        for (NodeAttribute node : availableOptions) {
            cumulativeWeight += node.getWeight();
            if (cumulativeWeight >= randomWeight) {
                return node;
            }
        }

        return availableOptions.get(0);
    }
}
