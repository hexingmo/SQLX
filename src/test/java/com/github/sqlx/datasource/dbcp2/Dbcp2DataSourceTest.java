package com.github.sqlx.datasource.dbcp2;

import com.github.sqlx.jdbc.datasource.DatasourceManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.locks.LockSupport;

/**
 * @author: he peng
 * @create: 2024/11/1 15:22
 * @description:
 */

@SpringBootTest(classes = {Dbcp2DataSourceApplication.class} , properties = "spring.config.location=classpath:application-dbcp2.yaml")
class Dbcp2DataSourceTest {

    @Autowired
    DatasourceManager datasourceManager;


}
