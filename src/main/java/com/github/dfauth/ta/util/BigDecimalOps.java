package com.github.dfauth.ta.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.valueOf;

public class BigDecimalOps {

    public static boolean isGreaterThan(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>0;
    }

    public static BigDecimal divide(Integer t, Integer i) {
        return valueOf(t).divide(valueOf(i), RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(BigDecimal bd, Integer i) {
        return bd.divide(valueOf(i), RoundingMode.HALF_UP);
    }
}
