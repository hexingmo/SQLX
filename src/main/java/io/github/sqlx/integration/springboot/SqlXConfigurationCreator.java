package io.github.sqlx.integration.springboot;

import io.github.sqlx.config.ClusterConfiguration;
import io.github.sqlx.config.DataSourceConfiguration;
import io.github.sqlx.config.MetricsConfiguration;
import io.github.sqlx.config.PointcutConfiguration;
import io.github.sqlx.config.SqlParsingConfiguration;
import io.github.sqlx.config.SqlXConfiguration;
import io.github.sqlx.integration.springboot.properties.ClusterProperties;
import io.github.sqlx.integration.springboot.properties.DataSourceProperties;
import io.github.sqlx.integration.springboot.properties.MetricsProperties;
import io.github.sqlx.integration.springboot.properties.PointcutProperties;
import io.github.sqlx.integration.springboot.properties.SqlParsingProperties;
import io.github.sqlx.integration.springboot.properties.SqlXProperties;
import io.github.sqlx.sql.parser.AnnotationSqlParser;
import io.github.sqlx.sql.parser.DefaultAnnotationSqlHintParser;
import io.github.sqlx.sql.parser.FailBehaviorSqlParser;
import io.github.sqlx.sql.parser.JSqlParser;
import io.github.sqlx.sql.parser.SqlParser;
import io.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jing yun
 * @since 1.0
 */
@Slf4j
public class SqlXConfigurationCreator {


    /**
     * Creates a SqlXConfiguration instance based on the provided SqlXProperties.
     * Initializes and validates the configuration before returning it.
     *
     * @param sqlXProperties The properties used to configure SqlX.
     * @return A fully initialized and validated SqlXConfiguration instance.
     */
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

    /**
     * Creates a MetricsConfiguration instance based on the provided MetricsProperties.
     *
     * @param metrics The properties used to configure metrics.
     * @return A MetricsConfiguration instance with the specified settings.
     */
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

    /**
     * Creates a list of PointcutConfiguration instances based on the provided PointcutProperties.
     *
     * @param pointcuts The properties used to configure pointcuts.
     * @return A list of PointcutConfiguration instances.
     */
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

    /**
     * Creates a list of ClusterConfiguration instances based on the provided ClusterProperties.
     *
     * @param clusters The properties used to configure clusters.
     * @return A list of ClusterConfiguration instances.
     */
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

    /**
     * Creates a list of DataSourceConfiguration instances based on the provided DataSourceProperties.
     *
     * @param dataSources The properties used to configure data sources.
     * @return A list of DataSourceConfiguration instances.
     */
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
            Optional.ofNullable(t.getProps()).ifPresent(configuration::setProps);
            return configuration;
        }).collect(Collectors.toList());
    }

    /**
     * Creates a SqlParsingConfiguration instance based on the provided SqlParsingProperties.
     * Initializes the SQL parser with the specified fail behavior and annotation support.
     *
     * @param sqlParsing The properties used to configure SQL parsing.
     * @return A SqlParsingConfiguration instance with the specified settings.
     */
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
