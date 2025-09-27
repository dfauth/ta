package com.github.dfauth.ta.model;

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

    SORT_BY_DATE_THEN_CODE(() -> PositionCollectors.<Comparable, Comparable>sortedDoubleKeyedMapCollector(
                     p -> p.getDate().toLocalDateTime().toLocalDate(),
                     Position::getCode)),

    SORT_BY_CODE_THEN_DATE(() -> PositionCollectors.<Comparable, Comparable>sortedDoubleKeyedMapCollector(
                    Position::getCode,
                     p -> p.getDate().toLocalDateTime().toLocalDate())),

    METRICS(TradingMetrics::collector);

    private final Supplier<Collector<Position, ?, ?>> supplier;

    public static <T> Collector<Position, Object, T> defaultCollector() {
        return SORT_BY_DATE_THEN_CODE.toCollector();
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
