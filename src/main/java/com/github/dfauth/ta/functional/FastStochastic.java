package com.github.dfauth.ta.functional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.functions.Reducers;
import lombok.*;

import java.math.BigDecimal;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FastStochastic implements Stochastic {

    @JsonIgnore private Collection<SlowStochastic> buffer;

    @Override
    public BigDecimal getFast() {
        return buffer.stream().map(SlowStochastic::getK).collect(Reducers.sma());
    }

    @Override
    public BigDecimal getSlow() {
        return buffer.stream().map(SlowStochastic::getK).reduce(Reducers.latest()).orElseThrow();
    }
}
