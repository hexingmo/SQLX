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
import java.util.stream.Collectors;

/**
 * Random load balancing.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class WeightRandomLoadBalance extends AbstractLoadBalance<NodeAttribute> {

    private final Random random = new Random();

    public WeightRandomLoadBalance(List<NodeAttribute> options) {
        super(options);
    }


    @Override
    public NodeAttribute choose() {

        List<NodeAttribute> validOptions = getOptions().stream()
                .filter(nodeAttr -> nodeAttr.getNodeState().isAvailable())
                .collect(Collectors.toList());

        if (validOptions.isEmpty()) {
            return null;
        }

        if (validOptions.size() == 1) {
            return validOptions.get(0);
        }

        double totalWeight = validOptions.stream().mapToDouble(NodeAttribute::getWeight).sum();
        double randomWeight = random.nextDouble() * totalWeight;

        double cumulativeWeight = 0;
        for (NodeAttribute node : validOptions) {
            cumulativeWeight += node.getWeight();
            if (cumulativeWeight >= randomWeight) {
                return node;
            }
        }

        return validOptions.get(0);
    }
}
