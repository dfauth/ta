package com.github.dfauth.ta.model;

import jakarta.persistence.GenerationType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String confirmation_no;
    private Timestamp _date;
    private String code;
    private Integer size;
    private BigDecimal price;
    private BigDecimal cost;
    private Side side;
    private String notes;
}
