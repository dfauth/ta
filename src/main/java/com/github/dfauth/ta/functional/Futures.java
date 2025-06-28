package com.github.dfauth.ta.functional;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

@Slf4j
public class Futures {

    private static String thingy = Futures.class.getPackage().getImplementationVersion();

    public static <T> Collector<CompletableFuture<T>, ?, CompletableFuture<List<T>>> futures() {
        return futures(new ArrayList<>(), (l,t) -> l.thenApply(_l -> Lists.add(_l, t)), Lists::add);
    }

    public static <T,R> Collector<CompletableFuture<T>, ?, CompletableFuture<R>> futures(R initial, BiFunction<CompletableFuture<R>,T,CompletableFuture<R>> mergingFunction, BinaryOperator<R> combiningFunction) {
        return futures(CompletableFuture.completedFuture(initial), mergingFunction, combiningFunction);
    }

    public static <T,R> Collector<CompletableFuture<T>, ?, CompletableFuture<R>> futures(CompletableFuture<R> initial, BiFunction<CompletableFuture<R>,T,CompletableFuture<R>> mergingFunction, BinaryOperator<R> combiningFunction) {
        return new Collector<CompletableFuture<T>, AtomicReference<CompletableFuture<R>>, CompletableFuture<R>>(){
            @Override
            public Supplier<AtomicReference<CompletableFuture<R>>> supplier() {
                return () -> new AtomicReference<>(initial);
            }

            @Override
            public BiConsumer<AtomicReference<CompletableFuture<R>>, CompletableFuture<T>> accumulator() {
                return (ref, fut) -> fut.thenAccept(t -> {
                    var r = new CompletableFuture<R>();
                    ref.getAndSet(mergingFunction.apply(r,t)).thenAccept(r::complete);
                });
            }

            @Override
            public BinaryOperator<AtomicReference<CompletableFuture<R>>> combiner() {
                return (l,r) -> new AtomicReference<>(l.get().thenCompose(leftValue -> r.get().thenApply(rightValue -> combiningFunction.apply(leftValue, rightValue))));
            }

            @Override
            public Function<AtomicReference<CompletableFuture<R>>, CompletableFuture<R>> finisher() {
                return AtomicReference::get;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

}
