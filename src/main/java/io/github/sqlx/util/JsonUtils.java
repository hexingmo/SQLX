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

package io.github.sqlx.util;

import io.github.sqlx.metrics.SortOrder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Utility class for JSON operations.
 *
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public abstract class JsonUtils {

    /**
     * Gson instance configured to serialize null values.
     */
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(SortOrder.class, new CaseInsensitiveEnumTypeAdapter<>(SortOrder.class))
            .registerTypeAdapter(Class.class, new ClassTypeJsonSerializer())
            .create();

    private static final Gson GSON_EXCLUDE_WITHOUT_EXPOSE = new GsonBuilder()
            .serializeNulls()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(SortOrder.class, new CaseInsensitiveEnumTypeAdapter<>(SortOrder.class))
            .registerTypeAdapter(Class.class, new ClassTypeJsonSerializer())
            .create();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a JSON string to a Map<String, JsonElement>.
     *
     * @param json the JSON string to be converted
     * @return a Map<String, JsonElement> representing the JSON string
     */
    public static Map<String, JsonElement> fromJson(String json) {
        return GSON.fromJson(json, new TypeToken<Map<String, JsonElement>>(){}.getType());
    }


    /**
     * Converts a JSON string to an object of the specified class.
     *
     * @param json  the JSON string to be converted
     * @param clazz the class of the object to be returned
     * @param <T>   the type of the object to be returned
     * @return an object of the specified class
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param object the object to be converted
     * @return a JSON string representation of the object
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static String toJsonExcludeWithoutExpose(Object object) {
        return GSON_EXCLUDE_WITHOUT_EXPOSE.toJson(object);
    }

    /**
     * Converts an object to a JSON string and masks any fields containing "password".
     *
     * @param object the object to be converted
     * @return a JSON string with masked password fields
     */
    public static String maskPasswordToJson(Object object) {
        return maskPassword(GSON , toJson(object));
    }

    public static String maskPasswordToJsonExcludeWithoutExpose(Object object) {
        return maskPassword(GSON_EXCLUDE_WITHOUT_EXPOSE , toJsonExcludeWithoutExpose(object));
    }

    /**
     * Masks any fields containing "password" in the given JSON string.
     *
     * @param json the JSON string to be processed
     * @return a JSON string with masked password fields
     */
    public static String maskPassword(Gson gson , String json) {
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            maskPasswordsRecursively(jsonElement);
            return gson.toJson(jsonElement);
        } catch (Exception e) {
            log.error("mask password error", e);
            return json;
        }
    }

    /**
     * Recursively masks any fields containing "password" in the given JSON element.
     *
     * @param element the JSON element to be processed
     */
    private static void maskPasswordsRecursively(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            for (String key : jsonObject.keySet()) {
                JsonElement value = jsonObject.get(key);
                if (key.toLowerCase().contains("password")) {
                    jsonObject.addProperty(key, "***");
                } else {
                    maskPasswordsRecursively(value);
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            for (JsonElement item : jsonArray) {
                maskPasswordsRecursively(item);
            }
        }
    }
}
