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

import com.github.sqlx.metrics.MetricsQueryCriteria;
import com.github.sqlx.metrics.TableAccessMetrics;
import com.github.sqlx.metrics.TableAccessMetricsRepository;
import com.github.sqlx.metrics.TableMetricsQueryCriteria;
import com.github.sqlx.util.CollectionUtils;
import com.github.sqlx.util.TimeUtils;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class NitriteTableAccessMetricsRepository extends AbstractNitriteRepository<TableAccessMetrics> implements TableAccessMetricsRepository {

    public NitriteTableAccessMetricsRepository(String fileDirectory) {
        super(TableAccessMetrics.class, fileDirectory);
    }

    @Override
    public synchronized void save(TableAccessMetrics target) {
        long timeMillis = System.currentTimeMillis();
        Cursor<TableAccessMetrics> cursor = repository.find(ObjectFilters.eq("fullTableName", target.getFullTableName()));
        TableAccessMetrics metrics = cursor.firstOrDefault();
        if (Objects.isNull(metrics)) {
            target.setUpdatedTime(timeMillis);
            repository.insert(target);
        } else {
            long queryCount = metrics.getQueryCount() + target.getQueryCount();
            long insertCount = metrics.getInsertCount() + target.getInsertCount();
            long updateCount = metrics.getUpdateCount() + target.getUpdateCount();
            long deleteCount = metrics.getDeleteCount() + target.getDeleteCount();
            long readCount = metrics.getReadCount() + target.getReadCount();
            long writeCount = metrics.getWriteCount() + target.getWriteCount();
            long totalCount = metrics.getTotalCount() + target.getTotalCount();
            metrics.setQueryCount(queryCount)
                    .setInsertCount(insertCount)
                    .setUpdateCount(updateCount)
                    .setDeleteCount(deleteCount)
                    .setReadCount(readCount)
                    .setWriteCount(writeCount)
                    .setTotalCount(totalCount)
                    .setUpdatedTime(timeMillis);
            repository.update(metrics);
        }
    }

    @Override
    protected ObjectFilter buildFilter(MetricsQueryCriteria criteria) {

        TableMetricsQueryCriteria tmqc = (TableMetricsQueryCriteria) criteria;
        List<ObjectFilter> filters = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(tmqc.getDatabases())) {
            filters.add(ObjectFilters.in("database" , tmqc.getDatabases().toArray()));
        }

        if (CollectionUtils.isNotEmpty(tmqc.getTables())) {
            filters.add(ObjectFilters.in("table" , tmqc.getTables().toArray()));
        }

        if (tmqc.getStartUpdatedTime() != null && tmqc.getEndUpdatedTime() != null) {
            filters.add(ObjectFilters.and(
                    ObjectFilters.gte("updatedTime", tmqc.getStartUpdatedTime()),
                    ObjectFilters.lte("updatedTime", tmqc.getEndUpdatedTime())
            ));
        }
        return filters.isEmpty() ? null : ObjectFilters.and(filters.toArray(new ObjectFilter[0]));
    }
}
