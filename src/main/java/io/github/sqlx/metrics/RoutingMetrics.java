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
package io.github.sqlx.metrics;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
@Accessors(chain = true)
@Indices({
        @Index(value = "sql" ,type = IndexType.Fulltext),
        @Index(value = "clusterName" ,type = IndexType.NonUnique),
        @Index(value = "hitNodeAttr.name" ,type = IndexType.NonUnique),
        @Index(value = "transactionId" ,type = IndexType.NonUnique),
        @Index(value = "transactionName" ,type = IndexType.NonUnique),
        @Index(value = "timeElapsedMillis" ,type = IndexType.NonUnique),
        @Index(value = "succeeded" ,type = IndexType.NonUnique),
        @Index(value = "createdTime" ,type = IndexType.NonUnique),
})
public class RoutingMetrics {

    @Id
    private String routingId;

    private String sql;

    private String nativeSql;

    private String clusterName;

    private List<RoutingGroupTypeName> possibleRoutingGroups;

    private RoutingGroupTypeName hitRoutingGroup;

    private String hitRule;

    private SqlInfo sqlInfo;

    private NodeInfo hitNodeAttr;

    private Boolean isTransactionActive;

    private String transactionId;

    private String transactionName;

    private Long beforeTimeMillis;

    private Long afterTimeMillis;

    private Long timeElapsedMillis;

    private Boolean succeeded;

    private String exception;

    private Long createdTime;
}
