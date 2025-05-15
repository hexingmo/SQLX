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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public abstract class TimeUtils {

    private TimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static LocalDateTime convertToLocalDateTime(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static String formatMillisTime(long millis , String pattern) {
        Instant instant = Instant.ofEpochMilli(millis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public static Duration duration(long startNanoTime, long endNanoTime) {
        return Duration.between(nanoToInstant(startNanoTime), nanoToInstant(endNanoTime));
    }

    public static double durationSeconds(long startMillisTime, long endMillisTime) {
        return (endMillisTime - startMillisTime) / 1000.0;
    }

    public static long durationMillis(long timeElapsedNanos) {
        BigDecimal nanos = BigDecimal.valueOf(timeElapsedNanos);
        BigDecimal millis = nanos.divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP);
        return millis.longValue();
    }

    public static Instant nanoToInstant(long nanoTime) {
        long seconds = nanoTime / 1_000_000_000;
        long nanos = nanoTime % 1_000_000_000;
        return Instant.ofEpochSecond(seconds, nanos);
    }

    public static long convertToMillis(LocalDateTime dateTime) {
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }
}
