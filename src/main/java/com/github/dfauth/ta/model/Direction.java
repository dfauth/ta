package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Optional;

import static com.github.dfauth.ta.util.BigDecimalOps.toType;
import static java.util.Arrays.stream;

@Getter
public enum Direction {

    BUY(-1), SELL(1);

    private final int multiplier;

    Direction(int multiplier) {
        this.multiplier = multiplier;
    }

    public <N extends Number> N getValue(N n) {
        return (N) toType(n.getClass()).apply(BigDecimalOps.valueOf(n).multiply(BigDecimal.valueOf(multiplier)));
    }

    public static Optional<Direction> fromString(String label) {
        return stream(values()).filter(v -> v.name().equalsIgnoreCase(label)).findFirst();
    }
}
