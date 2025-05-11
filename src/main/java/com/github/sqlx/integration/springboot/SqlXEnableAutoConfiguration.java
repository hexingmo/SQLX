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
import com.github.sqlx.banner.Banner;
import com.github.sqlx.banner.BlocksBanner;
import com.github.sqlx.cluster.Cluster;
import com.github.sqlx.cluster.ClusterManager;
import com.github.sqlx.config.ClusterConfiguration;
import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.endpoint.http.DispatcherHttpHandler;
import com.github.sqlx.endpoint.http.ManagementServlet;
import com.github.sqlx.endpoint.http.ManagementWebHandler;
import com.github.sqlx.endpoint.http.V1HttpHandler;
import com.github.sqlx.endpoint.jmx.StatManager;
import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.exception.SqlXRuntimeException;
import com.github.sqlx.factory.CompositeObjectFactory;
import com.github.sqlx.factory.InstantiationObjectFactory;
import com.github.sqlx.factory.ObjectFactory;
import com.github.sqlx.factory.SpringObjectFactory;
import com.github.sqlx.integration.datasource.CompositeDataSourceInitializer;
import com.github.sqlx.integration.datasource.DataSourceInitializer;
import com.github.sqlx.integration.datasource.GenericDataSourceInitializer;
import com.github.sqlx.integration.springboot.properties.SqlXProperties;
import com.github.sqlx.jdbc.datasource.DataSourceWrapper;
import com.github.sqlx.jdbc.datasource.DatasourceManager;
import com.github.sqlx.jdbc.datasource.DefaultSqlXDataSource;
import com.github.sqlx.jdbc.datasource.SqlXDataSource;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.jdbc.transaction.TransactionIdGenerator;
import com.github.sqlx.jdbc.transaction.UUIDTransactionIdGenerator;
import com.github.sqlx.listener.*;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.loadbalance.WeightRandomLoadBalance;
import com.github.sqlx.metrics.*;
import com.github.sqlx.metrics.nitrite.*;
import com.github.sqlx.rule.group.*;
import com.github.sqlx.sql.parser.SqlParser;
import com.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Auto-configuration class for SQLX, enabling various components and configurations
 * based on the application properties.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@EnableConfigurationProperties({SqlXProperties.class})
