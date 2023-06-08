package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_UP;

public class HighLow {

    public static Optional<BigDecimal> max(List<BigDecimal> values) {
        return values.stream().max(BigDecimal::compareTo);
    }

    public static Optional<BigDecimal> min(List<BigDecimal> values) {
        return values.stream().max(BigDecimal::compareTo);
    }

    public static Optional<BigDecimal> pctBelowMax(List<BigDecimal> values, BigDecimal current) {
        return max(values).map(v -> ONE.setScale(v.scale()).subtract(v.divide(current, HALF_UP)));
    }

    public static Optional<BigDecimal> pctAboveMin(List<BigDecimal> values, BigDecimal current) {
        return max(values).map(v -> current.divide(v, HALF_UP).subtract(ONE.setScale(v.scale())));
    }
}
