package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.stream.Stream;

@Getter
public enum Side {
    Buy(1), Sell(-1);

    private final int multiplier;

    Side(int multiplier) {
        this.multiplier = multiplier;
    }

    public static Side fromMultiplier(int multiplier) {
        return Stream.of(values()).filter(s -> s.multiplier == multiplier).findFirst().orElseThrow();
    }

    public BigDecimal valueOf(BigDecimal bd) {
        return BigDecimalOps.multiply(bd, multiplier);
    }

    public static Side fromString(Object o) {
        if(o instanceof String) {
            return Stream.of(Side.values()).filter(v -> v.name().equalsIgnoreCase((String)o)).findFirst().orElse(Buy);
        } else if(o instanceof Integer) {
            return Side.values()[(Integer)o];
        } else {
            throw new IllegalArgumentException("Unexpected type: "+o);
        }
    }
}
