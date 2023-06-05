package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.ONE;

public class CAGR {

    BigDecimal cagr(BigDecimal pctRtn, int periods) {
        BigDecimal x = ONE.divide(BigDecimal.valueOf(periods), RoundingMode.HALF_UP).subtract(ONE);
        return BigDecimal.valueOf(Math.pow(ONE.add(pctRtn).doubleValue(),x.doubleValue()));
    }

}