@ConditionalOnProperty(prefix = "sqlx", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({SqlXEnableAutoConfiguration.BaseConfiguration.class,
        SqlXEnableAutoConfiguration.MetricsConfiguration.class})
@Slf4j
public class SqlXEnableAutoConfiguration {

    private static final String HTTP_RESOURCES_PATH = "META-INF/http/resources";

    @Configuration
    static class BaseConfiguration {

        private final SqlXProperties properties;

        BaseConfiguration(SqlXProperties properties) {
            this.properties = properties;
        }

        private static void registerMBean(StatManager statManager) {
            try {
                MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
                ObjectName objectName = new ObjectName("com.github.sqlx:type=SQLXStat");
                mBeanServer.registerMBean(statManager, objectName);
            } catch (Exception e) {
                throw new SqlXRuntimeException("register JMX MBean StatManager Error", e);
            }
        }

        @Bean
        public SqlXConfiguration sqlXConfiguration() {
            Banner banner = new BlocksBanner();
            banner.printBanner(log::info);
            return SqlXConfigurationCreator.create(properties);
        }

        @Bean
        public ExpressionBeanPostProcessor expressionBeanPostProcessor() {
            return new ExpressionBeanPostProcessor(sqlXConfiguration());
        }

        @Bean
        public AnnotationBeanPostProcessor annotationBeanPostProcessor() {
            return new AnnotationBeanPostProcessor(sqlXConfiguration());
        }


        @Bean
        public ObjectFactory objectFactory(ApplicationContext context) {
            List<ObjectFactory> factories = new LinkedList<>();
            factories.add(new SpringObjectFactory(context));
            factories.add(new InstantiationObjectFactory());
            return new CompositeObjectFactory(factories);
        }

        @Bean
        public TransactionIdGenerator transactionIdGenerator() {
            return new UUIDTransactionIdGenerator();
        }

        @Bean
        public Transaction transaction(TransactionIdGenerator transactionIdGenerator) {
            return new SpringTransaction(transactionIdGenerator);
        }

        @Bean
        public SqlParser sqlParser() {
            SqlXConfiguration configuration = sqlXConfiguration();
            return configuration.getSqlParser();
        }

        @Bean
        public DataSourceInitializer compositeDataSourceInitializer(@Autowired(required = false) Set<GenericDataSourceInitializer<?>> initializers) {
            return new CompositeDataSourceInitializer(initializers);
        }


        @Bean
        public DatasourceManager datasourceManager(DataSourceInitializer dataSourceInitializer) {
            SqlXConfiguration configuration = sqlXConfiguration();
            Map<String, DataSourceWrapper> allDataSources = new HashMap<>();
            for (DataSourceConfiguration dataSourceConf : configuration.getDataSources()) {
                DataSource dataSource = dataSourceInitializer.initialize(dataSourceConf);
                allDataSources.put(dataSourceConf.getName(), new DataSourceWrapper(dataSourceConf.getName(), dataSource, dataSourceConf.getNodeAttribute(), dataSourceConf.getDefaulted()));
            }

            DatasourceManager datasourceManager = new DatasourceManager(configuration);
            datasourceManager.addDataSources(allDataSources);
            return datasourceManager;
        }

        @Bean
        public ClusterManager clusterManager(SqlParser sqlParser, Transaction transaction, EventListener eventListener) {
            SqlXConfiguration config = sqlXConfiguration();
            ClusterManager cm = new ClusterManager(config);
            for (ClusterConfiguration conf : config.getClusters()) {
                LoadBalance wlb = Optional.ofNullable(conf.getWriteLoadBalanceClass())
                        .map(t -> {
                            LoadBalance loadBalance = Reflect.onClass(t).create().get();
                            conf.getWritableRoutingNodeAttributes().forEach(loadBalance::addOption);
                            return loadBalance;
                        }).orElse(new WeightRandomLoadBalance(conf.getWritableRoutingNodeAttributes()));

                LoadBalance rlb = Optional.ofNullable(conf.getReadLoadBalanceClass())
                        .map(t -> {
                            LoadBalance loadBalance = Reflect.onClass(t).create().get();
                            conf.getReadableRoutingNodeAttributes().forEach(loadBalance::addOption);
                            return loadBalance;
                        }).orElse(new WeightRandomLoadBalance(conf.getReadableRoutingNodeAttributes()));;

                Cluster cluster = new Cluster();
                cluster.setName(conf.getName());
                cluster.setNodes(conf.getNodeAttributes());

                CompositeRouteGroup compositeRoutingGroup = new CompositeRouteGroup(eventListener, transaction);
                DefaultRouteGroup defaultRoutingGroup = ClusterRouteGroupBuilder.builder()
                        .sqlXConfiguration(config)
                        .sqlParser(sqlParser)
                        .transaction(transaction)
                        .readLoadBalance(rlb)
                        .writeLoadBalance(wlb)
                        .build();
                compositeRoutingGroup.installLast(defaultRoutingGroup);
                cluster.setRule(compositeRoutingGroup);

                cm.addCluster(conf.getName(), cluster);
            }
            return cm;
        }


        @Bean
        public EventListener eventListener(@Autowired(required = false) List<EventListener> eventListeners) {
            List<EventListener> listeners = new ArrayList<>();
            listeners.add(new DefaultEventListener());
            listeners.add(new LoggingEventListener());
            if (eventListeners != null && !eventListeners.isEmpty()) {
                listeners.addAll(eventListeners);
            }
            return new CompositeEventListener(listeners);
        }

        @Bean
        public CompositeRouteGroup routingGroup(SqlParser sqlParser, Transaction transaction, @Autowired(required = false) List<RouteGroup<?>> routingGroups, EventListener eventListener, DatasourceManager datasourceManager) {

            SqlXConfiguration sqlXConfiguration = sqlXConfiguration();
            DefaultRouteGroup drg = NoneClusterRouteGroupBuilder.builder()
                    .sqlXConfiguration(sqlXConfiguration)
                    .sqlParser(sqlParser)
                    .transaction(transaction)
                    .datasourceManager(datasourceManager)
                    .build();
            CompositeRouteGroup compositeRoutingGroup = new CompositeRouteGroup(eventListener, transaction);
            compositeRoutingGroup.installFirst(routingGroups);
            compositeRoutingGroup.installLast(drg);
            return compositeRoutingGroup;
        }

        @Bean
        public StatManager statManager(DataSourceInitializer dataSourceInitializer, DatasourceManager datasourceManager, ClusterManager clusterManager, List<RouteGroup<?>> routingGroups, EventListener eventListener, Transaction transaction) {
            return new StatManager(sqlXConfiguration(), dataSourceInitializer, datasourceManager, clusterManager, routingGroups, eventListener, transaction);
        }

        @Bean("sqlXDataSource")
        public SqlXDataSource sqlXDataSource(StatManager statManager, ClusterManager clusterManager, DatasourceManager datasourceManager, EventListener eventListener, Transaction transaction) {
            registerMBean(statManager);
            SqlXConfiguration configuration = sqlXConfiguration();
            DefaultRouteGroup drg = NoneClusterRouteGroupBuilder.builder()
                    .sqlXConfiguration(configuration)
                    .sqlParser(configuration.getSqlParser())
                    .transaction(transaction)
                    .datasourceManager(datasourceManager)
                    .build();
            CompositeRouteGroup compositeRoutingGroup = new CompositeRouteGroup(eventListener, transaction);
            compositeRoutingGroup.installLast(drg);
            return new DefaultSqlXDataSource(clusterManager, datasourceManager, eventListener, compositeRoutingGroup);
        }

    }


    @Configuration
    @DependsOn("eventListener")
    @ConditionalOnProperty(prefix = "sqlx.metrics", name = "enabled", havingValue = "true")
    static class MetricsConfiguration implements InitializingBean {

        private static final String PATH_PREFIX = "/sqlx";

        private final SqlXConfiguration sqlXConfiguration;

        private final CompositeEventListener compositeEventListener;

        private final NitriteRoutingMetricsRepository routingMetricsRepository;

        private final NitriteSqlMetricsRepository sqlMetricsRepository;

        private final NitriteTransactionMetricsRepository transactionMetricsRepository;

        private final NitriteTableAccessMetricsRepository tableAccessMetricsRepository;

        private final NodeSqlExecuteNumMetricsRepository nodeSqlExecuteNumMetricsRepository;

        public MetricsConfiguration(SqlXConfiguration sqlXConfiguration, CompositeEventListener compositeEventListener) {
            this.sqlXConfiguration = sqlXConfiguration;
            this.compositeEventListener = compositeEventListener;

            com.github.sqlx.config.MetricsConfiguration metrics = sqlXConfiguration.getMetrics();
            this.routingMetricsRepository = new NitriteRoutingMetricsRepository(metrics.getFileDirectory());
            this.sqlMetricsRepository = new NitriteSqlMetricsRepository(metrics.getFileDirectory());
            this.transactionMetricsRepository = new NitriteTransactionMetricsRepository(metrics.getFileDirectory());
            this.tableAccessMetricsRepository = new NitriteTableAccessMetricsRepository(metrics.getFileDirectory());
            this.nodeSqlExecuteNumMetricsRepository = new NodeSqlExecuteNumMetricsRepository(metrics.getFileDirectory());
        }

        @Override
        public void afterPropertiesSet() {
            com.github.sqlx.config.MetricsConfiguration metrics = sqlXConfiguration.getMetrics();

            GenericMetricsRepository metricsRepository = new GenericMetricsRepository();
            metricsRepository.registerRepository(RoutingMetrics.class, routingMetricsRepository);
            metricsRepository.registerRepository(SqlMetrics.class, sqlMetricsRepository);
            metricsRepository.registerRepository(TransactionMetrics.class, transactionMetricsRepository);
            metricsRepository.registerRepository(TableAccessMetrics.class, tableAccessMetricsRepository);
            metricsRepository.registerRepository(NodeSqlExecuteNumMetrics.class, nodeSqlExecuteNumMetricsRepository);

            MetricsCollector metricsCollector;
            if (Objects.equals(metrics.getCollectMode(), MetricsCollectMode.SYNC)) {
                metricsCollector = new SyncMetricsCollector(metricsRepository);
            } else if (Objects.equals(metrics.getCollectMode(), MetricsCollectMode.ASYNC)) {
                metricsCollector = new AsyncMetricsCollector(metricsRepository, metrics);
            } else {
                throw new ConfigurationException("Unsupported metrics collect mode: " + metrics.getCollectMode());
            }
            compositeEventListener.addListener(new MetricsCollectEventListener(metrics, metricsCollector));

            DeleteByCreateTimeStorageReleaser storageReleaser = new DeleteByCreateTimeStorageReleaser(metrics);
            storageReleaser.registerRepository(metricsRepository);
            storageReleaser.registerRepository(sqlMetricsRepository);
            storageReleaser.registerRepository(transactionMetricsRepository);
            storageReleaser.start();
        }

        @ConditionalOnBean(type = "org.springframework.web.servlet.DispatcherServlet")
        @Bean
        public ServletRegistrationBean<ManagementServlet> servletRegistrationBean(StatManager statManager) {

            List<Object> handlers = new ArrayList<>();
            handlers.add(new V1HttpHandler(HTTP_RESOURCES_PATH, sqlXConfiguration, statManager, routingMetricsRepository, sqlMetricsRepository, transactionMetricsRepository, tableAccessMetricsRepository, nodeSqlExecuteNumMetricsRepository));
            DispatcherHttpHandler dispatcherHttpHandler = new DispatcherHttpHandler(handlers);
            return new ServletRegistrationBean<>(new ManagementServlet(dispatcherHttpHandler), PATH_PREFIX + "/*");
        }


        @ConditionalOnBean(type = "org.springframework.web.reactive.DispatcherHandler")
        @Bean
        public RouterFunction<ServerResponse> route(StatManager statManager) {
            List<Object> handlers = new ArrayList<>();
            handlers.add(new V1HttpHandler(HTTP_RESOURCES_PATH, sqlXConfiguration, statManager, routingMetricsRepository, sqlMetricsRepository, transactionMetricsRepository, tableAccessMetricsRepository, nodeSqlExecuteNumMetricsRepository));
            DispatcherHttpHandler dispatcherHttpHandler = new DispatcherHttpHandler(handlers);
            ManagementWebHandler managementWebHandler = new ManagementWebHandler(PATH_PREFIX, dispatcherHttpHandler);
            return RouterFunctions.route()
                    .path(PATH_PREFIX, builder -> builder
                            .GET("/**", request -> managementWebHandler.handleFunction(request.exchange()))
                            .POST("/**", request -> managementWebHandler.handleFunction(request.exchange()))
                            .PUT("/**", request -> managementWebHandler.handleFunction(request.exchange()))
                            .DELETE("/**", request -> managementWebHandler.handleFunction(request.exchange()))
                            .OPTIONS("/**", request -> managementWebHandler.handleFunction(request.exchange()))
                            .build())
                    .build();
        }
    }

}
