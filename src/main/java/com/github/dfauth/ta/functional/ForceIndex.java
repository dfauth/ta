package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.CalculatingRingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.ForceIndex.ForceIndexCalculator.curriedForceIndex;
import static com.github.dfauth.ta.functional.Function2.rightCurry;
import static com.github.dfauth.ta.util.BigDecimalOps.multiply;

@Slf4j
public class ForceIndex {

    public List<BigDecimal> calculateForceIndex(List<PriceAction> priceAction, int period) {

        List<Optional<Function<PriceAction, BigDecimal>>> result = priceAction
                .stream()
                .map(curriedForceIndex)
                .reduce(
                        List.of(),
                        (l, f) -> Lists.add(l,Optional.of(f)),
                        Lists::add
                        );
        // Lists.zip(priceAction, result, (pa,f) -> f.map(_f -> _f.apply(pa)));

        BiFunction<ForceIndexCalculator,ForceIndexCalculator,ForceIndexCalculator> accumulator = null;
        Function<ForceIndexCalculator,BigDecimal> finisher = null;
        SimpleCollector<ForceIndexCalculator, ForceIndexCalculator, BigDecimal> f = null; //SimpleCollector.reduce(ZERO, accumulator, finisher);
        CalculatingRingBuffer<ForceIndexCalculator,ForceIndexCalculator,BigDecimal> buffer = new CalculatingRingBuffer<>(new ArrayRingBuffer<>(new ForceIndexCalculator[period]), f);
        return null; //priceAction.stream().map(ForceIndexCalculator::curriedForceIndex).map(peek(buffer::write)).map(supply(buffer::calculate)).flatMap(Optional::stream).collect(Collectors.toList());
    }

    interface ForceIndexCalculator {

        BiFunction<PriceAction,PriceAction,BigDecimal> forceIndex = (previous, current) -> multiply(current.getClose().subtract(previous.getClose()),current.getVolume());
        Function<PriceAction,Function<PriceAction,BigDecimal>> curriedForceIndex = rightCurry(forceIndex);
    }

}
