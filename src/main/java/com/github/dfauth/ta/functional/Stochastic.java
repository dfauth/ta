package com.github.dfauth.ta.functional;

import java.math.BigDecimal;

public interface Stochastic {
    BigDecimal getFast();
    BigDecimal getSlow();
}
