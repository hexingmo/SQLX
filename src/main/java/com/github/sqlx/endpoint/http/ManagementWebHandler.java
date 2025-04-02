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

import com.github.sqlx.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.WebSession;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A WebHandler implementation for managing HTTP requests in a reactive manner.
 * This handler reads the request body, processes it using a DispatcherHttpHandler,
 * and sends the appropriate response back to the client.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class ManagementWebHandler implements WebHandler {

    /**
     * The DispatcherHttpHandler used to handle the processed HTTP request.
     */
    private final DispatcherHttpHandler dispatcherHttpHandler;

    private final String handlerPath;

    /**
     * Constructs a new ManagementWebHandler with the given DispatcherHttpHandler.
     *
     * @param dispatcherHttpHandler the DispatcherHttpHandler to use for request processing
     */
    public ManagementWebHandler(String handlerPath , DispatcherHttpHandler dispatcherHttpHandler) {
        this.handlerPath = handlerPath;
        this.dispatcherHttpHandler = dispatcherHttpHandler;
    }

    /**
     * Handles the incoming HTTP request.
     * Reads the request body, processes it, and sends the response.
     *
     * @param exchange the ServerWebExchange containing the request and response
     * @return a Mono<Void> representing the completion of the request handling
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {

        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        MediaType contentType = headers.getContentType();
        if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> Mono.just(readBody(dataBuffer)))
                    .switchIfEmpty(Mono.just(""))
                    .flatMap(body -> handleRequest(request, body, exchange));
        } else {
            return handleRequest(request, "", exchange);
        }
    }

    public Mono<ServerResponse> handleFunction(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        MediaType contentType = headers.getContentType();

        String path = getPath(request);
        
        return exchange.getSession().flatMap(session -> {
            boolean hasUser = SecurityUtils.containsUser(session);
            boolean needLogin = !hasUser
                    && !path.endsWith("/login") && !path.endsWith("/login.html")
                    && !path.endsWith(".css") && !path.endsWith(".js");

            // TODO 还有问题
            if (needLogin) {
                return ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create(handlerPath + "/login.html"))
                        .build();
            }
            
            Mono<String> bodyMono;
            if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
                bodyMono = DataBufferUtils.join(request.getBody())
                        .map(this::readBody)
                        .switchIfEmpty(Mono.just(""));
            } else {
                bodyMono = Mono.just("");
            }

            return bodyMono.flatMap(body -> {
                Mono<MultiValueMap<String, String>> formData = exchange.getFormData();
                return formData.flatMap(data -> {
                    HttpRequest httpRequest = getHttpRequest(exchange, data, body, session);
                    HttpResponse httpResponse = dispatcherHttpHandler.handle(httpRequest);
                    MediaType responseContentType = new MediaType(MediaType.parseMediaType(httpResponse.getContentType()), StandardCharsets.UTF_8);
                    return ServerResponse.status(httpResponse.getStatus())
                            .contentType(responseContentType)
                            .bodyValue(httpResponse.getBody());
                });
            });
        });
    }

    private HttpRequest getHttpRequest(ServerWebExchange exchange, MultiValueMap<String, String> formData, String body, WebSession session) {
        ServerHttpRequest request = exchange.getRequest();
        String path = getPath(request);
        Map<String, String[]> parameterMap = convertMultiValueMapToMap(formData);
        return new HttpRequest()
                .setUri(request.getURI().toString())
                .setPath(path)
                .setMethod(request.getMethodValue())
                .setParameterMap(parameterMap)
                .setWebSession(session)
                .setBody(body);
    }

    private String getPath(ServerHttpRequest request) {
        RequestPath requestPath = request.getPath();
        String fullPath = requestPath.value();
        PathContainer contextPathContainer = requestPath.contextPath();
        String contextPath = "";
        if (Objects.nonNull(contextPathContainer)) {
            contextPath = contextPathContainer.value();
        }
        return fullPath.substring(contextPath.length() + handlerPath.length());
    }

    /**
     * Reads the body of the request from a DataBuffer and converts it to a String.
     *
     * @param dataBuffer the DataBuffer containing the request body
     * @return the request body as a String
     */
    private String readBody(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);
        String body = new String(bytes, StandardCharsets.UTF_8);
        if (log.isDebugEnabled()) {
            log.debug("Request body: {}", body);
        }
        return body;
    }

    /**
     * Processes the request and sends the response.
     *
     * @param request the ServerHttpRequest containing the request details
     * @param body the request body as a String
     * @param exchange the ServerWebExchange containing the request and response
     * @return a Mono<Void> representing the completion of the request handling
     */
    private Mono<Void> handleRequest(ServerHttpRequest request, String body, ServerWebExchange exchange) {
        Mono<MultiValueMap<String, String>> formData = exchange.getFormData();
        return formData.flatMap(data -> {
            Map<String, String[]> parameterMap = convertMultiValueMapToMap(data);

            HttpRequest httpRequest = new HttpRequest()
                    .setUri(request.getURI().toString())
                    .setPath(getPath(request))
                    .setMethod(request.getMethodValue())
                    .setParameterMap(parameterMap)
                    .setBody(body);

            HttpResponse httpResponse = dispatcherHttpHandler.handle(httpRequest);

            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(httpResponse.getStatus());
            HttpHeaders headers = response.getHeaders();
            headers.add("Content-Type", httpResponse.getContentType());

            DataBuffer responseBuffer = response.bufferFactory()
                    .wrap(httpResponse.getBody().getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(responseBuffer));
        });
    }

    private Map<String, String[]> convertMultiValueMapToMap(MultiValueMap<String, String> multiValueMap) {
        return multiValueMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toArray(new String[0])
                ));
    }
}
