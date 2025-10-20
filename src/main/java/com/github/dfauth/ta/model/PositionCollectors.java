package com.github.dfauth.ta.model;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.identityCollector;
import static com.github.dfauth.ta.functional.Optionals.reduce;
import static java.util.Optional.empty;

@Slf4j
@AllArgsConstructor
public enum PositionCollectors {

    SORT_BY_DATE_THEN_CODE(() -> PositionCollectors.<Comparable, Comparable>sortedDoubleKeyedMapCollector(
            Position::getOpen,
                     Position::getCode)),

    SORT_BY_CODE_THEN_DATE(() -> PositionCollectors.<Comparable, Comparable>sortedDoubleKeyedMapCollector(
                    Position::getCode,
            Position::getOpen)),

    SORT_BY_PROFIT_THEN_DATE(() -> PositionCollectors.<Comparable, Comparable>sortedDoubleKeyedMapCollector(
                    p -> p.getProfit().orElseGet(() -> p.getMarketValue().map(mv -> mv.add(p.getPurchaseValue())).orElse(BigDecimal.ZERO)),
            Position::getOpen)),

    METRICS(TradingMetrics::collector),

    PROFIT(Position::getProfit),

    DIVIDEND(Position::getDividends),

    TRACK_RECORD(TrackRecord::collector),

    MARKET_VALUE(Position::getMarketValue);

    private final Supplier<Collector<Position, ?, ?>> supplier;

    PositionCollectors(Function<Position, Optional<BigDecimal>> extractor) {
        this(() -> summing(extractor));
    }
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

    public static Collector<Position, ?, Optional<BigDecimal>> summing(Function<Position, Optional<BigDecimal>> extractor) {
        return Collectors.reducing(empty(),extractor, reduce(BigDecimal::add));
    }


}
