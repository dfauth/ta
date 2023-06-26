package com.github.dfauth.ta.functional;

import java.util.function.Function;

public interface SimpleReducer<T, R> extends Reducer<T, R, R> {

    @Override
    default Function<R, R> finisher() {
        return Function.identity();
    }
}
