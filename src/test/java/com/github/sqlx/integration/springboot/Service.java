package com.github.sqlx.integration.springboot;

import com.github.sqlx.annotation.SqlRouting;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author He Xing Mo
 * @since 1.1
 */

@org.springframework.stereotype.Service
@AllArgsConstructor
public class Service {

    private final JdbcTemplate jdbcTemplate;

    private final ApplicationContext appContext;

    @SqlRouting(nodes = "write_0")
    public String func_0() {

        String sql1 = "select * from employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql1 , new Object[] {1} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(1L);
        assertThat(result).extractingByKey("NAME").isEqualTo("John Doe");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);

        String sql2 = "insert into employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql2, ps -> {
            ps.setLong(1 , RandomUtils.nextLong());
            ps.setString(2 , "He Xing Mo");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);
        Service proxy = appContext.getBean(Service.class);
        return proxy.func_1();
    }

    @SqlRouting(nodes = "read_0")
    public String func_1() {

        String sql1 = "select * from employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql1 , new Object[] {1} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(1L);
        assertThat(result).extractingByKey("NAME").isEqualTo("John Doe");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);

        String sql2 = "insert into employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql2, ps -> {
            ps.setLong(1 , RandomUtils.nextLong());
            ps.setString(2 , "He Xing Mo");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);
        return "ok";
    }



    @SqlRouting(nodes = "write_0")
    public String noTxFunc() {

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
        return "ok";
    }

    @SqlRouting(nodes = "write_0")
    @Transactional(rollbackFor = Throwable.class)
    public String txFunc() {

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
        return "ok";
    }

    @Transactional(rollbackFor = Throwable.class , isolation = Isolation.READ_COMMITTED)
    public String txFunc1() {
        return "ok";
    }
}
