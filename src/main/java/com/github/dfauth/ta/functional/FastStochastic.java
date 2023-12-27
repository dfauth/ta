package com.github.dfauth.ta.functional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.functions.Reducers;
import lombok.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FastStochastic implements Stochastic {

    @JsonIgnore private Collection<SlowStochastic> buffer;

    @Override
    public Optional<BigDecimal> getFast() {
        return Optional.ofNullable(buffer.stream().map(SlowStochastic::getK).collect(Reducers.sma()));
    }

    @Override
    public Optional<BigDecimal> getSlow() {
        return buffer.stream().map(SlowStochastic::getK).reduce(Reducers.latest());
    }
}
