package com.github.dfauth.ta.model;

import com.github.dfauth.ta.model.txn.Payment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Maps.listMerge;
import static com.github.dfauth.ta.model.Dated.dated;
import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;

@Slf4j
@AllArgsConstructor
public enum PaymentCollectors {

    PROGRESSION(() -> list(p -> dated(p.getDate(), p.getBalance()))),

    LIST(PaymentCollectors::list);

    private final Supplier<Collector<Payment, ?, ?>> supplier;

    public static <R> Collector<Payment, Object, R> defaultCollector() {
        return (Collector<Payment, Object, R>) LIST.collector();
    }

    public <T> Collector<Payment, Object, T> collector() {
        return (Collector<Payment, Object, T>) supplier.get();
    }

    public static Collector<Payment, List<Payment>, List<Payment>> list() {
        return list(identity());
    }

    public static <T> Collector<Payment, List<T>, List<T>> list(Function<Payment, T> f) {
        return list(f, identity());
    }

    public static <T,R> Collector<Payment, List<T>, R> list(Function<Payment, T> f, Function<List<T>,R> finisher) {
        return new Collector<>() {

            @Override
            public Supplier<List<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<T>, Payment> accumulator() {
                return (l, p) -> l.add(f.apply(p));
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return listMerge();
            }

            @Override
            public Function<List<T>, R> finisher() {
                return finisher;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return emptySet();
            }
        };
    }
}
