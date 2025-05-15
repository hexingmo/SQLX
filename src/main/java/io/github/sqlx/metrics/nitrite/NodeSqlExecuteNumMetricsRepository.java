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

import io.github.sqlx.metrics.DatasourceDashboardMetrics;
import io.github.sqlx.metrics.DatasourceDashboardMetricsQueryCriteria;
import io.github.sqlx.metrics.MetricsQueryCriteria;
import io.github.sqlx.metrics.NodeSqlExecuteNumMetrics;
import io.github.sqlx.util.CollectionUtils;
import io.github.sqlx.util.UUIDUtils;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class NodeSqlExecuteNumMetricsRepository extends AbstractNitriteRepository<NodeSqlExecuteNumMetrics> {


    public NodeSqlExecuteNumMetricsRepository(String fileDirectory) {
        super(NodeSqlExecuteNumMetrics.class, fileDirectory);
    }

    @Override
    protected ObjectFilter buildFilter(MetricsQueryCriteria criteria) {

        return null;
    }

    @Override
    public synchronized void save(NodeSqlExecuteNumMetrics target) {
        ObjectFilter filter = ObjectFilters.and(ObjectFilters.eq("nodeName", target.getNodeName()),
                ObjectFilters.eq("timestamp", target.getTimestamp()));
        Cursor<NodeSqlExecuteNumMetrics> cursor = repository.find(filter);
        NodeSqlExecuteNumMetrics metrics = cursor.firstOrDefault();
        if (Objects.isNull(metrics)) {
            target.setId(UUIDUtils.getSimpleUUID());
            repository.insert(target);
        } else {
            metrics.setInsertCount(metrics.getInsertCount() + target.getInsertCount())
                    .setUpdateCount(metrics.getUpdateCount() + target.getUpdateCount())
                    .setDeleteCount(metrics.getDeleteCount() + target.getDeleteCount())
                    .setSelectCount(metrics.getSelectCount() + target.getSelectCount())
                    .setOtherCount(metrics.getOtherCount() + target.getOtherCount());
            repository.update(metrics);
        }
    }

    public List<DatasourceDashboardMetrics> selectDatasourceDashboardMetrics(DatasourceDashboardMetricsQueryCriteria criteria) {

        List<ObjectFilter> filters = new ArrayList<>();
        filters.add(ObjectFilters.in("nodeName", criteria.getDatasourceList().toArray()));
        filters.add(ObjectFilters.and(
                ObjectFilters.gte("timestamp", criteria.getStart()),
                ObjectFilters.lte("timestamp", criteria.getEnd())));

        ObjectFilter filter = ObjectFilters.and(filters.toArray(new ObjectFilter[0]));
        Cursor<NodeSqlExecuteNumMetrics> cursor = repository.find(filter);
        List<DatasourceDashboardMetrics> metricsList = new ArrayList<>();
        List<NodeSqlExecuteNumMetrics> cursorList = cursor.toList();
        if (CollectionUtils.isNotEmpty(cursorList)) {
            Map<String, List<NodeSqlExecuteNumMetrics>> grouped = cursorList.stream().collect(Collectors.groupingBy(NodeSqlExecuteNumMetrics::getNodeName));
            for (String nodeName : criteria.getDatasourceList()) {
                List<NodeSqlExecuteNumMetrics> value = grouped.get(nodeName);
                if (CollectionUtils.isNotEmpty(value)) {
                    value.sort(Comparator.comparingLong(NodeSqlExecuteNumMetrics::getTimestamp));
                } else {
                    value = new ArrayList<>();
                }
                DatasourceDashboardMetrics datasourceDashboardMetrics = new DatasourceDashboardMetrics()
                        .setDataSource(nodeName)
                        .setMetrics(value);
                metricsList.add(datasourceDashboardMetrics);
            }
        } else {
            for (String nodeName : criteria.getDatasourceList()) {
                DatasourceDashboardMetrics datasourceDashboardMetrics = new DatasourceDashboardMetrics()
                        .setDataSource(nodeName)
                        .setMetrics(new ArrayList<>());
                metricsList.add(datasourceDashboardMetrics);
            }
        }
        return metricsList;
    }
}
