package com.github.sqlx.integration.springboot.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * SqlX configuration properties.
 *
 * @author jing yun
 * @since 1.0
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "sqlx")
public class SqlXProperties {

    /**
     * Whether sqlx is enabled.
     */
    private boolean enabled = true;

    @NestedConfigurationProperty
    private SqlParsingProperties sqlParsing;

    /**
     * Datasource configurations properties.
     */
    @NestedConfigurationProperty
    private List<DataSourceProperties> dataSources;

    /**
     * Cluster configurations properties.
     */
    @NestedConfigurationProperty
    private List<ClusterProperties> clusters;

    /**
     * Pointcut configurations properties.
     */
    @NestedConfigurationProperty
    private List<PointcutProperties> pointcuts;

    /**
     * Metrics configuration properties.
     */
    @NestedConfigurationProperty
    private MetricsProperties metrics;

}
