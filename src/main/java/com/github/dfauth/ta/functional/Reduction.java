package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.txn.Payment;

import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.reducing;

public enum Reduction implements Supplier<Collector<?,?,?>> {
    SUM_PAYMENTS(() -> reducing(ZERO, Payment::getValue, BigDecimal::add));

    private BiFunction<BigDecimal,Payment,BigDecimal> f2;
    private Supplier<Collector<?,?,?>> supplier;

    Reduction(Supplier<Collector<?,?,?>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Collector<?, ?, ?> get() {
        return supplier.get();
    }
}
