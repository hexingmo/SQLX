package com.github.sqlx.datasource.dbcp2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.locks.LockSupport;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@SpringBootApplication(
        scanBasePackages = {"com.github.devx.sql.routing.datasource.dbcp2"}
)
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class Dbcp2DataSourceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Dbcp2DataSourceApplication.class, args);
        LockSupport.park(Thread.currentThread());
    }
}
