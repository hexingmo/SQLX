package io.github.sqlx.endpoint.http;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author: he peng
 * @create: 2024/11/6 15:42
 * @description:
 */

@SpringBootApplication(
        scanBasePackages = {"io.github.sqlx.endpoint.http"}
)
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class ServletApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServletApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
