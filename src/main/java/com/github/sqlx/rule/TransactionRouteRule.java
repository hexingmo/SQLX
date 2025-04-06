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

package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;

/**
 * When transactions exist, the routing rules are as follows:
 * If a read-only transaction exists and the current SQL being
 * executed is a read statement, it will be routed to the read
 * data source. If the transaction is not read-only and there
 * are write statements in the transaction, all SQL statements
 * in the transaction will be routed to the write data source
 * for execution.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class TransactionRouteRule extends AbstractRouteRule {

    private final Transaction transaction;


    public TransactionRouteRule(Integer priority, SqlParser sqlParser, LoadBalance readLoadBalance, LoadBalance writeLoadBalance, Transaction transaction) {
        super(priority, sqlParser, readLoadBalance, writeLoadBalance);
        this.transaction = transaction;
    }

    @Override
    public NodeAttribute routing(SqlAttribute attribute) {

        if (!transaction.isActive()) {
            return null;
        }
        NodeAttribute node = transaction.getCurrentNode();
        if (node != null) {
            transaction.addSql(attribute);
            return node;
        }
        node = chooseWriteNode();
        if (node != null) {
            transaction.registerNode(node , attribute);
        }
        return node;
    }
}
