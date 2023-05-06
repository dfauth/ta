package com.github.dfauth.ta.functions;

import java.util.function.BinaryOperator;

public class Reducers {

    public static <T> BinaryOperator<T> latest() {
        return (t1,t2) -> t2;
    }
}
