package com.github.dfauth.ta.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum Ranking {
    MOMENTUM_QUARTERLY("mtm-qtr"),
    ALLORDS_MOMENTUM_QUARTERLY("xao-mtm-qtr");

    public static Function<RankListDateCodeComposite, BiFunction<String, Map<LocalDate,Integer>, Map<LocalDate,Integer>>> reMapper = rldc -> (c, r1) -> Optional.ofNullable(r1).map(_r -> {
        _r.put(rldc.getDate().toLocalDateTime().toLocalDate(), rldc.getRank());
        return _r;
    }).orElseGet(() -> {
        HashMap<LocalDate, Integer> _r = new HashMap<>();
        _r.put(rldc.getDate().toLocalDateTime().toLocalDate(), rldc.getRank());
        return _r;
    });
    private final String code;

    Ranking(String code) {
        this.code = code;
    }

    public static Optional<Ranking> findByCode(String code) {
        return Arrays.stream(values()).filter(r -> r.name().equalsIgnoreCase(code) || r.code.equalsIgnoreCase(code)).findFirst();
    }
}
