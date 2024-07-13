package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

import static com.github.dfauth.ta.functional.RingBufferCollector.ringBufferCollector;
import static com.github.dfauth.ta.model.PriceAction.sma;
import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThan;

@Slf4j
@AllArgsConstructor
@Data
public class Thingy {

    private final PriceAction[] priceAction;
    private final PriceAction[] longPriceAction;
    private final PriceAction[] slowPriceAction;
    private final PriceAction[] fastPriceAction;

    public static Thingy calculateThingy(List<PriceAction> priceAction) {
        return calculateThingy(priceAction, 20, 50, 200);
    }

    public static Thingy calculateThingy(List<PriceAction> priceAction, int fastPeriod, int slowPeriod, int longPeriod) {
        int n = 3;
        List<PriceAction> prices = priceAction.subList(priceAction.size()-n-1, priceAction.size()-1);
        List<PriceAction> fastBuffer = priceAction.stream()
                .collect(ringBufferCollector(new PriceAction[fastPeriod+n], sma(n)));
        List<PriceAction> slowBuffer = priceAction.stream()
                .collect(ringBufferCollector(new PriceAction[slowPeriod+n], sma(n)));
        List<PriceAction> longBuffer = priceAction.stream()
                .collect(ringBufferCollector(new PriceAction[longPeriod+n], sma(n)));
        return new Thingy(prices.toArray(new PriceAction[n]),
                longBuffer.toArray(new PriceAction[n]),
                slowBuffer.toArray(new PriceAction[n]),
                fastBuffer.toArray(new PriceAction[n]));
    }

    public ThingyState getState() {
        return getState(priceAction.length);
    }

    public ThingyState getState(int i) {
        return ThingyState.fromSMA(longPriceAction[i].getClose(), slowPriceAction[i].getClose(), fastPriceAction[i].getClose());
    }

    enum ThingyState {
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
    }
}
