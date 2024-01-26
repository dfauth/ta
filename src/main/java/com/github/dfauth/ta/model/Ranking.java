package com.github.dfauth.ta.model;

import java.util.Arrays;
import java.util.Optional;

public enum Ranking {
    MOMENTUM_QUARTERLY("mtm-qtr"),
    ALLORDS_MOMENTUM_QUARTERLY("xao-mtm-qtr");

    private final String code;

    Ranking(String code) {
        this.code = code;
    }

    public static Optional<Ranking> findByCode(String code) {
        return Arrays.stream(values()).filter(r -> r.name().equalsIgnoreCase(code) || r.code.equalsIgnoreCase(code)).findFirst();
    }
}
