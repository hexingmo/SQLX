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
import com.github.sqlx.cluster.Cluster;
import com.github.sqlx.cluster.ClusterManager;
import com.github.sqlx.jdbc.datasource.*;
import com.github.sqlx.config.ClusterConfiguration;
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
import com.github.sqlx.integration.datasource.GenericDataSourceInitializer;

import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.integration.datasource.CompositeDataSourceInitializer;
import com.github.sqlx.integration.datasource.DataSourceInitializer;
import com.github.sqlx.jdbc.transaction.Transaction;
import com.github.sqlx.jdbc.transaction.TransactionIdGenerator;
import com.github.sqlx.jdbc.transaction.UUIDTransactionIdGenerator;
import com.github.sqlx.listener.CompositeEventListener;
import com.github.sqlx.listener.DefaultEventListener;
import com.github.sqlx.listener.LoggingEventListener;
import com.github.sqlx.listener.MetricsCollectEventListener;
import com.github.sqlx.listener.EventListener;
import com.github.sqlx.loadbalance.LoadBalance;
import com.github.sqlx.loadbalance.WeightRandomLoadBalance;
import com.github.sqlx.metrics.*;
import com.github.sqlx.metrics.nitrite.*;
import com.github.sqlx.rule.group.*;
import com.github.sqlx.rule.group.ClusterRouteGroupBuilder;

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
@EnableConfigurationProperties(SqlXProperties.class)
@ConditionalOnProperty(prefix = "sqlx.config", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({SqlXEnableAutoConfiguration.BaseConfiguration.class,
        SqlXEnableAutoConfiguration.MetricsConfiguration.class})
@Slf4j
public class SqlXEnableAutoConfiguration {

    private static final String HTTP_RESOURCES_PATH = "META-INF/http/resources";

    private static void registerMBean(StatManager statManager) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName("com.github.sqlx:type=SQLXStat");
            mBeanServer.registerMBean(statManager, objectName);
        } catch (Exception e) {
            throw new SqlXRuntimeException("register JMX MBean StatManager Error", e);
        }
    }

    @Configuration
    class BaseConfiguration {

        @Bean
        public ExpressionBeanPostProcessor expressionBeanPostProcessor(SqlXProperties properties) {
            return new ExpressionBeanPostProcessor(properties.getConfig());
        }

        @Bean
        public AnnotationBeanPostProcessor annotationBeanPostProcessor(SqlXProperties properties) {
            return new AnnotationBeanPostProcessor(properties.getConfig());
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
        public SqlParser sqlParser(SqlXProperties properties) {
            SqlXConfiguration configuration = properties.getConfig();
            return configuration.getSqlParserInstance();
        }

        @Bean
        public DataSourceInitializer compositeDataSourceInitializer(@Autowired(required = false) Set<GenericDataSourceInitializer<?>> initializers) {
            return new CompositeDataSourceInitializer(initializers);
        }



        @Bean
        public DatasourceManager datasourceManager(SqlXProperties properties, DataSourceInitializer dataSourceInitializer) {
            SqlXConfiguration configuration = properties.getConfig();
            Map<String, DataSourceWrapper> allDataSources = new HashMap<>();
            for (DataSourceConfiguration dataSourceConf : configuration.getDataSources()) {
                DataSource dataSource = dataSourceInitializer.initialize(dataSourceConf);
                allDataSources.put(dataSourceConf.getName(), new DataSourceWrapper(dataSourceConf.getName(), dataSource, dataSourceConf.getNodeAttribute() , dataSourceConf.getDefaulted()));
            }

            DatasourceManager datasourceManager = new DatasourceManager(configuration);
            datasourceManager.addDataSources(allDataSources);
            return datasourceManager;
        }

        @Bean
        public ClusterManager clusterManager(SqlXProperties properties, SqlParser sqlParser, Transaction transaction, EventListener eventListener) {
            SqlXConfiguration config = properties.getConfig();
            ClusterManager cm = new ClusterManager(config);
            for (ClusterConfiguration conf : config.getClusters()) {
                String writeLoadBalanceClass = conf.getWriteLoadBalanceClass();
                LoadBalance wlb;
                if (StringUtils.isBlank(writeLoadBalanceClass)) {
                    wlb = new WeightRandomLoadBalance(conf.getWritableRoutingNodeAttributes());
                } else {
                    wlb = Reflect.onClass(writeLoadBalanceClass).create().get();
                    for (NodeAttribute node : conf.getWritableRoutingNodeAttributes()) {
                        wlb.addOption(node);
                    }
                }

                String readLoadBalanceClass = conf.getReadLoadBalanceClass();

                LoadBalance rlb;
                if (StringUtils.isBlank(readLoadBalanceClass)) {
                    rlb = new WeightRandomLoadBalance(conf.getReadableRoutingNodeAttributes());
                } else {
                    rlb = Reflect.onClass(readLoadBalanceClass).create().get();
                    for (NodeAttribute node : conf.getReadableRoutingNodeAttributes()) {
                        rlb.addOption(node);
                    }
                }

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
        public CompositeRouteGroup routingGroup(SqlXProperties properties, SqlParser sqlParser, Transaction transaction, @Autowired(required = false) List<RouteGroup<?>> routingGroups, EventListener eventListener, DatasourceManager datasourceManager) {

            SqlXConfiguration routing = properties.getConfig();
            DefaultRouteGroup drg = NoneClusterRouteGroupBuilder.builder()
                    .sqlXConfiguration(routing)
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
        public StatManager statManager(SqlXProperties properties, DataSourceInitializer dataSourceInitializer, DatasourceManager datasourceManager, ClusterManager clusterManager, List<RouteGroup<?>> routingGroups, EventListener eventListener, Transaction transaction) {
            return new StatManager(properties.getConfig(), dataSourceInitializer, datasourceManager, clusterManager, routingGroups, eventListener, transaction);
        }

        @Bean("sqlXDataSource")
        public SqlXDataSource sqlXDataSource(SqlXProperties properties ,StatManager statManager, ClusterManager clusterManager, DatasourceManager datasourceManager, EventListener eventListener , Transaction transaction) {
            registerMBean(statManager);
            SqlXConfiguration configuration = properties.getConfig();
            DefaultRouteGroup drg = NoneClusterRouteGroupBuilder.builder()
                    .sqlXConfiguration(configuration)
                    .sqlParser(configuration.getSqlParserInstance())
                    .transaction(transaction)
                    .datasourceManager(datasourceManager)
                    .build();
            CompositeRouteGroup compositeRoutingGroup = new CompositeRouteGroup(eventListener, transaction);
            compositeRoutingGroup.installLast(drg);
            return new DefaultSqlXDataSource(clusterManager ,datasourceManager, eventListener , compositeRoutingGroup);
        }

    }


    @Configuration
    @DependsOn("eventListener")
    @ConditionalOnProperty(prefix = "sqlx.config.metrics", name = "enabled", havingValue = "true")
    class MetricsConfiguration implements InitializingBean {

        private static final String PATH_PREFIX = "/sqlx";

        private final SqlXProperties properties;

        private final CompositeEventListener compositeEventListener;

        private final NitriteRoutingMetricsRepository routingMetricsRepository;

        private final NitriteSqlMetricsRepository sqlMetricsRepository;

        private final NitriteTransactionMetricsRepository transactionMetricsRepository;

        private final NitriteTableAccessMetricsRepository tableAccessMetricsRepository;

        private final NodeSqlExecuteNumMetricsRepository nodeSqlExecuteNumMetricsRepository;

        public MetricsConfiguration(CompositeEventListener compositeEventListener, SqlXProperties properties) {
            this.properties = properties;
            this.compositeEventListener = compositeEventListener;

            com.github.sqlx.config.MetricsConfiguration metrics = properties.getConfig().getMetrics();
            this.routingMetricsRepository = new NitriteRoutingMetricsRepository(metrics.getFileDirectory());
            this.sqlMetricsRepository = new NitriteSqlMetricsRepository(metrics.getFileDirectory());
            this.transactionMetricsRepository = new NitriteTransactionMetricsRepository(metrics.getFileDirectory());
            this.tableAccessMetricsRepository = new NitriteTableAccessMetricsRepository(metrics.getFileDirectory());
            this.nodeSqlExecuteNumMetricsRepository = new NodeSqlExecuteNumMetricsRepository(metrics.getFileDirectory());
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            com.github.sqlx.config.MetricsConfiguration metrics = properties.getConfig().getMetrics();

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
            handlers.add(new V1HttpHandler(HTTP_RESOURCES_PATH, properties, statManager, routingMetricsRepository, sqlMetricsRepository, transactionMetricsRepository, tableAccessMetricsRepository, nodeSqlExecuteNumMetricsRepository));
            DispatcherHttpHandler dispatcherHttpHandler = new DispatcherHttpHandler(handlers);
            return new ServletRegistrationBean<>(new ManagementServlet(dispatcherHttpHandler), PATH_PREFIX + "/*");
        }


        @ConditionalOnBean(type = "org.springframework.web.reactive.DispatcherHandler")
        @Bean
        public RouterFunction<ServerResponse> route(StatManager statManager) {
            List<Object> handlers = new ArrayList<>();
            handlers.add(new V1HttpHandler(HTTP_RESOURCES_PATH, properties, statManager, routingMetricsRepository, sqlMetricsRepository, transactionMetricsRepository, tableAccessMetricsRepository, nodeSqlExecuteNumMetricsRepository));
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
