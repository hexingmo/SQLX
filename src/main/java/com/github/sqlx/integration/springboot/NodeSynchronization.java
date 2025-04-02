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

import com.github.sqlx.RoutingContext;
import com.github.sqlx.NodeAttribute;
import com.github.sqlx.sql.SqlAttribute;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedList;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@Getter
@Slf4j
public class NodeSynchronization implements TransactionSynchronization , Ordered {

    private final NodeAttribute nodeAttr;

    private final LinkedList<SqlAttribute> sqlAttrs = new LinkedList<>();

    public NodeSynchronization(NodeAttribute nodeAttr) {
        this.nodeAttr = nodeAttr;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    public void addSqlAttr(SqlAttribute sqlAttr) {
        if (sqlAttr != null) {
            sqlAttrs.addLast(sqlAttr);
        }
    }

    @Override
    public void afterCompletion(int status) {
        if (log.isDebugEnabled()) {
            logging(status);
        }
    }

    private void logging(int status) {
        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        String txStatus = "Unknown";
        if (TransactionSynchronization.STATUS_COMMITTED == status) {
            txStatus = "Commit";
        }
        if (TransactionSynchronization.STATUS_ROLLED_BACK == status) {
            txStatus = "Rollback";
        }

        String cluster = "None";
        RouteAttribute ra = RoutingContext.getRoutingAttribute();
        if (ra != null) {
            cluster = ra.getCluster();
        }
        String node = "None";
        if (Objects.nonNull(nodeAttr)) {
            node = "Name[" + nodeAttr.getName() + "], Type[" + nodeAttr.getNodeType() + "], Weight[" + nodeAttr.getWeight() + "], State[" + nodeAttr.getNodeState() + "]";
        }

        StringBuilder sqlMsg = new StringBuilder("[");
        StringBuilder nativeSqlMsg = new StringBuilder("[");
        for (int i = 0; i < sqlAttrs.size(); i++) {
            SqlAttribute sqlAttr = sqlAttrs.get(i);
            String sql = sqlAttr.getSql();
            String nativeSql = sqlAttr.getNativeSql();
            sqlMsg.append(System.lineSeparator()).append(i).append(". ").append(sql);
            nativeSqlMsg.append(System.lineSeparator()).append(i).append(". ").append(nativeSql);
        }
        sqlMsg.append("]");
        nativeSqlMsg.append("]");

        String msg = "Transaction " + txStatus +
                System.lineSeparator() +
                "Transaction Name: " + transactionName +
                System.lineSeparator() +
                "Cluster: " + cluster +
                System.lineSeparator() +
                "Node: " + node +
                System.lineSeparator() +
                "SQL: " + sqlMsg +
                System.lineSeparator() +
                "Native SQL: " + nativeSqlMsg;
        log.debug(msg);
    }
}
