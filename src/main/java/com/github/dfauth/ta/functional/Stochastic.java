package com.github.dfauth.ta.functional;

import java.math.BigDecimal;
import java.util.Optional;

public interface Stochastic {
    Optional<BigDecimal> getFast();
    Optional<BigDecimal> getSlow();
}
