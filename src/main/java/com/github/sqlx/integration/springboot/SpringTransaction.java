/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.sqlx.integration.springboot;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.jdbc.transaction.TransactionIdGenerator;
import com.github.sqlx.sql.SqlAttribute;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class SpringTransaction implements Transaction {

    private final TransactionIdGenerator transactionIdGenerator;

    public SpringTransaction(TransactionIdGenerator transactionIdGenerator) {
        this.transactionIdGenerator = transactionIdGenerator;
    }

    @Override
    public boolean isActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public boolean isReadOnly() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    @Override
    public void registerNode(NodeAttribute node, SqlAttribute sqlAttr) {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        Optional<TransactionSynchronization> optional = synchronizations.stream()
                .filter(NodeSynchronization.class::isInstance).findFirst();
        if (!optional.isPresent()) {
            NodeSynchronization rns = new NodeSynchronization(node);
            rns.addSqlAttr(sqlAttr);
            TransactionSynchronizationManager.registerSynchronization(rns);
            TransactionSynchronizationManager.registerSynchronization(new TransactionIdSynchronization(transactionIdGenerator.getTransactionId()));
        } else {
            NodeSynchronization rns = (NodeSynchronization) optional.get();
            rns.addSqlAttr(sqlAttr);
        }
    }

    @Override
    public void addSql(SqlAttribute sqlAttr) {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        Optional<TransactionSynchronization> optional = synchronizations.stream()
                .filter(NodeSynchronization.class::isInstance).findFirst();
        if (optional.isPresent()) {
            NodeSynchronization rns = (NodeSynchronization) optional.get();
            rns.addSqlAttr(sqlAttr);
        }
    }

    @Override
    public NodeAttribute getCurrentNode() {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        Optional<TransactionSynchronization> optional = synchronizations.stream()
                .filter(NodeSynchronization.class::isInstance).findFirst();
        NodeAttribute node = null;
        if (optional.isPresent()) {
            NodeSynchronization ts = (NodeSynchronization) optional.get();
            node = ts.getNodeAttr();
        }
        return node;
    }

    @Override
    public String getName() {
        return TransactionSynchronizationManager.getCurrentTransactionName();
    }

    @Override
    public String getTransactionId() {
        Optional<TransactionSynchronization> optional = TransactionSynchronizationManager.getSynchronizations()
                .stream()
                .filter(TransactionIdSynchronization.class::isInstance)
                .findFirst();
        String trxId = null;
        if (optional.isPresent()) {
            TransactionIdSynchronization synchronization = (TransactionIdSynchronization) optional.get();
            trxId = synchronization.getTransactionId();
        }
        return trxId;
    }
}
