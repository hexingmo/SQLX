package io.github.sqlx.integration.springboot.repository;

import io.github.sqlx.integration.springboot.entity.Employee;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class EmployeeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;


    public Employee getEmployee(Long id) {
        List<Employee> employees = jdbcTemplate.query("select * from employee where id = :id",
                new MapSqlParameterSource()
                        .addValue("id", id),
                new BeanPropertyRowMapper<>(Employee.class)
        );
        return Optional.of(employees)
                .filter(t -> !t.isEmpty())
                .map(t -> t.get(0))
                .orElse(null);
    }

    public void save(Employee employee) {
        String sql = "INSERT INTO employee (id, name, department_id) " +
                "VALUES (:id, :name, :departmentId)";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", employee.getId())
                .addValue("name", employee.getName())
                .addValue("departmentId", employee.getDepartmentId());

        jdbcTemplate.update(sql, parameters);
    }
}