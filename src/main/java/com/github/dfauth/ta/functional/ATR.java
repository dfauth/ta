package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.RingBufferCollector.ringBufferCollector;
import static com.github.dfauth.ta.functional.Collectors.*;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.util.BigDecimalOps.divide;
import static com.github.dfauth.ta.util.BigDecimalOps.multiply;

public class ATR {

    public static Optional<BigDecimal> trueRange(List<Price> prices, int period) {
        if(period > prices.size()) {
            throw new IllegalArgumentException("period of "+period+" is less the sample size of "+prices.size());
        }
        return prices.stream().collect(consecutive(PriceAction.trueRange)).stream().collect(ringBufferCollector(new BigDecimal[period], SMA));
    }

    public static Optional<BigDecimal> avgTrueRange(List<Price> prices, int period) {
        Tuple2<List<Price>,List<Price>> t2 = Lists.splitAt(prices,prices.size() - period);
        Optional<BigDecimal> otr = trueRange(t2._1(), period);
        return otr.map(tr -> t2._2().stream().collect(consecutive(PriceAction.trueRange))
                .stream()
                .reduce(tr,
                    (atr,_tr) -> divide(multiply(atr, period-1).add(_tr),period),
                    oops()
                ));
    }

    public static Optional<AverageTrueRange> atr(List<Price> prices, int period) {
        Optional<BigDecimal> atr = avgTrueRange(prices, period);
        return last(prices).map(Price::getClose).flatMap(close -> atr.map(_atr -> new AverageTrueRange(_atr,period,close)));
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class AverageTrueRange {
        private BigDecimal atr;
        private Integer period;
        private BigDecimal last;

        public BigDecimal pctAvgTrueRange() {
            return atr.divide(last, RoundingMode.HALF_UP);
        }
    }

}
