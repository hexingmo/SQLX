package com.github.sqlx.integration.springboot;

import com.github.sqlx.banner.Banner;
import com.github.sqlx.banner.BlocksBanner;
import com.github.sqlx.config.*;
import com.github.sqlx.integration.springboot.properties.*;
import com.github.sqlx.integration.springboot.properties.SqlXProperties;
import com.github.sqlx.sql.parser.*;
import com.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jing yun
 * @since 1.0
 */
@Slf4j
public class SqlXConfigurationCreator {


    public static SqlXConfiguration create(SqlXProperties sqlXProperties) {
        SqlXConfiguration configuration = new SqlXConfiguration();
        configuration.setSqlParsing(createSqlParsing(sqlXProperties.getSqlParsing()));
        configuration.setDataSources(createDataSources(sqlXProperties.getDataSources()));
        configuration.setClusters(createClusters(sqlXProperties.getClusters()));
        configuration.setPointcuts(createPointcuts(sqlXProperties.getPointcuts()));
        configuration.setMetrics(createMetrics(sqlXProperties.getMetrics()));
        configuration.init();
        configuration.validate();
        return configuration;
    }

    private static MetricsConfiguration createMetrics(MetricsProperties metrics) {
        MetricsConfiguration configuration = new MetricsConfiguration();
        configuration.setEnabled(metrics.getEnabled());
        configuration.setUsername(metrics.getUsername());
        configuration.setPassword(metrics.getPassword());
        configuration.setSlowSqlMillis(metrics.getSlowSqlMillis());
        configuration.setSlowTransactionMillis(metrics.getSlowTransactionMillis());
        configuration.setEnableRoutingMetrics(metrics.getEnableRoutingMetrics());
        configuration.setEnableSqlMetrics(metrics.getEnableSqlMetrics());
        configuration.setEnableTransactionMetrics(metrics.getEnableTransactionMetrics());
        configuration.setCollectScope(metrics.getCollectScope());
        configuration.setCollectMode(metrics.getCollectMode());
        configuration.setFileDirectory(metrics.getFileDirectory());
        configuration.setDataRetentionDuration(metrics.getDataRetentionDuration());
        configuration.setCollectCorePoolSize(metrics.getCollectCorePoolSize());
        configuration.setCollectMaxPoolSize(metrics.getCollectMaxPoolSize());
        configuration.setCollectKeepAliveMillis(metrics.getCollectKeepAliveMillis());
        configuration.setCollectQueueCapacity(metrics.getCollectQueueCapacity());
        return configuration;
    }

    private static List<PointcutConfiguration> createPointcuts(List<PointcutProperties> pointcuts) {
        return pointcuts.stream().map(t -> {
            PointcutConfiguration configuration = new PointcutConfiguration();
            configuration.setExpression(t.getExpression());
            configuration.setCluster(t.getCluster());
            configuration.setNodes(t.getNodes());
            configuration.setPropagation(t.getPropagation());
            return configuration;
        }).collect(Collectors.toList());
    }

    private static List<ClusterConfiguration> createClusters(List<ClusterProperties> clusters) {
        return clusters.stream().map(t -> {
            ClusterConfiguration configuration = new ClusterConfiguration();
            configuration.setName(t.getName());
            configuration.setDefaulted(t.getDefaulted());
            configuration.setWritableNodes(t.getWritableNodes());
            configuration.setReadableNodes(t.getReadableNodes());
            configuration.setWriteLoadBalanceClass(t.getWriteLoadBalanceClass());
            configuration.setReadLoadBalanceClass(t.getReadLoadBalanceClass());
            return configuration;
        }).collect(Collectors.toList());
    }

    private static List<DataSourceConfiguration> createDataSources(List<DataSourceProperties> dataSources) {
        return dataSources.stream().map(t -> {
            DataSourceConfiguration configuration = new DataSourceConfiguration();
            configuration.setName(t.getName());
            configuration.setDefaulted(t.getDefaulted());
            configuration.setDataSourceClass(t.getDataSourceClass());
            configuration.setInitMethod(t.getInitMethod());
            configuration.setDestroyMethod(t.getDestroyMethod());
            configuration.setInitSqlScript(t.getInitSqlScript());
            configuration.setWeight(t.getWeight());
            configuration.setHeartbeatInterval(t.getHeartbeatInterval());
            configuration.setHeartbeatSql(t.getHeartbeatSql());
            configuration.setProps(t.getProps());
            return configuration;
        }).collect(Collectors.toList());
    }

    private static SqlParsingConfiguration createSqlParsing(SqlParsingProperties sqlParsing) {
        SqlParser sqlParser;
        if (Objects.isNull(sqlParsing) || StringUtils.isBlank(sqlParsing.getSqlParserClass())) {
            log.warn("sqlParserClass is empty , default use {}", JSqlParser.class.getName());
            sqlParser = new JSqlParser();
        } else {
            sqlParser = Reflect.onClass(sqlParsing.getSqlParserClass()).create().get();
        }
        sqlParser = new AnnotationSqlParser(new FailBehaviorSqlParser(sqlParser, sqlParsing.getSqlParsingFailBehavior()), new DefaultAnnotationSqlHintParser());
        SqlParsingConfiguration configuration = new SqlParsingConfiguration();
        configuration.setSqlParser(sqlParser);
        configuration.setSqlParsingFailBehavior(sqlParsing.getSqlParsingFailBehavior());
        return configuration;
    }


}
