package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.functional.WindowingReducer;
import com.github.dfauth.ta.model.PV;
import com.github.dfauth.ta.model.Price;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functions.MovingAverages.sma;
import static com.github.dfauth.ta.functions.RateOfChange.roc;
import static com.github.dfauth.ta.functions.TestData.EMR;
import static com.github.dfauth.ta.util.Accumulator.pvAveragingAccumulator;
import static com.github.dfauth.ta.util.BigDecimalOps.pctChange;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SMATest {

    private static final int[] TEST_DATA = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    private static final double[] adt_prices = new double[]{3.80,3.81,3.77, 3.72, 3.68,3.60,3.51,3.71};

    @Test
    public void testIt() {
        assertEquals(
                List.of(3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 10.5, 11.5, 12.5, 13.5, 14.5),
                IntStream.of(TEST_DATA).boxed()
                        .map(sma(4, Accumulator.INT_ACCUMULATOR.get()))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testASX_ADT_sma() {
        assertEquals(
                List.of(3.716, 3.656, 3.644).stream().map(BigDecimal::valueOf).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(sma(5))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testASX_ADT_roc() {
        assertEquals(
                List.of(0.003, -0.010, -0.013, -0.011, -0.022, -0.025, 0.057).stream()
                        .map(BigDecimal::valueOf)
                        .map(bd -> bd.setScale(3))
                        .collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(roc())
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testASX_ADT_roc_sma() {
        assertEquals(
                List.of(-0.016, -0.003).stream().map(BigDecimal::valueOf).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(roc())
                        .flatMap(Optional::stream)
                        .map(sma(5))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testPVAccumulator() {
        int period = 21;
        WindowingReducer<Price, com.github.dfauth.ta.util.Accumulator<Price, PV>, PV> reducer = new WindowingReducer<>(new Price[period], Reducers.sma(pvAveragingAccumulator()));
        List<PV> pv = EMR.stream().collect(reducer);
        assertEquals(EMR.size() - period + 1, pv.size());
        List<SMAComparison> smaComparisons = reverseZip(EMR, pv, SMAComparison::new);
        assertEquals(EMR.size() - period + 1, smaComparisons.size());
        SMAComparison l = last(smaComparisons).orElseThrow();
        log.info("comparator: {}",renderJson(l));
    }

    public static <T> String renderJson(T t) {
        return tryCatch(() -> new ObjectMapper().writeValueAsString(t));
    }

    public <T,R,S> List<S> reverseZip(List<T> tList, List<R> rList, BiFunction<T,R,S> f2) {
        int min = Integer.min(tList.size(), rList.size());
        List<T> tSubList = tList.size() > min ? tList.subList(tList.size()-min, tList.size()) : tList;
        List<R> rSubList = rList.size() > min ? rList.subList(rList.size()-min, rList.size()) : rList;
        Iterator<T> tIt = tSubList.iterator();
        Iterator<R> rIt = rSubList.iterator();
        return IntStream.range(0, min).mapToObj(ignored -> f2.apply(tIt.next(), rIt.next())).collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class SMAComparison {
        @JsonIgnore private Price price;
        @JsonIgnore private PV sma;

        public BigDecimal getPriceVariation() {
            return pctChange(price.getClose(),sma.getClosingPrice()).get();
        }

        public BigDecimal getVolumeVariation() {
            return pctChange(price.getVolume(),sma.getVolume()).get();
        }
    }
}
