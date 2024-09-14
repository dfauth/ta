package com.github.dfauth.ta.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Comparator;

@Slf4j
@Entity
@Data
@IdClass(CodeDateCompositeKey.class)
public class Valuation {

    public static Comparator<? super Valuation> sortByDate = Comparator.comparing(v -> v.date);

    @Id private String code;
    @Id private Timestamp date;
    private Rating rating;
    private Integer buy;
    private Integer hold;
    private Integer sell;
    private BigDecimal target;

    public Valuation() {
    }

    public Valuation(String code, Timestamp date, Rating rating, Integer buy, Integer hold, Integer sell, BigDecimal target) {
        this.code = code;
        this.date = date;
        this.rating = rating;
        this.buy = buy;
        this.hold = hold;
        this.sell = sell;
        this.target = target;
    }

    public CodeDateCompositeKey getKey() {
        return new CodeDateCompositeKey(code, date);
    }
}
