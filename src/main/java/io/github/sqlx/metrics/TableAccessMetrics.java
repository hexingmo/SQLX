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

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Indices({
        @Index(value = "database" ,type = IndexType.NonUnique),
        @Index(value = "table" ,type = IndexType.NonUnique),
        @Index(value = "queryCount" ,type = IndexType.NonUnique),
        @Index(value = "insertCount" ,type = IndexType.NonUnique),
        @Index(value = "updateCount" ,type = IndexType.NonUnique),
        @Index(value = "deleteCount" ,type = IndexType.NonUnique),
        @Index(value = "readCount" ,type = IndexType.NonUnique),
        @Index(value = "writeCount" ,type = IndexType.NonUnique),
        @Index(value = "totalCount" ,type = IndexType.NonUnique),
        @Index(value = "updatedTime" ,type = IndexType.NonUnique),
})
@Data
@Accessors(chain = true)
public class TableAccessMetrics {

    @Id
    private String fullTableName;

    private String database;

    private String table;

    private Long queryCount;

    private Long insertCount;

    private Long updateCount;

    private Long deleteCount;

    private Long readCount;

    private Long writeCount;

    private Long totalCount;

    private Long updatedTime;
}
