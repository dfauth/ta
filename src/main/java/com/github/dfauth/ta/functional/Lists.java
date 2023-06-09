package com.github.dfauth.ta.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lists<T> extends ArrayList<T> {

    public Lists(List<T> list) {
        super(list);
    }

    public static <T,R> List<R> mapList(List<T> l, Function<T,R> f) {
        return new Lists<>(l).map(f);
    }

    public static <T> Lists<T> of(T... ts) {
        return new Lists<>(Stream.of(ts).collect(Collectors.toList()));
    }

    public <R> Lists<R> map(Function<T,R> f) {
        return new Lists<>(stream().map(f).collect(Collectors.toList()));
    }

    public <R,S> R reduce(Reducer<T, S, R> reducer) {
        return stream().collect(reducer);
    }
}
