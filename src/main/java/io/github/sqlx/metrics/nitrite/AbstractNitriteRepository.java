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
import io.github.sqlx.metrics.MetricsRepository;
import io.github.sqlx.metrics.Page;
import io.github.sqlx.metrics.PagingCriteria;
import io.github.sqlx.metrics.SortOrder;
import io.github.sqlx.metrics.SortOrderField;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NullOrder;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractNitriteRepository<T> implements MetricsRepository<T> {

    private final Nitrite nitrite;

    protected final ObjectRepository<T> repository;

    protected AbstractNitriteRepository(Class<T> type , String fileDirectory) {
        this.nitrite = Nitrite.builder()
                .filePath(fileDirectory + "/" + type.getSimpleName() + ".db")
                .compressed()
                .enableOffHeapStorage()
                .openOrCreate();
        this.repository = nitrite.getRepository(type);
    }

    @Override
    public String getFilePath() {
        return nitrite.getContext().getFilePath();
    }

    @Override
    public void save(T target) {
        repository.update(target , true);
    }

    @Override
    public void update(T target) {
        repository.update(target);
    }

    @Override
    public void delete(Class<T> type, Object id) {

    }

    @Override
    public Page<T> selectPage(MetricsQueryCriteria criteria) {
        Cursor<T> cursor = find(criteria);
        return Page.of(cursor.toList() , cursor.totalCount() , criteria.getPagingCriteria());
    }

    @Override
    public List<T> selectList(MetricsQueryCriteria criteria) {
        return find(criteria).toList();
    }

    @Override
    public int deleteByCreatedTimeLessThan(long timestamp) {
        if (!repository.hasIndex("createdTime")) {
            return 0;
        }
        WriteResult result = repository.remove(ObjectFilters.lt("createdTime", timestamp));
        return result.getAffectedCount();
    }

    /**
     * Builds an ObjectFilter based on the provided MetricsQueryCriteria.
     *
     * @param criteria the query criteria used to build the filter
     * @return an ObjectFilter that can be used to query metrics data
     */
    protected abstract ObjectFilter buildFilter(MetricsQueryCriteria criteria);


    protected FindOptions buildOptions(MetricsQueryCriteria criteria) {
        PagingCriteria pagingCriteria = criteria.getPagingCriteria();
        FindOptions findOptions = buildSortOrderFindOptions(criteria.getSortOrderField());
        if (findOptions == null) {
            findOptions = buildPagingFindOptions(criteria.getPagingCriteria());
        } else if (pagingCriteria != null) {
            findOptions = findOptions.thenLimit(pagingCriteria.getOffset() , pagingCriteria.getPageSize());
        }
        return findOptions;
    }

    private Cursor<T> find(MetricsQueryCriteria criteria) {
        ObjectFilter filter = buildFilter(criteria);
        FindOptions options = buildOptions(criteria);
        if (filter == null && options == null) {
            return repository.find();
        }

        if (filter != null && options == null) {
            return repository.find(filter);
        }

        if (filter == null) {
            return repository.find(options);
        }

        return repository.find(filter, options);
    }

    private FindOptions buildSortOrderFindOptions(SortOrderField sortOrderField) {
        if (sortOrderField == null) {
            return null;
        }
        org.dizitart.no2.SortOrder sortOrder;
        if (sortOrderField.getSortOrder() == SortOrder.ASC) {
            sortOrder = org.dizitart.no2.SortOrder.Ascending;
        } else {
            sortOrder = org.dizitart.no2.SortOrder.Descending;
        }
        return FindOptions.sort(sortOrderField.getFieldName(), sortOrder , NullOrder.Last);
    }

    private FindOptions buildPagingFindOptions(PagingCriteria pagingCriteria) {
        if (pagingCriteria == null) {
            return null;
        }
        return FindOptions.limit(pagingCriteria.getOffset() , pagingCriteria.getPageSize());
    }
}
