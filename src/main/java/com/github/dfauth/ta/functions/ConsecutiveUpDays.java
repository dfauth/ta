package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.model.Candlestick;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ConsecutiveUpDays {

    private int cnt;
    private Candlestick prev;

    public static Collector<Candlestick, ConsecutiveUpDays, Integer> collector() {
        return new Collector<>() {
            @Override
            public Supplier<ConsecutiveUpDays> supplier() {
                return ConsecutiveUpDays::new;
            }

            @Override
            public BiConsumer<ConsecutiveUpDays, Candlestick> accumulator() {
                return ConsecutiveUpDays::accumulate;
            }

            @Override
            public BinaryOperator<ConsecutiveUpDays> combiner() {
                return (gu1, gu2) -> {
                    throw new UnsupportedOperationException();
                };
            }

            @Override
            public Function<ConsecutiveUpDays, Integer> finisher() {
                return ConsecutiveUpDays::getCount;
            }

            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }

    public void accumulate(Candlestick curr) {
        if(prev != null && curr.closedHigher(prev)) {
            cnt++;
        } else {
            cnt = 0;
        }
        prev = curr;
    }

    public int getCount() {
        return cnt;
    }
}
