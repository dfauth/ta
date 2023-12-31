package com.github.dfauth.ta.functional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        return splitAt(ts, _ignore -> position);
    }

    public static <T> Tuple2<List<T>, List<T>> splitAt(List<T> ts, UnaryOperator<Integer> splitter) {
        int position = splitter.apply(ts.size());
        return Tuple2.tuple2(ts.subList(0,position),ts.subList(position,ts.size()));
    }

    public <R> Lists<R> map(Function<T,R> f) {
        return new Lists<>(stream().map(f).collect(Collectors.toList()));
    }

    public <R,S> R reduce(Reducer<T, S, R> reducer) {
        return stream().collect(reducer);
    }
}
