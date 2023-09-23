package com.github.dfauth.ta.util;

import java.math.BigDecimal;

public class BigDecimalOps {

    public static boolean isGreaterThan(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>0;
    }
}
