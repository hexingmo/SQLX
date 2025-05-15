package io.github.sqlx.integration.springboot.test;

import io.github.sqlx.integration.springboot.BeforeAfterEachHandleDataTest;
import io.github.sqlx.integration.springboot.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * sql routing annotation test
 *
 * @author jing yun
 * @since 1.0
 */
public class SqlRoutingAnnotationTest extends BeforeAfterEachHandleDataTest {

    @Autowired
    private SqlRoutingAnnotationTestService testService;

    @Test
    public void routing_to_write_0() {
        Employee employee = new Employee()
                .setId(new Date().getTime())
                .setName("Walt Disney")
                .setDepartmentId(999L);
        testService.saveEmployeeToWrite0(employee);

        Employee fetchedEmployee = testService.getEmployeeFromRead0(employee.getId());
        assertThat(fetchedEmployee).isNull();

        fetchedEmployee = testService.getEmployeeFromWrite0(employee.getId());
        assertThat(fetchedEmployee).isNotNull();
        assertThat(fetchedEmployee.getId()).isEqualTo(employee.getId());
        assertThat(fetchedEmployee.getName()).isEqualTo(employee.getName());
        assertThat(fetchedEmployee.getDepartmentId()).isEqualTo(employee.getDepartmentId());
    }

    @Test
    public void routing_to_cluster_0() {
        Employee employee = new Employee()
                .setId(new Date().getTime())
                .setName("Albert Einstein")
                .setDepartmentId(999L);
        testService.saveEmployeeToCluster0(employee);

        Employee fetchedEmployee = testService.getEmployeeFromRead0(employee.getId());
        assertThat(fetchedEmployee).isNull();

        fetchedEmployee = testService.getEmployeeFromCluster0Write0(employee.getId());
        assertThat(fetchedEmployee).isNotNull();
        assertThat(fetchedEmployee.getId()).isEqualTo(employee.getId());
        assertThat(fetchedEmployee.getName()).isEqualTo(employee.getName());
        assertThat(fetchedEmployee.getDepartmentId()).isEqualTo(employee.getDepartmentId());
    }

}
