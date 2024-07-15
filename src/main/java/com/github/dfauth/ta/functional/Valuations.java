package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.Valuation;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Slf4j
public class Valuations {

    public static List<BigDecimal> valuationChange(List<Valuation> valuations) {
        BinaryOperator<BigDecimal> f = BigDecimalOps::pctChangeOrZero;
        return HistoricalOffset.zipWithPrevious(valuations).map(z -> new Tuple2<>(z.getPrevious().getTarget(), z.getCurrent().getTarget())).map(t -> t.map(f)).collect(Collectors.toList());
    }
}
