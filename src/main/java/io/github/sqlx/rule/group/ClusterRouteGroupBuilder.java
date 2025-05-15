package io.github.sqlx.rule.group;

import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.jdbc.transaction.Transaction;
import io.github.sqlx.loadbalance.LoadBalance;
import io.github.sqlx.rule.DataSourceNameSqlHintRouteRule;
import io.github.sqlx.rule.ForceRouteRule;
import io.github.sqlx.rule.NullSqlAttributeRouteRule;
import io.github.sqlx.rule.ReadWriteSplittingRouteRule;
import io.github.sqlx.rule.RouteWritableRule;
import io.github.sqlx.rule.TransactionRouteRule;
import io.github.sqlx.sql.parser.SqlParser;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class ClusterRouteGroupBuilder {

    private SqlXConfiguration configuration;

    private SqlParser sqlParser;

    private Transaction transaction;

    private LoadBalance readLoadBalance;

    private LoadBalance writeLoadBalance;

    public static ClusterRouteGroupBuilder builder() {
        return new ClusterRouteGroupBuilder();
    }

    public ClusterRouteGroupBuilder sqlXConfiguration(SqlXConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public ClusterRouteGroupBuilder sqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }

    public ClusterRouteGroupBuilder transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public ClusterRouteGroupBuilder readLoadBalance(LoadBalance readLoadBalance) {
        this.readLoadBalance = readLoadBalance;
        return this;
    }

    public ClusterRouteGroupBuilder writeLoadBalance(LoadBalance writeLoadBalance) {
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
