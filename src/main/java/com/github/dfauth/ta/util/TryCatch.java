package com.github.dfauth.ta.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.dfauth.ta.util.Promise.promise;

@Slf4j
public class TryCatch {

    public static Executor runInCallingThread = Runnable::run;

    public static Promise<Void> tryCatchAsync(ExceptionalRunnable runnable) {
        return tryCatchAsync(() -> {
            runnable._run();
            return null;
        });
    }

    public static <T> T tryCatchAsync(Callable<T> callable) {
        return tryCatch(command -> ForkJoinPool.commonPool().execute(command), callable);
    }

    public static void tryCatchRunnable(ExceptionalRunnable callable) {
        tryCatch(callable);
    }

    public static <T> T tryCatchSupplier(Supplier<T> supplier) {
        return tryCatch(supplier::get);
    }

    public static <T> T tryCatch(Callable<T> callable) {
        return tryCatch(runInCallingThread, callable);
    }

    public static <T> T tryCatch(Executor executor, Callable<T> callable) {
        return tryCatch(executor, callable, propagate());
    }

    public static <T> T tryCatch(Callable<T> callable, Function<Throwable,T> exceptionHandler) {
        return tryCatch(runInCallingThread, callable, exceptionHandler);
    }

    public static <T> T tryCatch(Executor executor, Callable<T> callable, Function<Throwable,T> exceptionHandler) {
        return tryCatch(executor, callable, exceptionHandler, noOp());
    }

    public static <T> T tryCatch(Callable<T> callable, Function<Throwable,T> exceptionHandler, Runnable finallyRunnable) {
        return tryCatch(runInCallingThread, callable, exceptionHandler, finallyRunnable);
    }

    public static <T> T tryCatch(Executor executor, Callable<T> callable, Function<Throwable,T> exceptionHandler, Runnable finallyRunnable) {
        return promise(executor, callable).recover(exceptionHandler).andFinally(finallyRunnable).get();
    }

    public static <T> T tryFinally(Supplier<T> supplier, Runnable runnable) {
        return promise(runInCallingThread, supplier::get).andFinally(runnable).get();
    }

    public static <T> Function<Throwable, T> propagate() {
        return e -> {
            if(e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        };
    }

    public static Runnable noOp() {
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
