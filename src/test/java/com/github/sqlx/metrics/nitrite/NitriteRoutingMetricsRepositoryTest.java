package com.github.sqlx.metrics.nitrite;

import com.github.sqlx.metrics.Page;
import com.github.sqlx.metrics.PagingCriteria;
import com.github.sqlx.metrics.RoutingMetrics;
import com.github.sqlx.metrics.RoutingMetricsQueryCriteria;
import com.github.sqlx.metrics.SortOrder;
import com.github.sqlx.metrics.SortOrderField;
import com.github.sqlx.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author: he peng
 * @create: 2024/12/26 14:47
 * @description:
 */
@Slf4j
class NitriteRoutingMetricsRepositoryTest {

    final NitriteRoutingMetricsRepository repository = new NitriteRoutingMetricsRepository("C:\\Users\\Administrator\\Desktop\\test");

    @Test
    void deleteByCreatedTimeLessThan() throws Exception {

        int row = repository.deleteByCreatedTimeLessThan(System.currentTimeMillis());
        System.out.println(row);
    }


    @Test
    void selectList() {
        RoutingMetricsQueryCriteria criteria = new RoutingMetricsQueryCriteria();
        //criteria.setSql("select * from employee where id = 1");
        //criteria.setClusterName("cluster_0");
        //criteria.setClusterName("cluster_1");
        criteria.setNodeName("read_0");
        //criteria.setStartCreatedTime(LocalDateTime.now().minusMinutes(5));
        //criteria.setEndCreatedTime(LocalDateTime.now());
        List<RoutingMetrics> metricsList = repository.selectList(criteria);
        log.info("RoutingMetrics List {}" , JsonUtils.toJson(metricsList));
    }

    @Test
    void selectPage() {
        RoutingMetricsQueryCriteria criteria = new RoutingMetricsQueryCriteria();
        criteria.setPagingCriteria(new PagingCriteria(1, 3));
        criteria.setSortOrderField(new SortOrderField("createdTime"  , SortOrder.DESC));
        //criteria.setSql("select * from employee where id = 1");
        //criteria.setClusterName("cluster_0");
        //criteria.setClusterName("cluster_1");
        //criteria.setNodeName("read_0");
        //criteria.setStartCreatedTime(LocalDateTime.now().minusMinutes(5));
        //criteria.setEndCreatedTime(LocalDateTime.now());
        Page<RoutingMetrics> page = repository.selectPage(criteria);
        log.info("RoutingMetrics page {}" , JsonUtils.toJson(page));
    }
}