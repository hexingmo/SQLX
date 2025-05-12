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

package com.github.sqlx.util;

import org.springframework.web.server.WebSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for handling security-related operations such as session management and resource checks.
 *
 * <p>This class provides methods to:
 * <ul>
 *     <li>Check if a given path corresponds to a static resource (e.g., HTML, CSS, JS files).</li>
 *     <li>Set the authenticated user in both traditional {@link HttpSession} and reactive {@link WebSession}.</li>
 * </ul>
 *
 * <p>Note: This utility class cannot be instantiated.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class SecurityUtils {

    /**
     * The key used to store the authenticated user's username in the session.
     */
    private static final String AUTH_SESSION_USER_KEY = "SqlXUser";

    /**
     * List of file extensions considered as static resources.
     */
    private static final List<String> STATIC_FILE_EXTENSIONS = Arrays.asList(".html", ".css", ".js");

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if the provided path ends with any of the predefined static file extensions.
     *
     * @param path The path to check.
     * @return true if the path is a static resource, false otherwise.
     */
    public static boolean isStaticResource(String path) {
        for (String ext : STATIC_FILE_EXTENSIONS) {
            if (path.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the authenticated user's username in the traditional HTTP session.
     *
     * @param session  The HTTP session.
     * @param username The username to set.
     */
    public static void setAuthSessionUserKey(HttpSession session , String username) {
        session.setAttribute(AUTH_SESSION_USER_KEY , username);
    }


    public static void setAuthSessionUserKey(WebSession webSession, String username) {
        webSession.getAttributes().put(AUTH_SESSION_USER_KEY   , username);
    }

    public static boolean containsUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(AUTH_SESSION_USER_KEY) != null;
    }

    public static boolean containsUser(WebSession session) {
        return session != null && session.getAttribute(AUTH_SESSION_USER_KEY) != null;
    }

}
