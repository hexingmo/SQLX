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
package io.github.sqlx.endpoint.http;

import io.github.sqlx.util.JsonUtils;
import io.github.sqlx.util.StringUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.server.WebSession;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class HttpRequest {

    private String uri;

    private String path;

    private String method;

    private Map<String, String[]> parameterMap;

    private String body;

    private HttpSession httpSession;

    private WebSession webSession;

    public String getParameter(String name) {
        if (parameterMap == null) {
            return null;
        }
        String[] value = parameterMap.get(name);
        if (value == null) {
            return null;
        }
        return value[0];
    }


    public <T> T getBodyBean(Class<T> clazz) {
        if (StringUtils.isBlank(body)) {
            throw new IllegalArgumentException("body is empty");
        }
        return JsonUtils.fromJson(body, clazz);
    }
}
