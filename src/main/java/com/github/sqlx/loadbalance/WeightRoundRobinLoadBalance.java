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
import java.util.Set;

/**
 * Round-robin load balancing.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class WeightRoundRobinLoadBalance extends AbstractLoadBalance {

    private Integer currentIndex;
    private Double currentWeight;
    private Double maxWeight;
    private Double gcdWeight;

    public WeightRoundRobinLoadBalance(Set<NodeAttribute> options) {
        super(options);
        this.currentIndex = -1;
        this.currentWeight = 0d;
        this.maxWeight = 0d;
        this.gcdWeight = 0d;
    }

    @Override
    protected NodeAttribute choose(List<NodeAttribute> availableOptions) {
        for (NodeAttribute target : availableOptions) {
            double weight = target.getWeight();
            maxWeight = Math.max(maxWeight, weight);
            gcdWeight = gcd(gcdWeight, weight);
        }

        NodeAttribute chosen;
        while (true) {
            currentIndex = (currentIndex + 1) % availableOptions.size();
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcdWeight;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                    if (currentWeight == 0) {
                        return null;
                    }
                }
            }
            NodeAttribute target = availableOptions.get(currentIndex);
            if (target.getWeight() >= currentWeight) {
                chosen = target;
                break;
            }
        }
        return chosen;
    }

    private static double gcd(double a, double b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }
}
