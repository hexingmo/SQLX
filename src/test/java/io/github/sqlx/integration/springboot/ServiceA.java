//package com.github.sqlx.integration.springboot;
//
//import com.github.sqlx.integration.mybatis.DepartmentMapper;
//import com.github.sqlx.integration.mybatis.EmployeeMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.ColumnMapRowMapper;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.tuple;
//
///**
// *
// * @author He Xing Mo
// * @since 1.0
// */
//
//@Service
//@Slf4j
//public class ServiceA {
//
//    @Autowired
//    DepartmentMapper departmentMapper;
//
//    @Autowired
//    EmployeeMapper employeeMapper;
//
//    @Autowired
//    JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    ServiceB serviceB;
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void outerRequiredInnerRequired() {
//
//        Map<String, Object> dept = new HashMap<>();
//        dept.put("id" , 6);
//        dept.put("name" , "After-sales Service");
//        dept.put("areaId" , 6);
//        log.info("insert department {}" , dept);
//        int deptRow = departmentMapper.insert(dept);
//        assertThat(deptRow).isEqualTo(1);
//
//        serviceB.propagationRequiredFunc();
//
//
//        Map<String, Object> employee = new HashMap<>();
//        employee.put("id" , 6);
//        employee.put("name" , "He Xing Mo");
//        employee.put("departmentId" , 6);
//        log.info("insert employee {}" , dept);
//        int employeeRow = employeeMapper.insert(employee);
//        assertThat(employeeRow).isEqualTo(1);
//
//        String sql = "SELECT a.name AS area_name, d.name AS department_name, e.name AS employee_name\n" +
//                "FROM area a\n" +
//                "JOIN department d ON a.id = d.area_id\n" +
//                "JOIN employee e ON d.id = e.department_id\n" +
//                "WHERE a.id = ?";
//
//        List<Map<String, Object>> results = jdbcTemplate.query(sql, new Object[]{6}, new ColumnMapRowMapper());
//        assertThat(results)
//                .extracting("AREA_NAME", "DEPARTMENT_NAME" , "EMPLOYEE_NAME")
//                .containsExactlyInAnyOrder(
//                        tuple("Paris", "After-sales Service", "He Xing Mo")
//                );
//
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void outerRequiredInnerSupports() {
//
//        Map<String, Object> dept = new HashMap<>();
//        dept.put("id" , 6);
//        dept.put("name" , "After-sales Service");
//        dept.put("areaId" , 6);
//        log.info("insert department {}" , dept);
//        int deptRow = departmentMapper.insert(dept);
//        assertThat(deptRow).isEqualTo(1);
//
//        serviceB.propagationSupportsFunc();
//
//
//        Map<String, Object> employee = new HashMap<>();
//        employee.put("id" , 6);
//        employee.put("name" , "He Xing Mo");
//        employee.put("departmentId" , 6);
//        log.info("insert employee {}" , employee);
//        int employeeRow = employeeMapper.insert(employee);
//        assertThat(employeeRow).isEqualTo(1);
//
//        String sql = "SELECT a.name AS area_name, d.name AS department_name, e.name AS employee_name\n" +
//                "FROM area a\n" +
//                "JOIN department d ON a.id = d.area_id\n" +
//                "JOIN employee e ON d.id = e.department_id\n" +
//                "WHERE a.id = ?";
//
//        List<Map<String, Object>> results = jdbcTemplate.query(sql, new Object[]{6}, new ColumnMapRowMapper());
//        assertThat(results)
//                .extracting("AREA_NAME", "DEPARTMENT_NAME" , "EMPLOYEE_NAME")
//                .containsExactlyInAnyOrder(
//                        tuple("Paris", "After-sales Service", "He Xing Mo")
//                );
//
//    }
//
//    @Transactional(propagation = Propagation.MANDATORY)
//    public void propagationMandatory() {
//        Map<String, Object> employee = new HashMap<>();
//        employee.put("id" , 6);
//        employee.put("name" , "He Xing Mo");
//        employee.put("departmentId" , 6);
//        log.info("insert employee {}" , employee);
//        employeeMapper.insert(employee);
//    }
//
//
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void outerRequiredInnerNotSupports() {
//        Map<String, Object> employee = new HashMap<>();
//        employee.put("id" , 6);
//        employee.put("name" , "He Xing Mo");
//        employee.put("departmentId" , 6);
//        log.info("insert employee {}" , employee);
//        int employeeRow = employeeMapper.insert(employee);
//        assertThat(employeeRow).isEqualTo(1);
//
//        Map<String, Object> dept = new HashMap<>();
//        dept.put("id" , 6);
//        dept.put("name" , "After-sales Service");
//        dept.put("areaId" , 6);
//        log.info("insert department {}" , dept);
//        int deptRow = departmentMapper.insert(dept);
//        assertThat(deptRow).isEqualTo(1);
//
//
//        serviceB.propagationNotSupportsFunc();
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void outerRequiredInnerRequiresNew() {
//        Map<String, Object> employee = new HashMap<>();
//        employee.put("id" , 6);
//        employee.put("name" , "He Xing Mo");
//        employee.put("departmentId" , 6);
//        log.info("insert employee {}" , employee);
//        int employeeRow = employeeMapper.insert(employee);
//        assertThat(employeeRow).isEqualTo(1);
//
//        Map<String, Object> dept = new HashMap<>();
//        dept.put("id" , 6);
//        dept.put("name" , "After-sales Service");
//        dept.put("areaId" , 6);
//        log.info("insert department {}" , dept);
//        int deptRow = departmentMapper.insert(dept);
//        assertThat(deptRow).isEqualTo(1);
//
//
//        serviceB.propagationRequiresNewFunc();
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void outerRequiredInnerNever() {
//        Map<String, Object> employee = new HashMap<>();
//        employee.put("id" , 6);
//        employee.put("name" , "He Xing Mo");
//        employee.put("departmentId" , 6);
//        log.info("insert employee {}" , employee);
//        int employeeRow = employeeMapper.insert(employee);
//        assertThat(employeeRow).isEqualTo(1);
//
//        serviceB.propagationNeverFunc();
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void outerRequiredInnerNested() {
//        Map<String, Object> employee = new HashMap<>();
//        employee.put("id" , 6);
//        employee.put("name" , "He Xing Mo");
//        employee.put("departmentId" , 6);
//        log.info("insert employee {}" , employee);
//        int employeeRow = employeeMapper.insert(employee);
//        assertThat(employeeRow).isEqualTo(1);
//
//        serviceB.propagationNestedFunc();
//    }
//}
