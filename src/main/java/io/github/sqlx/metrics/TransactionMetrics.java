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

import io.github.sqlx.jdbc.transaction.TransactionStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.util.List;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Data
@Accessors(chain = true)
@Indices({
        @Index(value = "clusterName" ,type = IndexType.NonUnique),
        @Index(value = "node.name" ,type = IndexType.NonUnique),
        @Index(value = "transactionId" ,type = IndexType.NonUnique),
        @Index(value = "transactionName" ,type = IndexType.NonUnique),
        @Index(value = "transactionStatus" ,type = IndexType.NonUnique),
        @Index(value = "totalTimeElapsedMillis" ,type = IndexType.NonUnique),
        @Index(value = "succeeded" ,type = IndexType.NonUnique),
        @Index(value = "createdTime" ,type = IndexType.NonUnique),
})
public class TransactionMetrics {

    @Id
    private String transactionId;

    private String transactionName;

    private String clusterName;

    private NodeInfo node;

    private List<TransactionalSqlInfo> sqlList;

    private Long updateRows;

    private Long selectedRows;

    private Long sqlExecuteTimeElapsedMillis;

    private Long timeElapsedMillis;

    private Long totalTimeElapsedMillis;

    private Set<String> databases;

    private Set<String> tables;

    private TransactionStatus transactionStatus;

    private Boolean succeeded;

    private String exception;

    private Long createdTime;

    private Long updatedTime;

}
