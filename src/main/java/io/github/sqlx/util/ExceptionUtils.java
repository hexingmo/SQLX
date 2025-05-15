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

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for exception-related operations.
 * Provides methods to retrieve the root cause of an exception and to format exception messages.
 * 
 * @author He Xing Mo
 * @since 1.0
 */
public class ExceptionUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ExceptionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Retrieves a list of all throwables in the causal chain of the given throwable.
     * 
     * @param throwable the throwable to analyze
     * @return a list of throwables in the causal chain
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    /**
     * Retrieves the root cause of the given throwable.
     * 
     * @param throwable the throwable to analyze
     * @return the root cause of the throwable, or null if none found
     */
    public static Throwable getRootCause(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /**
     * Retrieves the message of the root cause of the given throwable.
     * 
     * @param th the throwable to analyze
     * @return the message of the root cause
     */
    public static String getRootCauseMessage(final Throwable th) {
        Throwable root = getRootCause(th);
        root = root == null ? th : root;
        return getMessage(root);
    }

    /**
     * Retrieves the message of the given throwable, including its class name.
     * 
     * @param th the throwable to analyze
     * @return the formatted message of the throwable
     */
    public static String getMessage(final Throwable th) {
        if (th == null) {
            return "";
        }
        final String clsName = th.getClass().getName();
        final String msg = th.getMessage();
        return clsName + ": " + StringUtils.defaultIfBlank(msg, "");
    }
}
