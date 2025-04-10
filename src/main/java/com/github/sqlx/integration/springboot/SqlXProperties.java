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

import com.github.sqlx.banner.Banner;
import com.github.sqlx.banner.BlocksBanner;
import com.github.sqlx.banner.IvritFontBanner;
import com.github.sqlx.config.SqlXConfiguration;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author He Xing Mo
 * @since 1.0
 */

@ConfigurationProperties(prefix = "sqlx")
@Data
@Slf4j
public class SqlXProperties implements InitializingBean {

    @Expose
    private SqlXConfiguration config;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (config.isEnabled()) {
            Banner banner = new BlocksBanner();
            banner.printBanner(log::info);
            config.init();
            config.validate();
        }
    }
}
