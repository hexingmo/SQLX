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
import io.github.sqlx.metrics.TransactionMetrics;
import io.github.sqlx.metrics.TransactionMetricsQueryCriteria;
import io.github.sqlx.metrics.TransactionMetricsRepository;
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
public class NitriteTransactionMetricsRepository extends AbstractNitriteRepository<TransactionMetrics> implements TransactionMetricsRepository {

    public NitriteTransactionMetricsRepository(String fileDirectory) {
        super(TransactionMetrics.class, fileDirectory);
    }

    @Override
    protected ObjectFilter buildFilter(MetricsQueryCriteria criteria) {

        TransactionMetricsQueryCriteria tmqc = (TransactionMetricsQueryCriteria) criteria;
        List<ObjectFilter> filters = new ArrayList<>();

        if (StringUtils.isNotBlank(criteria.getTransactionId())) {
            filters.add(ObjectFilters.eq("transactionId", criteria.getTransactionId()));
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
            filters.add(ObjectFilters.eq("transactionName", criteria.getTransactionName()));
        }

        if (CollectionUtils.isNotEmpty(tmqc.getDatabases())) {
            for (String database : tmqc.getDatabases()) {
                filters.add(ObjectFilters.or(ObjectFilters.elemMatch("databases", ObjectFilters.eq("$" , database))));
            }
        }

        if (CollectionUtils.isNotEmpty(tmqc.getTables())) {
            for (String table : tmqc.getTables()) {
                filters.add(ObjectFilters.or(ObjectFilters.elemMatch("tables", ObjectFilters.eq("$" , table))));
            }
        }

        if (tmqc.getTransactionStatus() != null) {
            filters.add(ObjectFilters.eq("transactionStatus", tmqc.getTransactionStatus()));
        }

        if (criteria.getSucceeded() != null) {
            filters.add(ObjectFilters.eq("succeeded", criteria.getSucceeded()));
        }


        if (tmqc.getStartTotalTimeElapsedMillis() != null) {
            filters.add(ObjectFilters.gte("totalTimeElapsedMillis", tmqc.getStartTotalTimeElapsedMillis()));
        }

        if (tmqc.getEndTotalTimeElapsedMillis() != null) {
            filters.add(ObjectFilters.lte("totalTimeElapsedMillis", tmqc.getEndTotalTimeElapsedMillis()));
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
