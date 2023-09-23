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
}
