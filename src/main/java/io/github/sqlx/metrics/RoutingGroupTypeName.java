package io.github.sqlx.metrics;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
@Accessors(chain = true)
public class RoutingGroupTypeName {

    private String groupClassName;

    private List<String> ruleClassNames;
}
