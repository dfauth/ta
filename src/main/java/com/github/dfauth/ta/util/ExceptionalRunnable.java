package com.github.dfauth.ta.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.dfauth.ta.util.Promise.promise;
import static com.github.dfauth.ta.util.Promise.tryWith;

public interface ExceptionalRunnable extends Callable<Void>, Runnable {

    Executor runInCallingThread = Runnable::run;

    @Slf4j enum Logger {}

    static <T> Function<Throwable, T> propagate() {
        return e -> {
            Logger.log.error(e.getMessage(), e);
            if(e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        };
    }

    static Runnable noOp() {
        return () -> {};
    }

    static void tryCatchRunnable(ExceptionalRunnable callable) {
        tryCatch(callable);
    }

    static <T> T tryCatchSupplier(Supplier<T> supplier) {
        return tryCatch(supplier::get);
    }

    static <T> T tryCatch(Callable<T> callable) {
        return tryCatch(callable, propagate());
    }

    static <T> T tryCatch(Callable<T> callable, Function<Throwable, T> exceptionHandler) {
        return tryCatch(callable, exceptionHandler, noOp());
    }

    static <T> T tryCatch(Callable<T> callable, Function<Throwable, T> exceptionHandler, Runnable finallyRunnable) {
        return tryWith(callable).recover(exceptionHandler).andFinally(finallyRunnable).get();
    }

    static <T> T tryFinally(Supplier<T> supplier, Runnable runnable) {
        return promise(runInCallingThread, supplier::get).andFinally(runnable).get();
    }

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
