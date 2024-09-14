package com.github.dfauth.ta.functional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThan;
import static com.github.dfauth.ta.util.BigDecimalOps.pctChange;

@Slf4j
@Data
public class Trend {

    @JsonIgnore
    private final Nested previous;
    @JsonIgnore
    private final Nested current;
    @JsonProperty("d")
    private final int daysInThisState;
    @JsonIgnore
    private Optional<TrendState> previousState;

    public Trend(PriceAction pa, PriceAction l, PriceAction s, PriceAction f) {
        this(null, new Nested(pa, l, s, f), 0, Optional.empty());
    }

    private Trend(Nested previous, Nested current, int daysInThisState, Optional<TrendState> previousState) {
        this.previous = previous;
        this.current = current;
        this.previousState = previousState;
        this.daysInThisState = previous == null ? 0 :
                current.getState() == previous.getState() ? daysInThisState +1 : resetPreviousState();
    }

    public <T> T map(BiFunction<Nested,Nested,T> f2) {
        return f2.apply(current,previous);
    }

    private int resetPreviousState() {
        this.previousState = Optional.of(previous.getState());
        return 0;
    }

    public static Optional<Trend> calculateTrend(List<PriceAction> priceAction) {
        return calculateTrend(priceAction, 20, 50, 200);
    }

    public static Optional<Trend> calculateTrend(List<PriceAction> priceAction, int fastPeriod, int slowPeriod, int longPeriod) {
        return calculateTrend(priceAction, fastPeriod+1, slowPeriod+1, longPeriod+1, PriceAction.EMA);
    }

    public static Optional<Trend> calculateTrend(List<PriceAction> priceAction, int fastPeriod, int slowPeriod, int longPeriod, Function<List<PriceAction>, Optional<PriceAction>> smoothingFunction) {
        RingBuffer<PriceAction> longBuffer = new ArrayRingBuffer<>(new PriceAction[longPeriod]);
        RingBuffer<PriceAction> slowBuffer = new ArrayRingBuffer<>(new PriceAction[slowPeriod]);
        RingBuffer<PriceAction> fastBuffer = new ArrayRingBuffer<>(new PriceAction[fastPeriod]);

        Optional<Trend> trend = priceAction.stream().map(pa -> {
            longBuffer.write(pa);
            slowBuffer.write(pa);
            fastBuffer.write(pa);

            return smoothingFunction.apply(fastBuffer.streamIfFull().collect(Collectors.toList()))
                    .flatMap(f -> smoothingFunction.apply(slowBuffer.streamIfFull().collect(Collectors.toList()))
                                    .flatMap(s -> smoothingFunction.apply(longBuffer.streamIfFull().collect(Collectors.toList()))
                                    .map(l -> new Trend(pa,l,s,f))));
        }).flatMap(Optional::stream).reduce(Trend::next);
        return trend;
    }

    public Trend next(Trend next) {
        return new Trend(current, next.current, daysInThisState, previousState);
    }

    @JsonProperty("s")
    public TrendState getState() {
        return current.getState();
    }

    @JsonProperty("o")
    public int getOrd() {
        return getState().ordinal();
    }

    public Optional<BigDecimal> getFdsma() {
        return current.getFastDistanceFromSlowMA();
    }

    @JsonProperty("ta")
    public Optional<BigDecimal> getTrendAcceleration() {
        return Nested.getDistance(current.fastPriceAction, current.slowPriceAction)
                .flatMap(d -> Nested.getDistance(previous.fastPriceAction, previous.slowPriceAction)
                        .flatMap(d2 -> pctChange(d,d2)));
    }

    public Optional<BigDecimal> getPdfma() {
        return current.getPriceDistanceFromFastMA();
    }

    public Optional<BigDecimal> getPdsma() {
        return current.getPriceDistanceFromSlowMA();
    }

    public Optional<BigDecimal> getPdlma() {
        return current.getPriceDistanceFromLongMA();
    }

//    public Optional<BigDecimal> getFvdlma() {
//        return current.getFastVolumeDistanceFromLongMA();
//    }
//
//    public Optional<BigDecimal> getSvdlma() {
//        return current.getSlowVolumeDistanceFromLongMA();
//    }

    public enum TrendState {
        LATE_BULL, // l < f < s
        EARLY_BEAR, // f < l < s
        BEAR, // f < s < l
        LATE_BEAR, // s < f < l
        EARLY_BULL, // s < l < f
        BULL // l < s < f
        ;

        public static TrendState getState(BigDecimal l, BigDecimal s, BigDecimal f) {
            return isGreaterThan(s,l) ? bullish(l, s, f) : bearish(l,s,f);
        }

        private static TrendState bearish(BigDecimal l, BigDecimal s, BigDecimal f) {
            return isGreaterThan(s,f) ? BEAR : isGreaterThan(f, l) ? EARLY_BULL : LATE_BEAR;
        }

        private static TrendState bullish(BigDecimal l, BigDecimal s, BigDecimal f) {
            return isGreaterThan(f,s) ? BULL : isGreaterThan(l, s) ? EARLY_BEAR : LATE_BULL;
        }

        public PriceAction getRelevantMA(Trend.Nested nested) {
            return nested.fastPriceAction;
        }
    }

    @Data
    public static class Nested {
        private final PriceAction priceAction;
        private final PriceAction longPriceAction;
        private final PriceAction slowPriceAction;
        private final PriceAction fastPriceAction;

        public TrendState getState() {
            return TrendState.getState(longPriceAction.getClose(),
                    slowPriceAction.getClose(),
                    fastPriceAction.getClose()
            );
        }

        public Optional<BigDecimal> getFastDistanceFromSlowMA() {
            return getDistance(fastPriceAction, slowPriceAction);
        }

        public Optional<BigDecimal> getDistanceFromMA(PriceAction pa) {
            return getDistance(pa, getState().getRelevantMA(this));
        }

        public Optional<BigDecimal> getPriceDistanceFromFastMA() {
            return getDistance(priceAction, fastPriceAction);
        }

        public Optional<BigDecimal> getPriceDistanceFromSlowMA() {
            return getDistance(priceAction, slowPriceAction);
        }

        public Optional<BigDecimal> getPriceDistanceFromLongMA() {
            return getDistance(priceAction, longPriceAction);
        }

        public Optional<BigDecimal> getFastVolumeDistanceFromLongMA() {
            return getDistance(fastPriceAction, longPriceAction, pa -> BigDecimal.valueOf(pa.getVolume()));
        }

        public Optional<BigDecimal> getSlowVolumeDistanceFromLongMA() {
            return getDistance(slowPriceAction, longPriceAction, pa -> BigDecimal.valueOf(pa.getVolume()));
        }

        public static Optional<BigDecimal> getDistance(PriceAction pa1, PriceAction pa2) {
            return getDistance(pa1, pa2, PriceAction::getClose);
        }

        public static Optional<BigDecimal> getDistance(PriceAction pa1, PriceAction pa2, Function<PriceAction,BigDecimal> fn) {
            return pctChange(fn.apply(pa1), fn.apply(pa2));
        }
    }
}
