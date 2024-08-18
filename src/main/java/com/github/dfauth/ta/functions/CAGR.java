package com.github.dfauth.ta.functions;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class CAGR {

    double cagr(double pctRtn, int periods) {
        double x = (1.0d / periods) - 1.0d;
        return Math.pow((1.0d + pctRtn),x);
    }

    <T> T cagr(T pctRtn, int periods, BiFunction<T, UnaryOperator<Double>,T> mapper) {
        double x = (1.0d / periods) - 1.0d;
        return mapper.apply(pctRtn, z -> Math.pow((1.0d + z),x));
    }

}
