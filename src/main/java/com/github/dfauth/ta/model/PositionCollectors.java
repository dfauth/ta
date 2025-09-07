package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.ComparableWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.identityCollector;

@Slf4j
@AllArgsConstructor
public enum PositionCollectors {

    DOUBLE_KEYED_MAP(() -> PositionCollectors.<Comparable, Comparable>sortedDoubleKeyedMapCollector(
                     p -> new ComparableWrapper<>(p.getDate().toLocalDateTime().toLocalDate(), java.time.LocalDate::compareTo),
                     Position::getCode)),

    METRICS(TradingMetrics::collector);

    private final Supplier<Collector<Position, ?, ?>> supplier;

    public static <T> Collector<Position, Object, T> defaultCollector() {
        return DOUBLE_KEYED_MAP.toCollector();
    }

    public <R> Collector<Position, Object, R> toCollector() {
        return (Collector<Position, Object, R>) supplier.get();
    }

    public static <K extends Comparable<K>, L extends Comparable<L>> Collector<Position, ?, Map<K, Map<L, Position>>> sortedDoubleKeyedMapCollector(Function<Position, K> primaryKeyMapper,
                                                                                                                                              Function<Position, L> secondaryKeyMapper) {
        return Collectors.groupingBy(primaryKeyMapper,
                TreeMap::new,
                Collectors.groupingBy(secondaryKeyMapper,
                        TreeMap::new,
                        identityCollector()));
    }


}
