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
package com.github.sqlx.metrics.nitrite;

import com.github.sqlx.metrics.*;
import com.github.sqlx.util.StringUtils;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class NitriteRoutingMetricsRepository extends AbstractNitriteRepository<RoutingMetrics> implements RoutingMetricsRepository {

    public NitriteRoutingMetricsRepository(String fileDirectory) {
        super(RoutingMetrics.class, fileDirectory);
    }

    @Override
    protected ObjectFilter buildFilter(MetricsQueryCriteria criteria) {


        List<ObjectFilter> filters = new ArrayList<>();

        String routingId = ((RoutingMetricsQueryCriteria) criteria).getRoutingId();
        if (StringUtils.isNotBlank(routingId)) {
            filters.add(ObjectFilters.text("routingId", routingId));
        }

        if (StringUtils.isNotBlank(criteria.getSql())) {
            filters.add(ObjectFilters.text("sql", criteria.getSql()));
        }

        if (StringUtils.isNotBlank(criteria.getClusterName())) {
            filters.add(ObjectFilters.eq("clusterName", criteria.getClusterName()));
        }

        if (StringUtils.isNotBlank(criteria.getNodeName())) {
            filters.add(ObjectFilters.eq("hitNodeAttr.name" , criteria.getNodeName()));
        }

        if (StringUtils.isNotBlank(criteria.getTransactionId())) {
            filters.add(ObjectFilters.eq("transactionId", criteria.getTransactionId()));
        }

        if (StringUtils.isNotBlank(criteria.getTransactionName())) {
            filters.add(ObjectFilters.eq("transactionName", criteria.getTransactionId()));
        }

        if (criteria.getSucceeded() != null) {
            filters.add(ObjectFilters.eq("succeeded", criteria.getSucceeded()));
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
