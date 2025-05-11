package com.github.sqlx.integration.springboot.test;

import com.github.sqlx.annotation.SqlRouting;
import com.github.sqlx.integration.springboot.entity.Employee;
import com.github.sqlx.integration.springboot.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * sql routing annotation test service
 *
 * @author jing yun
 * @since 1.0
 */
@Service
@AllArgsConstructor
public class SqlRoutingAnnotationTestService {

    private final EmployeeRepository employeeRepository;


    @SqlRouting(nodes = "write_0")
    public void saveEmployeeToWrite0(Employee employee) {
        employeeRepository.save(employee);
    }

    @SqlRouting(cluster = "cluster_0")
    public void saveEmployeeToCluster0(Employee employee) {
        employeeRepository.save(employee);
    }

    @SqlRouting(cluster = "cluster_0", nodes = "write_0")
    public Employee getEmployeeFromCluster0Write0(Long id) {
        return employeeRepository.getEmployee(id);
    }

    @SqlRouting(nodes = "read_0")
    public Employee getEmployeeFromRead0(Long id) {
        return employeeRepository.getEmployee(id);
    }

    @SqlRouting(nodes = "write_0")
    public Employee getEmployeeFromWrite0(Long id) {
        return employeeRepository.getEmployee(id);
    }


}
