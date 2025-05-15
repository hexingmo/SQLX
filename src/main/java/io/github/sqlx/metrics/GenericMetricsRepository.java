package io.github.sqlx.metrics;

import io.github.sqlx.exception.SqlXRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class GenericMetricsRepository implements MetricsRepository<Object> {

    public static final Map<Class<?> , MetricsRepository> METRICS_DAO_CACHE = new HashMap<>();

    public void registerRepository(Class<?> type , MetricsRepository dao) {
        METRICS_DAO_CACHE.put(type , dao);
    }

    @Override
    public void save(Object target) {
        getMetricsRepository(target.getClass()).save(target);
    }

    @Override
    public void update(Object target) {
        getMetricsRepository(target.getClass()).update(target);
    }

    @Override
    public void delete(Class<Object> type , Object id) {
        getMetricsRepository(type).delete(type , id);
    }

    @Override
    public String getFilePath() {
        throw new UnsupportedOperationException("Not supported getFilePath method");
    }

    @Override
    public Page<Object> selectPage(MetricsQueryCriteria criteria) {
        throw new UnsupportedOperationException("Not supported selectPage method");
    }

    @Override
    public List<Object> selectList(MetricsQueryCriteria criteria) {
        throw new UnsupportedOperationException("Not supported selectList method");
    }

    @Override
    public int deleteByCreatedTimeLessThan(long timestamp) {
        throw new UnsupportedOperationException("Not supported deleteByCreatedTimeLessThan method");
    }

    private MetricsRepository getMetricsRepository(Class<?> type) {
        MetricsRepository repository = METRICS_DAO_CACHE.get(type);
        if (repository == null) {
            throw new SqlXRuntimeException("No MetricsRepository found for type: " + type);
        }
        return repository;
    }
}
