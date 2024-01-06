package com.github.dfauth.ta.functional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class HistoricalOffset<T> {

    private final int offset;
    private final T payload;

    public enum Direction implements Function<Integer, UnaryOperator<Integer>> {
        FORWARD(l -> i -> i),
        BACKWARD(l -> i -> l-i-1);

        private final Function<Integer, UnaryOperator<Integer>> f;

        Direction(Function<Integer, UnaryOperator<Integer>> f) {
            this.f = f;
        }

        @Override
        public UnaryOperator<Integer> apply(Integer size) {
            return f.apply(size);
        }
    }

    public static <T> Stream<HistoricalOffset<T>> zipWithHistoricalOffset(List<T> listOfT) {
        return zipWithHistoricalOffset(listOfT, Direction.BACKWARD);
    }

    public static <T> Stream<HistoricalOffset<T>> zipWithHistoricalOffset(List<T> listOfT, Direction direction) {
        Iterator<T> it = listOfT.iterator();
        UnaryOperator<Integer> f = direction.apply(listOfT.size());
        return IntStream.range(0, listOfT.size()).mapToObj(i -> new HistoricalOffset<>(f.apply(i), it.next()));
    }

    public static <T> Stream<ZipWithPrevious<T>> zipWithPrevious(List<T> listOfT) {
        Iterator<T> it = listOfT.iterator();
        if(it.hasNext()) {
            // take one and discard
            it.next();

            Iterator<T> it2 = listOfT.iterator();
            return IntStream.range(0, listOfT.size()-1).mapToObj(i -> new ZipWithPrevious<>(it.next(), it2.next()));
        } else {
            return Stream.empty();
        }
    }

    public int duration(HistoricalOffset<T> historicalOffset) {
        return getOffset() - historicalOffset.getOffset();
    }

    public Map.Entry<Integer, T> toMapEntry() {
        return Map.entry(getOffset(), getPayload());
    }

    public static class ZipWithPrevious<T> extends Tuple2<T,T> {

        public ZipWithPrevious(T t, T t2) {
            super(t, t2);
        }

        public T getCurrent() {
            return _1();
        }

        public T getPrevious() {
            return _2();
        }
    }
}
