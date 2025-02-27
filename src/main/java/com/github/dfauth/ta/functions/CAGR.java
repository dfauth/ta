package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static java.lang.Math.pow;

public class CAGR {

    public static double cagr(double pctRtn, double periods) {
        double x = (1.0d / periods);
        return pow(1.0d + pctRtn,x)  - 1.0d;
    }

    public static <T,R> R cagr(T pctRtn, double periods, BiFunction<T, UnaryOperator<Double>,R> mapper) {
        return mapper.apply(pctRtn, z -> cagr(z,periods));
    }

    public static BiFunction<Double, UnaryOperator<Double>, Optional<BigDecimal>> bdMapper(int scale) {
        return (d, o) -> Optional.of(d).map(o).filter(_d -> !Double.isNaN(_d)).map(_d -> BigDecimal.valueOf(_d).setScale(scale, RoundingMode.HALF_UP));
//        return (d, o) -> tryWith(() -> o.apply(d))
//                .map(_t -> BigDecimal.valueOf(_t).setScale(scale, RoundingMode.HALF_UP));
    }
}
