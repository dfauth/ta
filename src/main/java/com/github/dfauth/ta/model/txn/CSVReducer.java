package com.github.dfauth.ta.model.txn;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static java.util.function.UnaryOperator.identity;

public class CSVReducer<T,R> implements Collector<String,T,R> {

    private final T accumulator;
    private final Function<T, R> finisher;
    private BiConsumer<T, String>[] fieldHandlers;
    private int i = 0;

    public CSVReducer(T accumulator, BiConsumer<T,String>[] fieldHandlers, Function<T,R> finisher) {
        this.accumulator = accumulator;
        this.fieldHandlers = fieldHandlers;
        this.finisher = finisher;
    }

    public static <T> CSVReducer<T,T> csvReducer(T accumulator, BiConsumer<T,String>[] fieldHandlers) {
        return new CSVReducer<>(accumulator, fieldHandlers, identity());
    }

    @Override
    public Supplier<T> supplier() {
        return () -> accumulator;
    }

    @Override
    public BiConsumer<T, String> accumulator() {
        return (t,s) -> {
            fieldHandlers[i].accept(accumulator, s);
            i = (i+1)%fieldHandlers.length;
        };
    }

    @Override
    public BinaryOperator<T> combiner() {
        return oops();
    }

    @Override
    public Function<T, R> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
