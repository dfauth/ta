package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.BigDecimalOps;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.SMA;
import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functional.Lists.head;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.util.BigDecimalOps.divide;

@Slf4j
@Data
@AllArgsConstructor
public class Momentum {

    public static Optional<PriceAction> processPrices(int period, List<Price> prices, Function<List<PriceAction>, Optional<PriceAction>> f) {
        RingBuffer<PriceAction> ringBuffer = new ArrayRingBuffer<>(new PriceAction[period]);
        List<PriceAction> emas = prices.stream().map(pa -> {
            ringBuffer.write(pa);
            return f.apply(ringBuffer.streamIfFull().collect(Collectors.toList()));
        }).flatMap(Optional::stream).collect(Collectors.toList());
        return last(emas)
                .flatMap(_l -> head(emas)
                        .map(_h -> _l.subtract(_h)
                                .divide(period)));
    }

    public static Optional<Momentum> processPricesMomentum(int period, List<Price> prices, Function<List<PriceAction>, Optional<PriceAction>> f) {
        RingBuffer<PriceAction> ringBuffer = new ArrayRingBuffer<>(new PriceAction[period]);
        List<PriceAction> emas = prices.stream().map(pa -> {
            ringBuffer.write(pa);
            return f.apply(ringBuffer.streamIfFull().collect(Collectors.toList()));
        }).flatMap(Optional::stream).collect(Collectors.toList());
        return AVG_TRUE_RANGE.apply(period, emas).map(atr -> new Momentum(emas, atr));
    }

    public static final BiFunction<PriceAction, PriceAction, Optional<BigDecimal>> TRUE_RANGE = (current, previous) -> {
        // greatest of the following:
        // current high less the current low;
        // the absolute value of the current high less the previous close;
        // and the absolute value of the current low less the previous close
        return BigDecimalOps.maxOf(current.getRange(),
                current.getHigh().subtract(previous.getClose()).abs(),
                current.getLow().subtract(previous.getClose()).abs()
        );
    };

    public static final BiFunction<Integer, List<PriceAction>, Optional<BigDecimal>> AVG_TRUE_RANGE = (period, pa) -> {
        RingBuffer<BigDecimal> trRingBuffer = new ArrayRingBuffer<>(new BigDecimal[period]);
        RingBuffer<BigDecimal> atrRingBuffer = new ArrayRingBuffer<>(new BigDecimal[period]);
        pa.stream().reduce((p, c) -> {
            TRUE_RANGE.apply(p, c).ifPresent(trRingBuffer::write);
            SMA.apply(trRingBuffer.streamIfFull().collect(Collectors.toList())).ifPresent(atrRingBuffer::write);
            return c;
        });
        return atrRingBuffer.streamIfFull().reduce((pAtr, cAtr) -> divide(pAtr.multiply(BigDecimal.valueOf(period - 1)).add(cAtr),period));
    };

    @JsonIgnore
    private final List<PriceAction> prices;
    private final BigDecimal atr;

    @JsonProperty("m")
    public BigDecimal getMomentum() {
        return prices.stream().map(PriceAction::getClose).reduce(new Momentum.Accumulator(), Accumulator::apply, oops()).avg();
    }

    @JsonProperty("v")
    public Optional<BigDecimal> getVolume() {
        return last(prices).map(PriceAction::getVolume).map(BigDecimalOps::valueOf);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    static class Accumulator {

        private BigDecimal first;
        private BigDecimal last;
        private int n;

        Accumulator apply(BigDecimal value) {
            return Optional.ofNullable(first).map(f -> new Accumulator(f, value,n+1)).orElse(new Accumulator(value, null,0));
        }

        public BigDecimal avg() {
            return divide(last.subtract(first), n);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    static class ATRWithPriceAction {

        private PriceAction current;
        private BigDecimal atr;

        public ATRWithPriceAction(PriceAction pa) {
            this.current = pa;
        }

        Optional<ATRWithPriceAction> next(PriceAction next) {
            return TRUE_RANGE.apply(current, next).map(atr -> new ATRWithPriceAction(next, atr));
        }
    }
}
