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
package com.github.sqlx.endpoint.http;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.config.ClusterConfiguration;
import com.github.sqlx.config.DataSourceConfiguration;
import com.github.sqlx.config.MetricsConfiguration;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.endpoint.jmx.StatManagerMBean;
import com.github.sqlx.integration.springboot.SqlXProperties;
import com.github.sqlx.metrics.*;
import com.github.sqlx.metrics.nitrite.NodeSqlExecuteNumMetricsRepository;
import com.github.sqlx.util.IOUtils;
import com.github.sqlx.util.JsonUtils;
import com.github.sqlx.util.SecurityUtils;
import com.github.sqlx.util.StringUtils;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP handler for version 1 of the API, responsible for processing various HTTP requests
 * related to data sources, clusters, and metrics.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class V1HttpHandler {

    private final String resourcePath;

    private final SqlXProperties sqlXProperties;

    private final SqlXConfiguration sqlXConfiguration;

    private final StatManagerMBean statManagerMBean;

    private final RoutingMetricsRepository routingMetricsRepository;

    private final SqlMetricsRepository sqlMetricsRepository;

    private final TransactionMetricsRepository transactionMetricsRepository;

    private final TableAccessMetricsRepository tableAccessMetricsRepository;

    private final NodeSqlExecuteNumMetricsRepository nodeSqlExecuteNumMetricsRepository;

    /**
     * Constructs a new V1HttpHandler.
     *
     * @param resourcePath                 the path to static resources
     * @param sqlXProperties               the SqlXProperties instance
     * @param statManagerMBean             the StatManagerMBean instance
     * @param routingMetricsRepository     the RoutingMetricsRepository instance
     * @param sqlMetricsRepository         the SqlMetricsRepository instance
     * @param transactionMetricsRepository the TransactionMetricsRepository instance
     * @param tableAccessMetricsRepository the TableAccessMetricsRepository instance
     */
    public V1HttpHandler(String resourcePath, SqlXProperties sqlXProperties, StatManagerMBean statManagerMBean, RoutingMetricsRepository routingMetricsRepository, SqlMetricsRepository sqlMetricsRepository, TransactionMetricsRepository transactionMetricsRepository, TableAccessMetricsRepository tableAccessMetricsRepository,NodeSqlExecuteNumMetricsRepository nodeSqlExecuteNumMetricsRepository) {
        this.resourcePath = resourcePath;
        this.sqlXProperties = sqlXProperties;
        this.sqlXConfiguration = sqlXProperties.getConfig();
        this.statManagerMBean = statManagerMBean;
        this.routingMetricsRepository = routingMetricsRepository;
        this.sqlMetricsRepository = sqlMetricsRepository;
        this.transactionMetricsRepository = transactionMetricsRepository;
        this.tableAccessMetricsRepository = tableAccessMetricsRepository;
        this.nodeSqlExecuteNumMetricsRepository = nodeSqlExecuteNumMetricsRepository;
    }

    @HttpHandle(path = "/v1/login", method = "post")
    public HttpResponse login(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        try {
            LoginPayload loginPayload = request.getBodyBean(LoginPayload.class);
            if (Objects.isNull(loginPayload)) {
                throw new IllegalArgumentException("login payload is null");
            }
            if (StringUtils.isBlank(loginPayload.getUsername()) || StringUtils.isBlank(loginPayload.getPassword())) {
                throw new IllegalArgumentException("username or password is empty");
            }
            MetricsConfiguration metrics = sqlXProperties.getConfig().getMetrics();
            if (!StringUtils.equals(metrics.getUsername(), loginPayload.getUsername())
                    || !StringUtils.equals(metrics.getPassword(), loginPayload.getPassword())) {
                throw new IllegalArgumentException("username or password is incorrect");
            }
            if (Objects.nonNull(request.getHttpSession())) {
                log.info("login success setAuthSessionUserKey in HttpSession");
                SecurityUtils.setAuthSessionUserKey(request.getHttpSession() , metrics.getUsername());
                httpResponse.setBody(JsonUtils.toJson(Result.ok()));
            } else if (Objects.nonNull(request.getWebSession())) {
                log.info("login success setAuthSessionUserKey in WebSession");
                SecurityUtils.setAuthSessionUserKey(request.getWebSession(), metrics.getUsername());
                httpResponse.setBody(JsonUtils.toJson(Result.ok()));
            }
        } catch (Exception e) {
            log.error("login error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }

    /**
     * Handles requests for static resources such as HTML, JavaScript, and CSS files.
     *
     * @param request the HttpRequest instance
     * @return an HttpResponse containing the requested resource or a 404 error
     * @throws IOException if an I/O error occurs while reading the resource
     */
    @HttpHandle(path = "\\/[a-zA-Z0-9\\-._~:\\/?#\\[\\]@!$&'()*+,;=]+\\.((html?)|(js)|(css))")
    public HttpResponse handleStaticResources(HttpRequest request) throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        String path = resourcePath + request.getPath();
        if (!IOUtils.exists(path)) {
            httpResponse.setStatus(404);

            if (path.endsWith(".html")) {
                httpResponse.setContentType("text/html;charset=UTF-8");
                httpResponse.setBody(IOUtils.readFromResource(resourcePath + "/404.html"));
            }
        } else {

            if (path.endsWith(".html")) {
                httpResponse.setContentType("text/html;charset=UTF-8");
            } else if (path.endsWith(".js")) {
                httpResponse.setContentType("text/javascript;charset=UTF-8");
            } else if (path.endsWith(".css")) {
                httpResponse.setContentType("text/css;charset=UTF-8");
            }
            httpResponse.setBody(IOUtils.readFromResource(path)).setStatus(200);
        }
        return httpResponse;
    }

    /**
     * Retrieves the list of data sources.
     *
     * @param request the HttpRequest instance
     * @return an HttpResponse containing the list of data sources in JSON format
     */
    @HttpHandle(path = "/v1/datasource", method = "get")
    public HttpResponse getDatasourceList(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8");
        List<DataSourceConfiguration> dataSources = sqlXConfiguration.getDataSources();
        Result<List<DataSourceConfiguration>> result = Result.ok(dataSources);
        httpResponse.setStatus(200).setBody(JsonUtils.maskPasswordToJson(result));
        return httpResponse;
    }

    /**
     * Tests the connection to a data source.
     *
     * @param request the HttpRequest instance containing the data source configuration
     * @return an HttpResponse indicating the success or failure of the connection test
     */
    @HttpHandle(path = "/v1/datasource/test-connection", method = "post")
    public HttpResponse testConnection(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        try {
            DataSourceConfiguration configuration = JsonUtils.fromJson(request.getBody(), DataSourceConfiguration.class);
            configuration.validate();
            httpResponse.setBody(JsonUtils.toJson(Result.ok()));
        } catch (Exception e) {
            log.error("test connection error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }

    /**
     * Adds a new data source.
     *
     * @param request the HttpRequest instance containing the data source configuration
     * @return an HttpResponse indicating the success or failure of adding the data source
     */
    @HttpHandle(path = "/v1/datasource/add", method = "post")
    public HttpResponse addDatasource(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        try {
            statManagerMBean.addDataSource(request.getBody());
            httpResponse.setBody(JsonUtils.toJson(Result.ok()));
        } catch (Exception e) {
            log.error("add datasource error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }


    /**
     * Modifies the weight and state of a data source.
     *
     * @param request the HttpRequest instance containing the data source name, weight, and state
     * @return an HttpResponse indicating the success or failure of modifying the data source
     */
    @HttpHandle(path = "/v1/datasource/modify-weight-state", method = "post")
    public HttpResponse modifyDatasourceWeightAndState(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);

        ModifyDatasourceWeightAndState payload = JsonUtils.fromJson(request.getBody(), ModifyDatasourceWeightAndState.class);
        try {
            statManagerMBean.setNodeWeight(payload.getName(), payload.getWeight());
            statManagerMBean.setNodeState(payload.getName(), payload.getNodeState());
            httpResponse.setBody(JsonUtils.toJson(Result.ok()));
        } catch (Exception e) {
            log.error("modify datasource weight and state error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }

    /**
     * Removes a data source.
     *
     * @param request the HttpRequest instance containing the data source name
     * @return an HttpResponse indicating the success or failure of removing the data source
     */
    @HttpHandle(path = "/v1/datasource/remove", method = "delete")
    public HttpResponse removeDatasource(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        JsonUtils.fromJson(request.getBody(), DataSourceConfiguration.class);
        try {
            statManagerMBean.removeDatasource(request.getParameter("name"));
            httpResponse.setBody(JsonUtils.toJson(Result.ok()));
        } catch (Exception e) {
            log.error("remove datasource error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }


    /**
     * Retrieves the list of clusters.
     *
     * @param request the HttpRequest instance
     * @return an HttpResponse containing the list of clusters in JSON format
     */
    @HttpHandle(path = "/v1/cluster", method = "get")
    public HttpResponse getClusterList(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8");
        List<ClusterConfiguration> clusters = sqlXConfiguration.getClusters();
        Result<List<ClusterConfiguration>> result = Result.ok(clusters);
        httpResponse.setStatus(200).setBody(JsonUtils.toJson(result));
        return httpResponse;
    }

    /**
     * Adds a new cluster.
     *
     * @param request the HttpRequest instance containing the cluster configuration
     * @return an HttpResponse indicating the success or failure of adding the cluster
     */
    @HttpHandle(path = "/v1/cluster/add", method = "post")
    public HttpResponse addCluster(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        try {
            statManagerMBean.addCluster(request.getBody());
            httpResponse.setBody(JsonUtils.toJson(Result.ok()));
        } catch (Exception e) {
            log.error("add cluster error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }

    /**
     * Adds a data source to a cluster.
     *
     * @param request the HttpRequest instance containing the cluster name and data source name
     * @return an HttpResponse indicating the success or failure of adding the data source to the cluster
     */
    @HttpHandle(path = "/v1/cluster/add-datasource", method = "post")
    public HttpResponse addDatasourceToCluster(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        try {
            Map<String, JsonElement> payload = JsonUtils.fromJson(request.getBody());
            String clusterName = payload.get("clusterName").getAsString();
            if (StringUtils.isBlank(clusterName)) {
                throw new IllegalArgumentException("clusterName is blank");
            }
            String nodeName = payload.get("nodeName").getAsString();
            if (StringUtils.isBlank(nodeName)) {
                throw new IllegalArgumentException("nodeName is blank");
            }
            statManagerMBean.addNodeInCluster(clusterName, nodeName);
            httpResponse.setBody(JsonUtils.toJson(Result.ok()));
        } catch (Exception e) {
            log.error("add datasource to cluster error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }

    /**
     * Removes a data source from a cluster.
     *
     * @param request the HttpRequest instance containing the cluster name and data source name
     * @return an HttpResponse indicating the success or failure of removing the data source from the cluster
     */
    @HttpHandle(path = "/v1/cluster/remove-datasource", method = "post")
    public HttpResponse removeDatasourceFromCluster(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        try {
            Map<String, JsonElement> payload = JsonUtils.fromJson(request.getBody());
            String clusterName = payload.get("clusterName").getAsString();
            if (StringUtils.isBlank(clusterName)) {
                throw new IllegalArgumentException("clusterName is blank");
            }
            String nodeName = payload.get("nodeName").getAsString();
            if (StringUtils.isBlank(nodeName)) {
                throw new IllegalArgumentException("nodeName is blank");
            }
            statManagerMBean.removeNodeInCluster(clusterName, nodeName);
            httpResponse.setBody(JsonUtils.toJson(Result.ok()));
        } catch (Exception e) {
            log.error("remove datasource from cluster error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }
        return httpResponse;
    }

    /**
     * Queries routing metrics with pagination.
     *
     * @param request the HttpRequest instance containing the query criteria
     * @return an HttpResponse containing the paginated routing metrics in JSON format
     */
    @HttpHandle(path = "/v1/routing-metrics/page", method = "post")
    public HttpResponse queryRoutingMetricsPage(HttpRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("request query routing-metrics page API body {}", request.getBody());
        }
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8");
        String body = request.getBody();
        Result<Page<RoutingMetrics>> result;
        try {
            RoutingMetricsQueryCriteria criteria = JsonUtils.fromJson(body, RoutingMetricsQueryCriteria.class);
            validatePagingCriteria(criteria.getPagingCriteria());
            Page<RoutingMetrics> page = routingMetricsRepository.selectPage(criteria);
            result = Result.ok(page);
            httpResponse.setStatus(200);
        } catch (Exception e) {
            log.error("request query routing-metrics page error", e);
            result = Result.fail(e.getMessage());
            httpResponse.setStatus(200);
        }
        httpResponse.setBody(JsonUtils.toJson(result));
        return httpResponse;
    }

    /**
     * Queries SQL metrics with pagination.
     *
     * @param request the HttpRequest instance containing the query criteria
     * @return an HttpResponse containing the paginated SQL metrics in JSON format
     */
    @HttpHandle(path = "/v1/sql-metrics/page", method = "post")
    public HttpResponse querySqlMetricsPage(HttpRequest request) {

        if (log.isDebugEnabled()) {
            log.debug("request query sql-metrics page API body {}", request.getBody());
        }
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8");
        String body = request.getBody();
        Result<Page<SqlMetrics>> result;
        try {
            SqlMetricsQueryCriteria criteria = JsonUtils.fromJson(body, SqlMetricsQueryCriteria.class);
            validatePagingCriteria(criteria.getPagingCriteria());
            Page<SqlMetrics> page = sqlMetricsRepository.selectPage(criteria);
            result = Result.ok(page);
            httpResponse.setStatus(200);
        } catch (Exception e) {
            log.error("request query sql-metrics page error", e);
            result = Result.fail(e.getMessage());
            httpResponse.setStatus(200);
        }
        httpResponse.setBody(JsonUtils.toJson(result));
        return httpResponse;
    }

    /**
     * Queries transaction metrics with pagination.
     *
     * @param request the HttpRequest instance containing the query criteria
     * @return an HttpResponse containing the paginated transaction metrics in JSON format
     */
    @HttpHandle(path = "/v1/transaction-metrics/page", method = "post")
    public HttpResponse queryTransactionMetricsPage(HttpRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("request query transaction-metrics page API body {}", request.getBody());
        }
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8");
        String body = request.getBody();
        Result<Page<TransactionMetrics>> result;
        try {
            TransactionMetricsQueryCriteria criteria = JsonUtils.fromJson(body, TransactionMetricsQueryCriteria.class);
            validatePagingCriteria(criteria.getPagingCriteria());
            Page<TransactionMetrics> page = transactionMetricsRepository.selectPage(criteria);
            result = Result.ok(page);
            httpResponse.setStatus(200);
        } catch (Exception e) {
            log.error("request query transaction-metrics page error", e);
            result = Result.fail(e.getMessage());
            httpResponse.setStatus(200);
        }
        httpResponse.setBody(JsonUtils.toJson(result));
        return httpResponse;
    }

    /**
     * Queries table access metrics with pagination.
     *
     * @param request the HttpRequest instance containing the query criteria
     * @return an HttpResponse containing the paginated table access metrics in JSON format
     */
    @HttpHandle(path = "/v1/table-metrics/page", method = "post")
    public HttpResponse queryTableMetricsPage(HttpRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("request query table-metrics page API body {}", request.getBody());
        }
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8");
        String body = request.getBody();
        Result<Page<TableAccessMetrics>> result;
        try {
            TableMetricsQueryCriteria criteria = JsonUtils.fromJson(body, TableMetricsQueryCriteria.class);
            Page<TableAccessMetrics> page = tableAccessMetricsRepository.selectPage(criteria);
            result = Result.ok(page);
            httpResponse.setStatus(200);
        } catch (Exception e) {
            log.error("request query table-metrics page error", e);
            result = Result.fail(e.getMessage());
            httpResponse.setStatus(200);
        }
        httpResponse.setBody(JsonUtils.toJson(result));
        return httpResponse;
    }

    /**
     * Queries datasource dashboard metrics.
     *
     * @param request the HttpRequest instance containing the query criteria
     * @return an HttpResponse containing the datasource dashboard metrics in JSON format
     */
    @HttpHandle(path = "/v1/datasource-dashboard", method = "post")
    public HttpResponse queryDatasourceDashboardMetrics(HttpRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("request query datasource-dashboard API body {}", request.getBody());
        }
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8");
        String body = request.getBody();
        Result<List<DatasourceDashboardMetrics>> result;
        try {
            DatasourceDashboardMetricsQueryCriteria criteria = JsonUtils.fromJson(body, DatasourceDashboardMetricsQueryCriteria.class);
            criteria.setDatasourceList(sqlXConfiguration.getDataSourceNames());
            List<DatasourceDashboardMetrics> metricsList = nodeSqlExecuteNumMetricsRepository.selectDatasourceDashboardMetrics(criteria);
            for (DatasourceDashboardMetrics metrics : metricsList) {
                DataSourceConfiguration dataSourceConfiguration = sqlXConfiguration.getDataSourceConfByName(metrics.getDataSource());
                if (Objects.nonNull(dataSourceConfiguration)) {
                    NodeAttribute nodeAttribute = dataSourceConfiguration.getNodeAttribute();
                    metrics.setNodeState(nodeAttribute.getNodeState());
                }
            }
            result = Result.ok(metricsList);
            httpResponse.setStatus(200);
        } catch (Exception e) {
            log.error("request query datasource-dashboard error", e);
            result = Result.fail(e.getMessage());
            httpResponse.setStatus(200);
        }
        httpResponse.setBody(JsonUtils.toJson(result));
        return httpResponse;
    }

    @HttpHandle(path = "/v1/configuration", method = "get")
    public HttpResponse getConfiguration(HttpRequest request) {
        HttpResponse httpResponse = new HttpResponse("application/json;charset=UTF-8").setStatus(200);
        try {
            Result<SqlXProperties> result = Result.ok(sqlXProperties);
            httpResponse.setBody(JsonUtils.maskPasswordToJsonExcludeWithoutExpose(result));
        } catch (Exception e) {
            log.error("get Configuration error", e);
            httpResponse.setBody(JsonUtils.toJson(Result.fail(e.getMessage())));
        }

        return httpResponse;
    }

    /**
     * Validates the PagingCriteria object.
     *
     * @param pagingCriteria the PagingCriteria object to validate
     * @throws IllegalArgumentException if the PagingCriteria is null or invalid
     */
    private void validatePagingCriteria(PagingCriteria pagingCriteria) {
        if (pagingCriteria == null) {
            throw new IllegalArgumentException("PagingCriteria is required");
        }
        if (pagingCriteria.getPageNo() == null) {
            throw new IllegalArgumentException("Page number (pageNo) is required");
        }
        if (pagingCriteria.getPageNo() < 0) {
            throw new IllegalArgumentException("Page number (pageNo) must be non-negative");
        }
        if (pagingCriteria.getPageSize() == null) {
            throw new IllegalArgumentException("Page size (pageSize) is required");
        }
        if (pagingCriteria.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size (pageSize) must be positive");
        }
    }

}
