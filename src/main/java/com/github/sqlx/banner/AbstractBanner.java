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

package com.github.sqlx.banner;

import com.github.sqlx.Version;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;

/**
 * An abstract implementation of the Banner interface, providing basic functionality for printing a banner.
 * This class encapsulates the logic for printing the banner, including the version information of SQL Routing,
 * and uses the Spring Boot AnsiOutput utility to style the banner text.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class AbstractBanner implements Banner {

    /**
     * Prints the banner to the specified printer.
     * This method constructs the banner string by combining the result of the getBanner method with version information,
     * and then prints it using the provided printer.
     *
     * @param printer The printer used to output the banner, defining where and how the banner is printed.
     */
    @Override
    public void printBanner(Printer printer) {
        String version = Version.getVersion();
        String sb = getBanner() + AnsiOutput.toString(AnsiColor.GREEN, " :: SQLX :: ", AnsiColor.DEFAULT,
                AnsiStyle.FAINT, version);
        printer.print(sb);
    }


    /**
     * Gets the banner string.
     * This method should be implemented by subclasses to return the specific content of the banner.
     *
     * @return The specific content of the banner.
     */
    protected abstract String getBanner();
}
