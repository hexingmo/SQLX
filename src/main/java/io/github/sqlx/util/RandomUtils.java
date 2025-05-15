package io.github.sqlx.util;


import java.util.Random;

public class RandomUtils {

    private static final Random RANDOM = new Random();

    private RandomUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static int nextInt(final int startInclusive, final int endExclusive) {
        if (startInclusive == endExclusive) {
            return startInclusive;
        }

        return startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
    }
}
