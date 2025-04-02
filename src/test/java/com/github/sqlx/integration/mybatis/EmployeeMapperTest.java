//package com.github.sqlx.integration.mybatis;
//
//import com.github.sqlx.integration.springboot.BeforeAfterEachHandleDataTest;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * @author He Xing Mo
// * @since 1.0
// */
//class EmployeeMapperTest extends BeforeAfterEachHandleDataTest {
//
//    @Autowired
//    EmployeeMapper mapper;
//
//    @Test
//    void testInsert() {
//
//        Map<String, Object> row = new HashMap<>();
//        row.put("id" , 6);
//        row.put("name" , "He Xing Mo");
//        row.put("departmentId" , 1);
//        int rows = mapper.insert(row);
//        assertThat(rows).isEqualTo(1);
//
//        Map<String, Object> result = mapper.selectById(6L);
//        assertThat(result).isNotNull()
//                .extracting("ID" , "NAME" , "DEPARTMENT_ID")
//                .containsExactlyInAnyOrder(6L , "He Xing Mo" , 1);
//    }
//
//    @Test
//    void testUpdateById() {
//
//        Map<String, Object> row = new HashMap<>();
//        row.put("id" , 1);
//        row.put("name" , "He Xing Mo");
//        row.put("departmentId" , 567);
//        int rows = mapper.updateById(row);
//        assertThat(rows).isEqualTo(1);
//
//        Map<String, Object> result = mapper.selectById(1L);
//        assertThat(result).isNotNull()
//                .extracting("ID" , "NAME" , "DEPARTMENT_ID")
//                .containsExactlyInAnyOrder(1L , "He Xing Mo" , 567);
//    }
//
//    @Test
//    void testDelete() {
//
//        int rows = mapper.deleteById(1L);
//        assertThat(rows).isEqualTo(1);
//        Map<String, Object> result = mapper.selectById(1L);
//        assertThat(result).isNull();
//    }
//
//    @Test
//    void testSelectById() {
//
//        Map<String, Object> result = mapper.selectById(1L);
//        assertThat(result).isNotNull()
//                .extracting("ID" , "NAME" , "DEPARTMENT_ID")
//                .containsExactlyInAnyOrder(1L , "John Doe" , 1);
//    }
//}
