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

import java.util.List;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class TransactionalSqlInfo {

    private String sql;

    private String nativeSql;

    private String sqlType;

    private Set<String> databases;

    private Set<String> tables;

    private Long executeTimeElapsedMillis;

    private Long updateRows;

    private Long selectedRows;

    private List<String> exceptions;
}
