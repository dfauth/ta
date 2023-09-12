package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.Price;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.StatefulFunction.asFunction;
import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static com.github.dfauth.ta.functional.Tuple3.tuple3;
import static java.util.function.Function.identity;

public class ATR {

    public static Optional<AverageTrueRange> trueRange(List<Price> prices, int period) {
        return prices.stream()
                .map(asFunction(trueRange(period)))
                .flatMap(Optional::stream)
                .collect(Reducer.last());
    }

    private static StatefulFunction<Price, AverageTrueRange, Tuple3<Price,Integer,BigDecimal>> trueRange(int period) {
        BigDecimal bdPeriod = BigDecimal.valueOf(period);
        return (p,t3) -> {
            // if no previous value
            if(t3 == null) {
                return tuple2(
                        Optional.empty(),
                        tuple3(p, 1, p.getRange())
                );
            } else if(period > t3._2()) {
                BigDecimal previousClose = t3._1().get_close();
                BigDecimal tr = p.getTrueRange(previousClose);
                int n = t3._2();
                return tuple2(Optional.empty(), tuple3(p, n+1, t3._3().add(tr)));
            } else if(period == t3._2()) {
                BigDecimal previousClose = t3._1().get_close();
                BigDecimal tr = p.getTrueRange(previousClose);
                int n = t3._2();
                BigDecimal atr = t3._3().add(tr).divide(bdPeriod, RoundingMode.HALF_UP);
                return tuple2(Optional.empty(), tuple3(p, n+1, atr));
            } else {
                BigDecimal previousClose = t3._1().get_close();
                BigDecimal tr = p.getTrueRange(previousClose);
                int n = t3._2();
                BigDecimal previousAtr = t3._3();
                BigDecimal atr = previousAtr.multiply(BigDecimal.valueOf(period-1)).add(tr).divide(bdPeriod, RoundingMode.HALF_UP);
                return tuple2(Optional.of(new AverageTrueRange(atr, period, previousClose)).filter(_atr -> n >= period), tuple3(p, n+1, atr));
            }
        };
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
