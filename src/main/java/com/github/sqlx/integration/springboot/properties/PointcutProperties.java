package com.github.sqlx.integration.springboot.properties;

import lombok.Data;

import java.util.List;

/**
 * Pointcut configuration properties.
 *
 * @author jing yun
 * @since 1.0
 */
@Data
public class PointcutProperties {

    /**
     * Expression: Specifies the pointcut expression, used to define the matching rules for join points.
     */
    private String expression;

    /**
     * Cluster: Specifies the cluster to which the pointcut belongs, used for routing decisions.
     */
    private String cluster;

    /**
     * Nodes: Specifies a list of nodes, used for detailed routing within the cluster.
     */
    private List<String> nodes;

    /**
     * Propagation: Indicates whether the routing behavior should be propagated, default is true.
     * When true, the routing behavior will affect subsequent calls in the call chain.
     */
    private Boolean propagation = true;

}
