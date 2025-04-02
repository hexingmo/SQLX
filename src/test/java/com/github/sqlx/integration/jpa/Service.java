package com.github.sqlx.integration.jpa;

import com.github.sqlx.RoutingContext;
import com.github.sqlx.annotation.SqlRouting;
import com.github.sqlx.integration.jpa.dao.AreaRepository;
import com.github.sqlx.integration.jpa.dao.DepartmentRepository;
import com.github.sqlx.integration.jpa.entity.AreaEntity;
import com.github.sqlx.integration.jpa.entity.DepartmentEntity;
import com.github.sqlx.integration.springboot.RouteAttribute;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: he peng
 * @create: 2024/10/31 15:08
 * @description:
 */

@Component
public class Service {

    @Autowired
    AreaRepository areaRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @SqlRouting(cluster = "cluster_0")
    @Transactional(rollbackFor = Throwable.class)
    public void saveAreaAndDepartment() {
        AreaEntity area = new AreaEntity();
        area.setId(1L);
        area.setName("Kilimanjaro");
        areaRepository.save(area);

        DepartmentEntity department = new DepartmentEntity();
        department.setId(1L);
        department.setName("R&D");
        department.setAreaId(area.getId());
        departmentRepository.save(department);

        Service proxy = (Service) AopContext.currentProxy();
        List<AreaEntity> areas = proxy.findAllArea();
        assertThat(areas).isEmpty();

        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        assertThat(ra).isNotNull()
                .extracting("cluster").isEqualTo("cluster_0");
        assertThat(ra).extracting("nodes").asList().contains("write_0" , "read_0");
        assertThat(ra).extracting("propagation").isEqualTo(true);
    }

    @SqlRouting(cluster = "cluster_0" , propagation = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<AreaEntity> findAllArea() {
        return areaRepository.findAll();
    }
}
