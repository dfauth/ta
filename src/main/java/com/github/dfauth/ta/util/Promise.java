package com.github.dfauth.ta.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.ta.util.TryCatch.*;

@Slf4j
public class Promise<T> {

    public static <T> BiFunction<T,Throwable,T> handleWith(Function<Throwable, T> exceptionHandler) {
        return handleWith(Function.identity(), exceptionHandler);
    }

    public static <T,R> BiFunction<T,Throwable,R> handleWith(Function<T,R> f, Function<Throwable, R> exceptionHandler) {
        return (t, e) -> Optional.ofNullable(e).map(exceptionHandler).orElseGet(() -> f.apply(t));
    }

    public static <T> Promise<T> promise(Callable<T> callable) {
        return promise(ForkJoinPool.commonPool(), callable);
    }

    public static <T> Promise<T> promise(Executor executor, Callable<T> callable) {
        return new Promise<>(executor, callable);
    }

    private final CompletableFuture<T> f;

    public Promise(Executor executor, Callable<T> callable) {
        this(executor, callable, propagate(), noOp());
    }

    public Promise(CompletableFuture<T> f) {
        this.f = f;
    }

    public Promise(Executor executor, Callable<T> callable, Function<Throwable, T> exceptionHandler, Runnable finallyRunnable) {
        this(new CompletableFuture<>());
        executor.execute(() -> {
            try {
                f.complete(callable.call());
            } catch (Exception e) {
                try {
                    log.error(e.getMessage(), e);
                    f.complete(exceptionHandler.apply(e));
                } catch (Exception ex) {
                    f.completeExceptionally(ex);
                }
            } finally {
                finallyRunnable.run();
            }
        });
    }

    public Promise<T> recover(Function<Throwable, T> exceptionHandler) {
        return new Promise<>(f.handle(handleWith(exceptionHandler)));
    }

    public Promise<T> andFinally(Runnable finallyRunnable) {
        finallyRunnable.run();
        return this;
    }

    public T get() {
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPending() {
        return !isDone();
    }

    public boolean isDone() {
        return f.isDone();
    }

    public <R> Promise<R> map(Function<T,R> f) {
        return new Promise<>(this.f.thenApply(f));
    }

    public <R> Promise<R> flatMap(Function<T,Promise<R>> f) {
        CompletableFuture<R> fut = new CompletableFuture<>();
        this.f.thenApply(f).thenAccept(p -> p.map(fut::complete));
        return new Promise<>(fut);
    }
}
