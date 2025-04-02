package com.github.sqlx.integration.jpa;


import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;


/**
 * @author: he peng
 * @create: 2024/10/31 13:18
 * @description:
 */

@SpringBootTest(classes = {SpringBootJpaApplication.class} , properties = "spring.config.location=classpath:application-jpa.yaml")
class JpaTransactionTest {

    @Autowired
    Service service;


    @DirtiesContext
    @Test
    @Order(1)
    void jpaTransactionTest() {

        service.saveAreaAndDepartment();
    }
}
