package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.CalculatingRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.PriceActionFunction.generate;

public enum IdentityPriceActionFunctions implements WithMatcher<PriceActionFunction<PriceAction,PriceAction>> {

    SMA(period -> generate(
            (pa1,pa2) ->
                    pa1.add(pa2),
            pa ->
                    pa.divide(period))
    ),
    ROC(period -> generate(
            (pa1,pa2) ->
                    pa2.subtract(pa1),
            pa ->
                    pa.divide(period))
    );
    // LOBF(period -> generateList(pa -> lobf(pa.stream().map(PriceAction::getClose).collect(Collectors.toList()))));

    private final Function<Integer, PriceActionFunction<PriceAction, PriceAction>> f;

    IdentityPriceActionFunctions(Function<Integer, PriceActionFunction<PriceAction,PriceAction>> f) {
        this.f = f;
    }

    public static Optional<CalculatingRingBuffer<PriceAction,PriceAction,PriceAction>> match(Map<Integer, RingBuffer<PriceAction>> buffers, String id) {
        return match(Arrays.stream(values()), buffers, id);
    }

    public static <T,R> Optional<CalculatingRingBuffer<PriceAction,T,R>> match(Stream<WithMatcher<PriceActionFunction<T,R>>> withMatcherStream, Map<Integer, RingBuffer<PriceAction>> buffers, String id) {
        return withMatcherStream
                .filter(v -> v.matcher(id).matches())
                .map(v -> {
                    NamePeriodMatcher matcher = v.matcher(id);
                    RingBuffer<PriceAction> ringBuffer = buffers.computeIfAbsent(matcher.period(), _k -> new ArrayRingBuffer<>(new PriceAction[_k]));
                    return CalculatingRingBuffer.create(ringBuffer, v.get(matcher.period()));
                })
                .findFirst();
    }

    public PriceActionFunction<PriceAction,PriceAction> get(int n) {
        return f.apply(n);
    }
}
