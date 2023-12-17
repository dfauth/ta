package com.github.dfauth.ta.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

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

    public static <T> Optional<T> last(List<T> l) {
        return Optional.of(l).filter(not(List::isEmpty)).map(_l -> _l.get(l.size()-1));
    }

    public static <T> List<T> add(List<T> l, T t) {
        List<T> tmp = new ArrayList<>(l);
        tmp.add(t);
        return tmp;
    }

    public static <T> List<T> add(List<T> l1, List<T> l2) {
        List<T> tmp = new ArrayList<>(l1);
        tmp.addAll(l2);
        return tmp;
    }

    public <R> Lists<R> map(Function<T,R> f) {
        return new Lists<>(stream().map(f).collect(Collectors.toList()));
    }

    public <R,S> R reduce(Reducer<T, S, R> reducer) {
        return stream().collect(reducer);
    }
}
