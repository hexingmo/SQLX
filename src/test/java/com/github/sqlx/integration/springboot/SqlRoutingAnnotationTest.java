package com.github.sqlx.integration.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author He Xing Mo
 * @since 1.1
 */
class SqlRoutingAnnotationTest extends BeforeAfterEachHandleDataTest {

    @Autowired
    Service service;

    @Test
    void testSqlRoutingFuncNestedCall() {

        service.func_0();
    }



    @Test
    void testSqlRoutingAnnotationNoTx() {

        service.noTxFunc();
    }

    @Test
    void testSqlRoutingAnnotationTx() {

        service.txFunc();
    }

    @Test
    void testTxFunc1() {

        service.txFunc1();
    }
}
