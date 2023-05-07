package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

public class RSI {

    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100.000);

    public static Function<BigDecimal, Optional<BigDecimal>> rsi(int period) {

        if (period <= 0) {
            throw new IllegalArgumentException("Period should be a positive integer");
        }

        BigDecimal p = BigDecimal.valueOf(period);
        BigDecimal pMinus1 = BigDecimal.valueOf(period-1);

        var ref = new Object() {
            BigDecimal avgGain = ZERO.setScale(3);
            BigDecimal avgLoss = ZERO.setScale(3);
            BigDecimal prev = null;
            int cnt = 0;
        };

        return t -> Optional.ofNullable(ref.prev).map(_p -> {
            BigDecimal change = t.subtract(_p);

            int i = ref.cnt++;
            if(i < period) {
                ref.avgGain = ref.avgGain.multiply(BigDecimal.valueOf(i).setScale(3)).add(change.max(ZERO)).divide(BigDecimal.valueOf(i+1), HALF_UP);
                ref.avgLoss = ref.avgLoss.multiply(BigDecimal.valueOf(i).setScale(3)).add(change.min(ZERO).abs()).divide(BigDecimal.valueOf(i+1), HALF_UP);
            } else {
                ref.avgGain = ref.avgGain.multiply(pMinus1)
                        .add(change.max(ZERO))
                        .divide(p, HALF_UP);
                ref.avgLoss = ref.avgLoss.multiply(pMinus1)
                        .add(change.min(ZERO).abs())
                        .divide(p, HALF_UP);
            }


            return Optional.of(ref)
                    .filter(r -> r.avgLoss.compareTo(ZERO) > 0)
                    .filter(r -> r.avgGain.compareTo(ZERO) > 0)
                    .map(r -> {
                        BigDecimal relativeStrength = r.avgGain.divide(r.avgLoss, HALF_UP);
                        return HUNDRED
                                .subtract(HUNDRED.divide(ONE.add(relativeStrength), HALF_UP));
            });
        }).orElseGet(() -> {
            ref.prev = t;
            return Optional.empty();
        });
    }


}
