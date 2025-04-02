package com.github.sqlx.integration.springboot;

import lombok.Getter;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;

/**
 * @author: he peng
 * @create: 2024/12/22 11:08
 * @description:
 */
public class TransactionIdSynchronization implements TransactionSynchronization, Ordered {

    @Getter
    private final String transactionId;

    public TransactionIdSynchronization(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }


}
