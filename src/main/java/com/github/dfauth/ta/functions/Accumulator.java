package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.ZERO;

public interface Accumulator<T,R> {
    Accumulator<Integer,Double> INT_ACCUMULATOR = new Accumulator<>() {

        private Double acc = 0.0;
        @Override
        public Double initial() {
            return 0.0;
        }

        @Override
        public Double add(Integer i) {
            return acc +=i;
        }

        @Override
        public Double subtract(Integer i) {
            return acc -= i;
        }

        @Override
        public Double divide(int size) {
            return acc / size;
        }

        @Override
        public void set(Integer previous) {
            acc = Double.valueOf(previous);
        }
    };

    Accumulator<Double,Double> DOUBLE_ACCUMULATOR = new Accumulator<>() {

        private Double acc = 0.0;
        @Override
        public Double initial() {
            return 0.0;
        }

        @Override
        public Double add(Double d) {
            return acc +=d;
        }

        @Override
        public Double subtract(Double d) {
            return acc -= d;
        }

        @Override
        public Double divide(int size) {
            return acc / size;
        }

        @Override
        public void set(Double previous) {
            acc = previous;
        }
    };
    Accumulator<BigDecimal, BigDecimal> BD_ACCUMULATOR = new Accumulator<>() {

        private BigDecimal acc = ZERO;

        @Override
        public BigDecimal initial() {
            return ZERO;
        }

        @Override
        public BigDecimal add(BigDecimal bd) {
            acc = acc.add(bd);
            return acc;
        }

        @Override
        public BigDecimal subtract(BigDecimal bd) {
            acc = acc.subtract(bd);
            return acc;
        }

        @Override
        public BigDecimal divide(int size) {
            return acc.divide(BigDecimal.valueOf(size), 3, RoundingMode.HALF_UP);
        }

        @Override
        public void set(BigDecimal previous) {
            acc = previous;
        }
    };

    R initial();

    R add(T t);

    R subtract(T remove);

    R divide(int size);

    void set(T previous);
}
