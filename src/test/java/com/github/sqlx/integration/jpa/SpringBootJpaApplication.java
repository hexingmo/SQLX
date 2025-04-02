package com.github.sqlx.integration.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@SpringBootApplication(
        scanBasePackages = {"com.github.sqlx.integration.jpa"}
)
@EnableJpaRepositories(basePackages = "com.github.sqlx.integration.jpa.dao")
@EntityScan(basePackages = "com.github.sqlx.integration.jpa.entity")
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class SpringBootJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootJpaApplication.class, args);
    }
}
