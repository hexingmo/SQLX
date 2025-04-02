package com.github.sqlx.integration.springboot;

import org.springframework.boot.SpringApplication;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@org.springframework.boot.autoconfigure.SpringBootApplication(
        scanBasePackages = {"com.github.sqlx.integration.springboot" , "com.github.sqlx.integration.mybatis"}
)
public class SpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApplication.class, args);
    }
}
