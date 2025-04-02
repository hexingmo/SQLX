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

/**
 * Blocks Font Banner
 * @author He Xing Mo
 * @since 1.0
 */
public class BlocksBanner extends AbstractBanner {

    private static final String BANNER = "\n" +
            ".----------------. .----------------. .----------------. .----------------.\n" +
            "| .--------------. | .--------------. | .--------------. | .--------------. |\n" +
            "| |    _______   | | |    ___       | | |   _____      | | |  ____  ____  | |\n" +
            "| |   /  ___  |  | | |  .'   '.     | | |  |_   _|     | | | |_  _||_  _| | |\n" +
            "| |  |  (__ \\_|  | | | /  .-.  \\    | | |    | |       | | |   \\ \\  / /   | |\n" +
            "| |   '.___`-.   | | | | |   | |    | | |    | |   _   | | |    > `' <    | |\n" +
            "| |  |`\\____) |  | | | \\  `-'  \\_   | | |   _| |__/ |  | | |  _/ /'`\\ \\_  | |\n" +
            "| |  |_______.'  | | |  `.___.\\__|  | | |  |________|  | | | |____||____| | |\n" +
            "| |              | | |              | | |              | | |              | |\n" +
            "| '--------------' | '--------------' | '--------------' | '--------------' |\n" +
            " '----------------' '----------------' '----------------' '----------------'\n";

    @Override
    protected String getBanner() {
        return BANNER;
    }
}
