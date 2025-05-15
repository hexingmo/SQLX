//package com.github.sqlx.integration.springboot;
//
//import com.github.sqlx.integration.mybatis.AreaMapper;
//import com.github.sqlx.integration.mybatis.DepartmentMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// *
// * @author He Xing Mo
// * @since 1.0
// */
//@Service
//@Slf4j
//public class ServiceB {
//
//    @Autowired
//    AreaMapper areaMapper;
//
//    @Autowired
//    DepartmentMapper departmentMapper;
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void propagationRequiredFunc() {
//        Map<String, Object> area = new HashMap<>();
//        area.put("id" , 6);
//        area.put("name" , "Paris");
//        log.info("insert area {}" , area);
//        int areaRow = areaMapper.insert(area);
//        assertThat(areaRow).isEqualTo(1);
//    }
//
//    @Transactional(propagation = Propagation.SUPPORTS)
//    public void propagationSupportsFunc() {
//        Map<String, Object> area = new HashMap<>();
//        area.put("id" , 6);
//        area.put("name" , "Paris");
//        log.info("insert area {}" , area);
//        int areaRow = areaMapper.insert(area);
//        assertThat(areaRow).isEqualTo(1);
//    }
//
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
//    public void propagationNotSupportsFunc() {
//        Map<String, Object> dept = departmentMapper.selectById(1L);
//        assertThat(dept).isNotNull()
//                .extracting("ID" , "NAME" , "AREA_ID")
//                .containsExactlyInAnyOrder(1L , "Research and Development" , 1);
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void propagationRequiresNewFunc() {
//        Map<String, Object> area = new HashMap<>();
//        area.put("id" , 6);
//        area.put("name" , "Paris");
//        log.info("insert area {}" , area);
//        int areaRow = areaMapper.insert(area);
//        assertThat(areaRow).isEqualTo(1);
//    }
//
//    @Transactional(propagation = Propagation.NEVER)
//    public void propagationNeverFunc() {
//        Map<String, Object> area = new HashMap<>();
//        area.put("id" , 6);
//        area.put("name" , "Paris");
//        log.info("insert area {}" , area);
//        areaMapper.insert(area);
//    }
//
//    @Transactional(propagation = Propagation.NESTED)
//    public void propagationNestedFunc() {
//        Map<String, Object> area = new HashMap<>();
//        area.put("id" , 6);
//        area.put("name" , "Paris");
//        log.info("insert area {}" , area);
//        areaMapper.insert(area);
//    }
//}
