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

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class Result<T> {

    @Expose
    private String msg;

    @Expose
    private T payload;

    @Expose
    private Boolean succeed;

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setSucceed(true).setMsg("success");
        return result;
    }

    public static <T> Result<T> ok(T payload) {
        Result<T> result = new Result<>();
        result.setSucceed(true).setPayload(payload).setMsg("success");
        return result;
    }

    public static <T> Result<T> fail(T payload , String msg) {
        Result<T> result = new Result<>();
        result.setSucceed(false).setPayload(payload).setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setSucceed(false).setMsg(msg);
        return result;
    }

}
