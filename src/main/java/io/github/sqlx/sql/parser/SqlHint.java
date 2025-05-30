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

package io.github.sqlx.sql.parser;

import lombok.Data;

import java.util.Map;

/**
 *
 * @author He Xing Mo
 * @since 1.0
 */

@Data
public class SqlHint {

    /**
     * key is hint key , value is hint value
     */
    private Map<String , String> hints;

    private String nativeSql;

    public SqlHint() {
    }

    public SqlHint(Map<String, String> hints, String nativeSql) {
        this.hints = hints;
        this.nativeSql = nativeSql;
    }
}
