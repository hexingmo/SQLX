package com.github.sqlx.integration.springboot;

import com.github.sqlx.RoutingContext;
import com.github.sqlx.annotation.SqlRouting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: he peng
 * @create: 2024/10/29 11:41
 * @description:
 */

@Service
@Slf4j
public class SqlRoutingPropagationServiceA {

    @Autowired
    SqlRoutingPropagationServiceB serviceB;
    
    
    @Autowired
    JdbcTemplate jdbcTemplate;


    @SqlRouting(cluster = "cluster_0" , nodes = {"write_0" , "read_0"})
    public void propagation() {
        log.info("propagation run ...");

        String sql1 = "select * from employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql1 , new Object[] {1} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(1L);
        assertThat(result).extractingByKey("NAME").isEqualTo("John Doe");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);

        String sql2 = "insert into employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql2, ps -> {
            ps.setLong(1 , 5L);
            ps.setString(2 , "He Xing Mo");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0");
        assertThat(ra).extracting("propagation").isEqualTo(true);

        serviceB.propagation();

        ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0");
        assertThat(ra).extracting("propagation").isEqualTo(true);
    }

    @SqlRouting(cluster = "cluster_0" , nodes = {"write_0" , "read_0"})
    public void notPropagation() {
        log.info("notPropagation run ...");
        String sql1 = "select * from employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql1 , new Object[] {1} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(1L);
        assertThat(result).extractingByKey("NAME").isEqualTo("John Doe");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);

        String sql2 = "insert into employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql2, ps -> {
            ps.setLong(1 , 5L);
            ps.setString(2 , "He Xing Mo");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0");
        assertThat(ra).extracting("propagation").isEqualTo(true);

        serviceB.notPropagation();

        ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0");
        assertThat(ra).extracting("propagation").isEqualTo(true);
    }

    public void expressionPropagation() {
        log.info("expressionPropagation run ...");

        String sql1 = "select * from employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql1 , new Object[] {1} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(1L);
        assertThat(result).extractingByKey("NAME").isEqualTo("John Doe");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);

        String sql2 = "insert into employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql2, ps -> {
            ps.setLong(1 , 5L);
            ps.setString(2 , "He Xing Mo");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0" , "read_1");
        assertThat(ra).extracting("propagation").isEqualTo(true);

        serviceB.propagation();

        ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0", "read_1");
        assertThat(ra).extracting("propagation").isEqualTo(true);
    }

    @Transactional
    @SqlRouting(cluster = "cluster_0")
    public void transactionMethod() {
        String sql1 = "select * from employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql1 , new Object[] {1} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(1L);
        assertThat(result).extractingByKey("NAME").isEqualTo("John Doe");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);

        String sql2 = "insert into employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql2, ps -> {
            ps.setLong(1 , 5L);
            ps.setString(2 , "He Xing Mo");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);

        serviceB.newTransactionMethod();

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0");
        assertThat(ra).extracting("propagation").isEqualTo(true);
    }
}
