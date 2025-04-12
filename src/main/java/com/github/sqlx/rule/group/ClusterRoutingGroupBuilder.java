package com.github.sqlx.rule.group;

import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.rule.*;
import com.github.sqlx.sql.parser.SqlParser;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class ClusterRoutingGroupBuilder {

    private SqlXConfiguration configuration;

    private SqlParser sqlParser;

    private Transaction transaction;

    private LoadBalance readLoadBalance;

    private LoadBalance writeLoadBalance;

    public static ClusterRoutingGroupBuilder builder() {
        return new ClusterRoutingGroupBuilder();
    }

    public ClusterRoutingGroupBuilder sqlXConfiguration(SqlXConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public ClusterRoutingGroupBuilder sqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }

    public ClusterRoutingGroupBuilder transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public ClusterRoutingGroupBuilder readLoadBalance(LoadBalance readLoadBalance) {
        this.readLoadBalance = readLoadBalance;
        return this;
    }

    public ClusterRoutingGroupBuilder writeLoadBalance(LoadBalance writeLoadBalance) {
        this.writeLoadBalance = writeLoadBalance;
        return this;
    }

    public DefaultRouteGroup build() {
        DefaultRouteGroup routingGroup = new DefaultRouteGroup(sqlParser);
        routingGroup.install(new TransactionRouteRule(0 ,configuration , transaction));
        routingGroup.install(new DataSourceNameSqlHintRouteRule(10 , configuration));
        routingGroup.install(new ForceRouteRule(20 , configuration));
        routingGroup.install(new ReadWriteSplittingRouteRule(30 ,  readLoadBalance , writeLoadBalance));
        routingGroup.install(new NullSqlAttributeRouteRule(40 ,  readLoadBalance , writeLoadBalance));
        routingGroup.install(new RouteWritableRule(50 ,  readLoadBalance , writeLoadBalance));
        return routingGroup;
    }
}
