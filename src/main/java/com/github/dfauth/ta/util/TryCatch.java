package com.github.dfauth.ta.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class TryCatch {

    public static void tryCatchRunnable(ExceptionalRunnable callable) {
        tryCatch(callable);
    }

    public static <T> T tryCatchSupplier(Supplier<T> supplier) {
        return tryCatch(supplier::get);
    }

    public static <T> T tryCatch(Callable<T> callable) {
        return tryCatch(callable, propagate());
    }

    public static <T> T tryCatch(Callable<T> callable, Function<Exception,T> exceptionHandler) {
        return tryCatch(callable, exceptionHandler, noOp());
    }

    public static <T> T tryCatch(Callable<T> callable, Function<Exception,T> exceptionHandler, Runnable finallyRunnable) {
        try {
            return callable.call();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return exceptionHandler.apply(e);
        } finally {
            finallyRunnable.run();
        }
    }

    public static <T> T tryFinally(Supplier<T> supplier, Runnable runnable) {
        try {
            return supplier.get();
        } finally {
            runnable.run();
        }
    }

    public static <T> Function<Exception, T> propagate() {
        return e -> {
            throw new RuntimeException(e);
        };
    }

    private static Runnable noOp() {
        return () -> {};
    }

    public interface ExceptionalRunnable extends Callable<Void>, Runnable {

        @Override
        default Void call() throws Exception {
            _run();
            return null;
        }

        default void run() {
            tryCatchRunnable(this);
        }

        void _run() throws Exception;
    }
}
