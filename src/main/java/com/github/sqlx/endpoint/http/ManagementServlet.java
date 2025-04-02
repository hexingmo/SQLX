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

import com.github.sqlx.util.IOUtils;
import com.github.sqlx.util.SecurityUtils;
import com.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public class ManagementServlet extends HttpServlet {


    private final DispatcherHttpHandler dispatcherHttpHandler;

    public ManagementServlet(DispatcherHttpHandler dispatcherHttpHandler) {
        this.dispatcherHttpHandler = dispatcherHttpHandler;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO 处理认证

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String path = getPath(req);
        boolean needLogin = !SecurityUtils.containsUser(req)
                && !path.endsWith("/login") && !path.endsWith("/login.html")
                && !path.endsWith(".css") && !path.endsWith(".js");
        if (needLogin) {
            resp.sendRedirect(req.getServletPath() + "/login.html");
            return;
        }

        String body = IOUtils.read(req.getInputStream());
        HttpRequest httpRequest = new HttpRequest()
                .setUri(req.getRequestURI())
                .setPath(path)
                .setMethod(req.getMethod())
                .setParameterMap(req.getParameterMap())
                .setHttpSession(req.getSession())
                .setBody(body);
        HttpResponse httpResponse = dispatcherHttpHandler.handle(httpRequest);
        resp.setStatus(httpResponse.getStatus());
        resp.setContentType(httpResponse.getContentType());
        if (StringUtils.isNotBlank(httpResponse.getBody())) {
            resp.getWriter().write(httpResponse.getBody());
            resp.flushBuffer();
        }
    }

    private String getPath(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        String servletPath = req.getServletPath();
        if (contextPath == null) {
            contextPath = "";
        }
        String uri = contextPath + servletPath;
        return req.getRequestURI().substring(uri.length());
    }

}
