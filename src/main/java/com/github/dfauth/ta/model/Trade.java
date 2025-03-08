package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.DateTimeUtils;
import jakarta.persistence.*;
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
}
