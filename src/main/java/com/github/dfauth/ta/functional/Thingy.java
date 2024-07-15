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

import static com.github.dfauth.ta.functional.Thingy.ThingyState.fromSMA;
import static com.github.dfauth.ta.model.PriceAction.SUM;
import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThan;
import static com.github.dfauth.ta.util.BigDecimalOps.pctChange;

@Slf4j
@Data
public class Thingy {

    @JsonIgnore
    private final Nested previous;
    @JsonIgnore
    private final Nested current;
    @JsonProperty("d")
    private final int daysInThisState;
    @JsonIgnore
    private Optional<ThingyState> previousState;

    public Thingy(PriceAction pa, PriceAction l, PriceAction s, PriceAction f) {
        this(null, new Nested(pa, l, s, f), 0, Optional.empty());
    }

    private Thingy(Nested previous, Nested current, int daysInThisState, Optional<ThingyState> previousState) {
        this.previous = previous;
        this.current = current;
        this.previousState = previousState;
        this.daysInThisState = previous == null ? 0 :
                current.getState() == previous.getState() ? daysInThisState +1 : resetPreviousState();
    }

    private int resetPreviousState() {
        this.previousState = Optional.of(previous.getState());
        return 0;
    }

    public static Optional<Thingy> calculateThingy(List<PriceAction> priceAction) {
        return calculateThingy(priceAction, 20, 50, 200);
    }

    public static Optional<Thingy> calculateThingy(List<PriceAction> priceAction, int fastPeriod, int slowPeriod, int longPeriod) {
        RingBuffer<PriceAction> longBuffer = new ArrayRingBuffer<>(new PriceAction[longPeriod]);
        RingBuffer<PriceAction> slowBuffer = new ArrayRingBuffer<>(new PriceAction[slowPeriod]);
        RingBuffer<PriceAction> fastBuffer = new ArrayRingBuffer<>(new PriceAction[fastPeriod]);

        Optional<Thingy> thingy = priceAction.stream().map(pa -> {
            longBuffer.write(pa);
            slowBuffer.write(pa);
            fastBuffer.write(pa);

            return SUM.apply(fastBuffer.streamIfFull())
                    .map(_pa -> _pa.divide(fastPeriod))
                    .flatMap(f -> SUM.apply(slowBuffer.streamIfFull())
                            .map(_pa -> _pa.divide(slowPeriod))
                            .flatMap(s -> SUM.apply(longBuffer.streamIfFull())
                                    .map(_pa -> _pa.divide(longPeriod)).map(l -> new Thingy(pa, l, s, f))
                            )
                    );
        }).flatMap(Optional::stream).reduce(Thingy::next);
        return thingy;
    }

    public Thingy next(Thingy next) {
        return new Thingy(current, next.current, daysInThisState, previousState);
    }

    @JsonProperty("s")
    public ThingyState getState() {
        return current.getState();
    }

    @JsonProperty("o")
    public int getOrd() {
        return getState().ordinal();
    }

    public Optional<BigDecimal> getSdfma() {
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

    public enum ThingyState {
        BULLISH, // l < s < f
        BULLISH_FADEOUT, // l < f < s
        ADVANCED_BULLISH_FADEOUT, // f < l < s
        BEARISH, // f < s < l
        BEARISH_RECOVERY, // s < f < l
        ADVANCED_BEARISH_RECOVERY, // s < l < f
        ;

        public static ThingyState fromSMA(BigDecimal l, BigDecimal s, BigDecimal f) {
            return isGreaterThan(s,l) ? bullish(l, s, f) : bearish(l,s,f);
        }

        private static ThingyState bearish(BigDecimal l, BigDecimal s, BigDecimal f) {
            return isGreaterThan(s,f) ? BEARISH : isGreaterThan(f, l) ? ADVANCED_BEARISH_RECOVERY : BEARISH_RECOVERY;
        }

        private static ThingyState bullish(BigDecimal l, BigDecimal s, BigDecimal f) {
            return isGreaterThan(f,s) ? BULLISH : isGreaterThan(l, s) ? ADVANCED_BULLISH_FADEOUT : BULLISH_FADEOUT;
        }

        public PriceAction getRelevantMA(Thingy.Nested thingy) {
            return thingy.fastPriceAction;
        }
    }

    @Data
    static class Nested {
        private final PriceAction priceAction;
        private final PriceAction longPriceAction;
        private final PriceAction slowPriceAction;
        private final PriceAction fastPriceAction;

        public ThingyState getState() {
            return fromSMA(longPriceAction.getClose(),
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
            return getDistance(priceAction, fastPriceAction);
        }

        public static Optional<BigDecimal> getDistance(PriceAction pa1, PriceAction pa2) {
            return pctChange(pa1.getClose(), pa2.getClose());
        }
    }
}
