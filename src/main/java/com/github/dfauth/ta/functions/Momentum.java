package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Lists.head;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.RingBufferCollector.ringBufferCollector;
import static com.github.dfauth.ta.util.BigDecimalOps.*;

@Slf4j
@Data
@AllArgsConstructor
public class Momentum {

    private final BigDecimal momentum;
    private final int period;

    public BigDecimal getPercentage() {
        return HUNDRED.add(getPercentageIncrease());
    }

    public BigDecimal getPercentageIncrease() {
        return multiply(momentum,HUNDRED);
    }

    public BigDecimal getPercentagePerPeriod() {
        return divide(getPercentageIncrease(), period);
    }

    public static final  Function<List<BigDecimal>, Optional<BigDecimal>> momentumF = l -> head(l)
            .flatMap(first -> last(l).map(last -> divide(last.subtract(first),first)));

    public static Optional<Momentum> momentum(List<PriceAction> prices, int period) {
        return prices.stream().map(PriceAction::getClose).collect(ringBufferCollector(new BigDecimal[period], momentumF)).map(m -> new Momentum(m,period));
    }

}
