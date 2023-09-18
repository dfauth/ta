package com.github.dfauth.ta.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Slf4j
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Trade {

    @Id private String id;
    private Timestamp _date;
    private String code;
    private Integer size;
    private BigDecimal price;
    private BigDecimal cost;
    private Side side;
    private String notes;
}
