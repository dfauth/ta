package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Collectors.oops;

@Slf4j
public class TrackRecord {

    @Getter
    private Map<String, WinLossCounter> cache = new HashMap<>();

    public static Collector<Position, TrackRecord, Map<String, WinLossCounter>> collector() {
        return new Collector<>() {
            @Override
            public Supplier<TrackRecord> supplier() {
                return TrackRecord::new;
            }

            @Override
            public BiConsumer<TrackRecord, Position> accumulator() {
                return TrackRecord::add;
            }

            @Override
            public BinaryOperator<TrackRecord> combiner() {
                return oops();
            }

            @Override
            public Function<TrackRecord, Map<String, WinLossCounter>> finisher() {
                return TrackRecord::getCache;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

    public void add(Position p) {
        cache.compute(p.getCode(), toBiFunction(WinLossCounter::new, (wlc) -> wlc.add(p)));
    }

    private <K, V> BiFunction<K, V, V> toBiFunction(Supplier<V> supplier, UnaryOperator<V> f) {
        return (k,v) -> Optional.ofNullable(v).map(f).orElse(f.apply(supplier.get()));
    }

    @RequiredArgsConstructor
    public static class WinLossCounter {

        @Getter
        @JsonProperty("w")
        private final int winners;
        @Getter
        @JsonProperty("l")
        private final int losers;

        public WinLossCounter() {
            this(0, 0);
        }

        public WinLossCounter add(Position p) {
            return p.isProfitable() ?
                    new WinLossCounter(winners+1, losers) : new WinLossCounter(winners, losers+1);
        }
    }
}
