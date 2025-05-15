package io.github.sqlx.integration.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@ActiveProfiles("database-routing-rule")
class DatabaseRouteRuleTest extends BeforeAfterEachHandleDataTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseRoutingRule() {

        String sql1 = "insert into test1.employee (id , name , department_id) values (? , ? , ?)";
        int row = jdbcTemplate.update(sql1, ps -> {
            ps.setLong(1 , 5L);
            ps.setString(2 , "He Xing Mo");
            ps.setInt(3 , 1);
        });
        assertThat(row).isEqualTo(1);

        String sql2 = "select * from test1.employee where id = ?";
        Map<String, Object> result = jdbcTemplate.queryForObject(sql2 , new Object[] {5L} ,new ColumnMapRowMapper());
        assertThat(result).isNotNull().extractingByKey("ID").isEqualTo(5L);
        assertThat(result).extractingByKey("NAME").isEqualTo("He Xing Mo");
        assertThat(result).extractingByKey("DEPARTMENT_ID").isEqualTo(1);
    }
}
