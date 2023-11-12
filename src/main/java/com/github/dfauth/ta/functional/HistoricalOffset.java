package com.github.dfauth.ta.functional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class HistoricalOffset<T> {

    private final int offset;
    private final T payload;

    public static <T> Stream<HistoricalOffset<T>> zipWithHistoricalOffset(List<T> listOfT) {
        Iterator<T> it = listOfT.iterator();
        return IntStream.range(0, listOfT.size()).mapToObj(i -> new HistoricalOffset<>(listOfT.size()-i, it.next()));
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
