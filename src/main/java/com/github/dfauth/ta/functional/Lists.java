package com.github.dfauth.ta.functional;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static java.util.function.Predicate.not;

public class Lists<T> extends ArrayList<T> {

    public Lists(List<T> list) {
        super(list);
    }

    public static <T,R> Function<List<T>,List<R>> mapList(Function<T,R> f) {
        return l -> new Lists<>(l).map(f);
    }

    public static <T,R> List<R> mapList(List<T> l, Function<T,R> f) {
        return mapList(f).apply(l);
    }

    public static <T> Lists<T> of(T... ts) {
        return new Lists<>(Stream.of(ts).collect(Collectors.toList()));
    }

    public static <T> Optional<T> last(List<T> l) {
        return Optional.of(l).filter(not(List::isEmpty)).map(_l -> _l.get(l.size()-1));
    }

    public static <T> List<T> last(List<T> l, int n) {
        return Optional.of(l).filter(_l -> _l.size() >= n).map(_l -> _l.subList(l.size()-n,l.size())).orElse(List.of());
    }

    public static <T> Optional<T> head(List<T> l) {
        return headAndTail(l).map(Tuple2::_1);
    }

    public static <T> List<T> tail(List<T> l) {
        return headAndTail(l).map(Tuple2::_2).orElse(Collections.emptyList());
    }

    public static <T> Optional<Tuple2<T,List<T>>> headAndTail(List<T> l) {
        return Optional.of(l)
                .filter(not(List::isEmpty))
                .map(_l -> tuple2(
                        _l.get(0),
                        Optional.of(l)
                                .filter(__l -> __l.size() > 1)
                                .map(__l -> __l.subList(1,_l.size()-1)).orElse(Collections.emptyList())));
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

    public static <T,R,U> Stream<U> zip(Iterable<T> tIterable, Iterable<R> rIterable, BiFunction<T,R,U> f2) {
        Iterator<R> itr = rIterable.iterator();
        return StreamSupport.stream(tIterable.spliterator(), false).filter(t -> itr.hasNext()).map(t -> f2.apply(t,itr.next()));
    }

    public static <T> Tuple2<List<T>, List<T>> splitAt(List<T> ts, int position) {
        int p = position < 0 ? ts.size() + position : position;
        return splitAt(ts, _ignore -> p);
    }

    public static <T> Tuple2<List<T>, List<T>> splitAt(List<T> ts, UnaryOperator<Integer> splitter) {
        int position = splitter.apply(ts.size());
        return Optional.ofNullable(ts)
                .filter(_ts -> _ts.size() > position)
                .map(_ts -> tuple2(_ts.subList(0,position),ts.subList(position,ts.size())))
                .orElse(tuple2(ts, Collections.emptyList()));
    }

    public static <T> List<T> collect(Stream<T> streamOfT) {
        return streamOfT.collect(Collectors.toList());
    }

    public static <T> Optional<List<T>> nonEmpty(List<T> l) {
        return Optional.ofNullable(l).filter(not(List::isEmpty));
    }

    public <R> Lists<R> map(Function<T,R> f) {
        return new Lists<>(stream().map(f).collect(Collectors.toList()));
    }

    public <R,S> R reduce(Reducer<T, S, R> reducer) {
        return stream().collect(reducer);
    }

    public static <V,T,R> Map<T,R> toMap(List<V> l, Function<V,T> keyMapper, Function<V,R> valueMapper) {
        return l.stream().collect(Collectors.toMap(keyMapper,valueMapper));
    }


}
