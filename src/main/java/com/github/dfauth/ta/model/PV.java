package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PV {

    private BigDecimal closingPrice = BigDecimal.ZERO;
    private BigDecimal volume = BigDecimal.ZERO;

    public PV add(Price p) {
        return new PV(
                getClosingPrice().add(p.getClose()),
                getVolume().add(BigDecimal.valueOf(p.getVolume()))
        );
    }

    public PV add(PV pv) {
        return new PV(getClosingPrice().add(pv.getClosingPrice()), getVolume().add(pv.getVolume()));
    }

    public PV divide(int divisor) {
        return new PV(BigDecimalOps.divide(getClosingPrice(), divisor), BigDecimalOps.divide(getVolume(), divisor));
    }
}
