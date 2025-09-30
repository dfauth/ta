package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MaxInvestment {

    private Dated<BigDecimal> value;
    private Dated<BigDecimal> maxValue;

    public MaxInvestment apply(Dated<BigDecimal> value) {
        if (this.value == null) {
            this.value = value;
            this.maxValue = value;
        } else {
            this.value = value.map(v -> v.add(this.value.getPayload()));
            this.maxValue = this.maxValue.flatMap(v -> BigDecimalOps.isGreaterThan(v, this.value.getPayload()) ? maxValue : this.value);
        }
        return this;
    }

    public MaxInvestment merge(MaxInvestment maxInvestment) {
        if (this.value == null) {
            this.value = maxInvestment.value;
            this.maxValue = maxInvestment.maxValue;
        } else {
            this.value = this.value.map(v -> v.add(maxInvestment.value.getPayload()));
            this.maxValue = this.maxValue.flatMap(v -> BigDecimalOps.isGreaterThan(v, maxInvestment.value.getPayload()) ?
                    maxInvestment.maxValue :
                    maxInvestment.value);
        }
        return this;
    }
}
