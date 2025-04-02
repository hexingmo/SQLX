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
package com.github.sqlx.metrics;

import java.util.List;

/**
 * Repository interface for managing metrics data.
 *
 * @param <T> the type of the metrics entity
 * @author He Xing Mo
 * @since 1.0
 */
public interface MetricsRepository<T> {

    /**
     * Retrieves the file path associated with the metrics repository.
     *
     * @return the file path as a String
     */
    String getFilePath();

    /**
     * Saves a new metrics entity.
     *
     * @param target the metrics entity to save
     */
    void save(T target);

    /**
     * Updates an existing metrics entity.
     *
     * @param target the metrics entity to update
     */
    void update(T target);

    /**
     * Deletes a metrics entity by its type and ID.
     *
     * @param type the class type of the metrics entity
     * @param id the ID of the metrics entity to delete
     */
    void delete(Class<T> type, Object id);

    /**
     * Selects a page of metrics entities based on the given criteria.
     *
     * @param criteria the query criteria for selecting metrics entities
     * @return a page of metrics entities
     */
    Page<T> selectPage(MetricsQueryCriteria criteria);

    /**
     * Selects a list of metrics entities based on the given criteria.
     *
     * @param criteria the query criteria for selecting metrics entities
     * @return a list of metrics entities
     */
    List<T> selectList(MetricsQueryCriteria criteria);

    /**
     * Deletes metrics entities created before the specified timestamp.
     *
     * @param timestamp the timestamp before which metrics entities should be deleted
     * @return the number of metrics entities deleted
     */
    int deleteByCreatedTimeLessThan(long timestamp);

}
