package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class CAGR {

    public static double cagr(double pctRtn, double periods) {
        double x = (1.0d / periods);
        return Math.pow((1.0d + pctRtn),x)  - 1.0d;
    }

    public static <T,R> R cagr(T pctRtn, double periods, BiFunction<T, UnaryOperator<Double>,R> mapper) {
        double x = (1.0d / periods) - 1.0d;
        return mapper.apply(pctRtn, z -> Math.pow((1.0d + z),x));
    }

    public static BiFunction<Double, UnaryOperator<Double>, BigDecimal> bdMapper(int scale) {
        return (d, o) -> BigDecimal.valueOf(o.apply(d)).setScale(scale, RoundingMode.HALF_UP);
    }
}
