package io.github.sqlx.integration.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: he peng
 * @create: 2024/10/29 11:50
 * @description:
 */
class SqlRoutingPropagationTest extends BeforeAfterEachHandleDataTest {

    @Autowired
    SqlRoutingPropagationServiceA serviceA;

    @Test
    void propagation() {
        serviceA.propagation();
    }

    @Test
    void notPropagation() {
        serviceA.notPropagation();
    }

    @Test
    void expressionPropagation() {
        serviceA.expressionPropagation();
    }

    @Test
    void transactionMethod() {
        serviceA.transactionMethod();
    }
}
