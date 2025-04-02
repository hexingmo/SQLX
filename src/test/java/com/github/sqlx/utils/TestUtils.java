package com.github.sqlx.utils;

import java.util.Objects;

/**
 * test utils
 *
 * @author jing yun
 * @since 1.0
 */
public class TestUtils {
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (Objects.nonNull(resource)) {
                runInTry(resource::close);
            }
        }
    }

    public static void runInTry(Runnable runnable){
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T callInTry(Callable<T> callable){
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @FunctionalInterface
    public interface Runnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface Callable<T> {
        T call() throws Exception;
    }

}
