package com.github.dfauth.ta.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamOps {

    public static <T,R> Function<Stream<T>,Stream<R>> flatten(Function<T,Stream<R>> f) {
        return streamOfT -> streamOfT.flatMap(f);
    }

    public static <K,V> Map<K,V> flatten(Map<K,Optional<V>> map) {
        return flattenStream(map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <K,V> Stream<Map.Entry<K, V>> flattenStream(Stream<Map.Entry<K,Optional<V>>> stream) {
        return stream.flatMap(e -> flattenEntry(e).stream());
    }

    public static <K,V> Optional<Map.Entry<K, V>> flattenEntry(Map.Entry<K,Optional<V>> entry) {
        return entry.getValue().map(v -> Map.entry(entry.getKey(), v));
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return stream(iterable.iterator());
    }

    public static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0),false);
    }
}
