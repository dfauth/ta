package com.github.dfauth.ta.functional;

import java.util.function.Consumer;
import java.util.function.Function;

public class Function2 {

    public static <T> Function<T,T> peek(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return t;
        };
    }
}
