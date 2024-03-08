package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.Candlestick;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Slf4j
public class GapUp {

    private int cnt;
    private Candlestick prev;

    public static Collector<Candlestick, GapUp, Integer> collector() {
        return new Collector<>() {
            @Override
            public Supplier<GapUp> supplier() {
                return GapUp::new;
            }

            @Override
            public BiConsumer<GapUp, Candlestick> accumulator() {
                return GapUp::accumulate;
            }

            @Override
            public BinaryOperator<GapUp> combiner() {
                return (gu1, gu2) -> {
                    throw new UnsupportedOperationException();
                };
            }

            @Override
            public Function<GapUp, Integer> finisher() {
                return GapUp::getCount;
            }

            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }

    public void accumulate(Candlestick curr) {
        if(prev != null && curr.gapUp(prev)) {
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
