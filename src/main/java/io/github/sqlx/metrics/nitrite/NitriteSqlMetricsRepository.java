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
package io.github.sqlx.metrics.nitrite;

import io.github.sqlx.metrics.MetricsQueryCriteria;
import io.github.sqlx.metrics.SortOrder;
import io.github.sqlx.metrics.SortOrderField;
import io.github.sqlx.metrics.SqlMetrics;
import io.github.sqlx.metrics.SqlMetricsQueryCriteria;
import io.github.sqlx.metrics.SqlMetricsRepository;
import io.github.sqlx.util.CollectionUtils;
import io.github.sqlx.util.StringUtils;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class NitriteSqlMetricsRepository extends AbstractNitriteRepository<SqlMetrics> implements SqlMetricsRepository {

    public NitriteSqlMetricsRepository(String fileDirectory) {
        super(SqlMetrics.class, fileDirectory);
    }

    @Override
    protected ObjectFilter buildFilter(MetricsQueryCriteria criteria) {

        SqlMetricsQueryCriteria sqc = (SqlMetricsQueryCriteria) criteria;
        List<ObjectFilter> filters = new ArrayList<>();

        if (StringUtils.isNotBlank(sqc.getStatementId())) {
            filters.add(ObjectFilters.eq("statementId", sqc.getStatementId()));
        }

        if (StringUtils.isNotBlank(criteria.getSql())) {
            filters.add(ObjectFilters.text("sql", criteria.getSql()));
        }

        if (StringUtils.isNotBlank(criteria.getClusterName())) {
            filters.add(ObjectFilters.eq("clusterName", criteria.getClusterName()));
        }

        if (StringUtils.isNotBlank(criteria.getNodeName())) {
            filters.add(ObjectFilters.eq("node.name" , criteria.getNodeName()));
        }

        if (StringUtils.isNotBlank(criteria.getTransactionId())) {
            filters.add(ObjectFilters.eq("transactionId", criteria.getTransactionId()));
        }

        if (StringUtils.isNotBlank(criteria.getTransactionName())) {
            filters.add(ObjectFilters.eq("transactionName", criteria.getTransactionId()));
        }

        if (StringUtils.isNotBlank(sqc.getSqlType())) {
            filters.add(ObjectFilters.eq("sqlType", sqc.getSqlType()));
        }

        if (CollectionUtils.isNotEmpty(sqc.getDatabases())) {
            for (String database : sqc.getDatabases()) {
                filters.add(ObjectFilters.or(ObjectFilters.elemMatch("databases", ObjectFilters.eq("$" , database))));
            }
        }

        if (CollectionUtils.isNotEmpty(sqc.getTables())) {
            for (String table : sqc.getTables()) {
                filters.add(ObjectFilters.or(ObjectFilters.elemMatch("tables", ObjectFilters.eq("$" , table))));
            }
        }

        if (criteria.getSucceeded() != null) {
            filters.add(ObjectFilters.eq("succeeded", criteria.getSucceeded()));
        }

        if (sqc.getStartExecuteTimeElapsedMillis() != null) {
            filters.add(ObjectFilters.gte("executeTimeElapsedMillis", sqc.getStartExecuteTimeElapsedMillis()));
        }

        if (sqc.getEndExecuteTimeElapsedMillis() != null) {
            filters.add(ObjectFilters.lte("executeTimeElapsedMillis", sqc.getEndExecuteTimeElapsedMillis()));
        }

        if (criteria.getStartCreatedTime() != null && criteria.getEndCreatedTime() != null) {
            filters.add(ObjectFilters.and(
                    ObjectFilters.gte("createdTime", criteria.getStartCreatedTime()),
                    ObjectFilters.lte("createdTime", criteria.getEndCreatedTime())
            ));
        }
        return filters.isEmpty() ? null : ObjectFilters.and(filters.toArray(new ObjectFilter[0]));
    }

    @Override
    protected FindOptions buildOptions(MetricsQueryCriteria criteria) {
        if (criteria.getSortOrderField() == null || StringUtils.isBlank(criteria.getSortOrderField().getFieldName()) || criteria.getSortOrderField().getSortOrder() == null) {
            criteria.setSortOrderField(new SortOrderField("createdTime", SortOrder.DESC));
        }
        return super.buildOptions(criteria);
    }
}
