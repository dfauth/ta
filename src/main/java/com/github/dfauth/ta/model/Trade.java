package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.util.BigDecimalOps;
import com.github.dfauth.ta.util.DateTimeUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import static com.github.dfauth.ta.util.BigDecimalOps.multiply;

@Slf4j
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Trade {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String confirmation_no;
    private Timestamp date;
    private String code;
    private Integer size;
    private BigDecimal price;
    private BigDecimal cost;
    private Side side;
    private String notes;
    private Theme theme;

    public BigDecimal getCommission() {
        return cost.subtract(multiply(price, size)).abs();
    }

    public LocalDate getLocalDate() {
        return DateTimeUtils.toLocalDate(getDate());
    }

    @JsonIgnore
    public BigDecimal getValue() {
        return BigDecimalOps.multiply(cost, getSide().getMultiplier());
    }

    public <N extends Number> N sided(N n) {
        return getSide().sided(n).getSignedValue();
    }
}
