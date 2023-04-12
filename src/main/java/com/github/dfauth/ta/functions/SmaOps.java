package com.github.dfauth.ta.functions;

public interface SmaOps<T,R> {
    SmaOps<Integer,Double> INT_OPS = new SmaOps<>() {
        @Override
        public Double initial() {
            return 0.0;
        }

        @Override
        public Double add(Double acc, Integer i) {
            return acc + i;
        }

        @Override
        public Double subtract(Double acc, Integer i) {
            return acc - i;
        }

        @Override
        public Double divide(Double acc, int size) {
            return acc / size;
        }
    };

    R initial();

    R add(R sum, T t);

    R subtract(R sum, T remove);

    R divide(R sum, int size);
}
