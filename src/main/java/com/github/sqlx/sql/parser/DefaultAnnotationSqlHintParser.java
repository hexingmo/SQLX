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

package com.github.sqlx.sql.parser;

import com.github.sqlx.exception.SqlParseException;
import com.github.sqlx.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class DefaultAnnotationSqlHintParser implements AnnotationSqlHintParser {

    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("/\\*!(.*?)\\*/");

    private static final String HINT_DELIMITER_REGEX = ";";

    private static final String HINT_KEY_VALUE_DELIMITER = "=";

    @Override
    public SqlHint parse(String sql) {

        if (StringUtils.isBlank(sql)) {
            return new SqlHint(new HashMap<>() , sql);
        }

        String hint = null;
        Matcher matcher = ANNOTATION_PATTERN.matcher(sql);
        if (matcher.find()) {
            hint = matcher.group(1);
        }
        String nativeSql = matcher.replaceAll("").trim();
        return new SqlHint(splitHint(hint) , nativeSql);
    }

    private Map<String , String> splitHint(String hintString) {
        if (hintString == null || hintString.length() == 0) {
            return null;
        }

        String[] hints = hintString.split(HINT_DELIMITER_REGEX);
        if (hints.length == 0) {
            return null;
        }

        Map<String , String> hintMap = new HashMap<>();
        for (String hint : hints) {
            String[] kv = hint.split(HINT_KEY_VALUE_DELIMITER);
            if (kv.length != 2) {
                throw new SqlParseException("hint key and value in SQL annotations must be separated by an equal sign (=)");
            }
            hintMap.put(kv[0].trim() , kv[1].trim());
        }

        return hintMap;
    }
}
