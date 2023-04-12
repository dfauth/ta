package com.github.dfauth.ta.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class MovingAverages {

    public static <T extends Number, R extends Number> Function<T, R> sma(int period, Accumulator<T,R> ops) {
        LinkedList<T> l = new LinkedList<>();
        return t -> {
            l.add(t);
            ops.add(t);
            if(l.size() > period) {
                ops.subtract(l.remove(0));
                return ops.divide(l.size());
            } else {
                return ops.initial();
            }
        };
    }
}
