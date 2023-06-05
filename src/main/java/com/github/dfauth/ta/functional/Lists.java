package com.github.dfauth.ta.functional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Lists {

    public static <T,R> List<R> mapList(List<T> l, Function<T,R> f) {
        return l.stream().map(f).collect(Collectors.toList());
    }
}
