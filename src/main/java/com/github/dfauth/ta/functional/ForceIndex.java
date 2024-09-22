package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.github.dfauth.ta.functional.Collectors.consecutive;
import static com.github.dfauth.ta.functional.RingBufferCollector.ringBufferCollector;
import static com.github.dfauth.ta.util.BigDecimalOps.multiply;

@Slf4j
@AllArgsConstructor
@Data
public class ForceIndex {

    public static BiFunction<PriceAction,PriceAction,BigDecimal> forceIndex = (previous, current) -> multiply(current.getClose().subtract(previous.getClose()),current.getVolume());

    public static Optional<ForceIndex> calculateForceIndex(List<PriceAction> priceAction, int smaPeriod, int emaPeriod) {

        List<BigDecimal> result = priceAction
                .stream()
                .collect(consecutive(forceIndex));
        Optional<BigDecimal> sma = result.stream()
                .collect(ringBufferCollector(new BigDecimal[smaPeriod], BigDecimalOps.sma()));
        Optional<BigDecimal> ema = result.stream()
                .collect(ringBufferCollector(new BigDecimal[emaPeriod], BigDecimalOps.ema()));
        return ema.flatMap(_ema -> sma.map(_sma -> new ForceIndex(_ema,_sma)));
    }

    private final BigDecimal shortPeriod;
    private final BigDecimal longPeriod;

    public BigDecimal idx() {
        return BigDecimalOps.divide(shortPeriod, longPeriod);
    }
}
