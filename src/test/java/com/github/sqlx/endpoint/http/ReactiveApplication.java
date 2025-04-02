package com.github.sqlx.endpoint.http;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
        scanBasePackages = {"com.github.sqlx.endpoint.http"}
)
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class ReactiveApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ReactiveApplication.class)
                .web(WebApplicationType.REACTIVE)
                .run(args);
    }
}
