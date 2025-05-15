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

package io.github.sqlx.banner;

/**
 * Ivrit Font Banner.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class IvritFontBanner extends AbstractBanner {

    private static final String BANNER = "   _____  ____  _     __   __\n" +
            "  / ____|/ __ \\| |    \\ \\ / /\n" +
            " | (___ | |  | | |     \\ V / \n" +
            "  \\___ \\| |  | | |      > <  \n" +
            "  ____) | |__| | |____ / . \\ \n" +
            " |_____/ \\___\\_\\______/_/ \\_\\";

    @Override
    protected String getBanner() {
        return BANNER;
    }
}
