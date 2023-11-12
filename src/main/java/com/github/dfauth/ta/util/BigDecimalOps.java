package com.github.dfauth.ta.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.valueOf;

public class BigDecimalOps {

    public static boolean isGreaterThan(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>0;
    }

    public static boolean isGreaterThanOrEqualTo(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>=0;
    }

    public static BigDecimal divide(Integer t, Integer i) {
        return valueOf(t).divide(valueOf(i), RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(BigDecimal bd, Integer i) {
        return bd.divide(valueOf(i), RoundingMode.HALF_UP);
    }

    public static BigDecimal pctChange(int i1, int i2) {
        return pctChange(i1, BigDecimal.valueOf(i2));
    }

    public static BigDecimal pctChange(int i, BigDecimal bd) {
        return pctChange(BigDecimal.valueOf(i),bd);
    }

    public static BigDecimal pctChange(BigDecimal bd1, BigDecimal bd2) {
        return scale(bd1.subtract(bd2),3).divide(scale(bd2,3),RoundingMode.HALF_UP);
    }

    public static BigDecimal scale(BigDecimal bd, int scale) {
        return bd.setScale(scale, RoundingMode.HALF_UP);
    }
}
