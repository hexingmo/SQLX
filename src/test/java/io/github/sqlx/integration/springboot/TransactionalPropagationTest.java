//package com.github.sqlx.integration.springboot;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.IllegalTransactionStateException;
//import org.springframework.transaction.NestedTransactionNotSupportedException;
//import org.springframework.transaction.support.AbstractPlatformTransactionManager;
//
///**
// * testing Transactional Propagation
// *
// * @author He Xing Mo
// * @since 1.0
// */
//class TransactionalPropagationTest extends BeforeAfterEachHandleDataTest {
//
//    @Autowired
//    ServiceA serviceA;
//
//    @Autowired
//    AbstractPlatformTransactionManager transactionManager;
//
//    @Test
//    void outerRequiredInnerRequired() {
//        serviceA.outerRequiredInnerRequired();
//    }
//
//    @Test
//    void outerRequiredInnerSupports() {
//        serviceA.outerRequiredInnerSupports();
//    }
//
//    @Test
//    void propagationMandatory() {
//        Assertions.assertThrows(IllegalTransactionStateException.class, () -> serviceA.propagationMandatory());
//    }
//
//
//    @Test
//    void outerRequiredInnerNotSupports() {
//        serviceA.outerRequiredInnerNotSupports();
//    }
//
//    @Test
//    void outerRequiredInnerRequiresNew() {
//        serviceA.outerRequiredInnerRequiresNew();
//    }
//
//    @Test
//    void outerRequiredInnerNever() {
//        Assertions.assertThrows(IllegalTransactionStateException.class, () -> serviceA.outerRequiredInnerNever());
//    }
//
//    @Test
//    void outerRequiredInnerNested() {
//        boolean nestedTransactionAllowed = transactionManager.isNestedTransactionAllowed();
//        if (!nestedTransactionAllowed) {
//            Assertions.assertThrows(NestedTransactionNotSupportedException.class, () -> serviceA.outerRequiredInnerNested());
//        } else {
//            serviceA.outerRequiredInnerNested();
//        }
//    }
//}
