package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.CalculatingRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.PriceActionFunction.generate;

public enum PriceActionFunctions {

    SMA(period -> generate((pa1,pa2) -> pa1.add(pa2),pa -> pa.divide(period)));
    // LOBF(period -> generateList(pa -> lobf(pa.stream().map(PriceAction::getClose).collect(Collectors.toList()))));

    private final Function<Integer, PriceActionFunction<PriceAction, PriceAction, PriceAction>> f;

    PriceActionFunctions(Function<Integer, PriceActionFunction<PriceAction,PriceAction,PriceAction>> f) {
        this.f = f;
    }

    public static Optional<CalculatingRingBuffer<PriceAction,PriceAction,PriceAction>> match(Map<Integer, List<RingBuffer<PriceAction>>> buffers, String id) {
        return Stream.of(values())
                .filter(v -> v.matcher(id).matches())
                .flatMap(v -> {
                    Matcher m = v.matcher(id);
                    m.find();
                    int period = Integer.parseInt(m.group(1));
                    List<RingBuffer<PriceAction>> ringBuffers = buffers.compute(period, (_k, _v) -> Optional.ofNullable(_v).map(__v -> {
                        __v.add(new ArrayRingBuffer<>(new PriceAction[_k]));
                        return __v;
                    }).orElseGet(() -> {
                        List<RingBuffer<PriceAction>> tmp = new ArrayList<>();
                        tmp.add(new ArrayRingBuffer<>(new PriceAction[_k]));
                        return tmp;
                    }));
                    return last(ringBuffers).map(rb -> CalculatingRingBuffer.create(rb, v.name(), v.get(period))).stream();
                })
                .findFirst();
    }

    private PriceActionFunction<PriceAction,PriceAction,PriceAction> get(int n) {
        return f.apply(n);
    }

    private Matcher matcher(String id) {
        return Pattern.compile(String.format("%s\\((\\d+)\\)",name().toLowerCase())).matcher(id);
    }
}
