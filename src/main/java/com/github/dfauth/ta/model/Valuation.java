package com.github.dfauth.ta.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Slf4j
@Entity
@Data
@IdClass(CodeDateCompositeKey.class)
public class Valuation {

    @Id private String code;
    @Id private Timestamp _date;
    private Rating rating;
    private Integer buy;
    private Integer hold;
    private Integer sell;
    private BigDecimal target;

    public Valuation() {
    }

    public Valuation(String code, Timestamp _date, Rating rating, Integer buy, Integer hold, Integer sell, BigDecimal target) {
        this.code = code;
        this._date = _date;
        this.rating = rating;
        this.buy = buy;
        this.hold = hold;
        this.sell = sell;
        this.target = target;
    }

    public CodeDateCompositeKey getKey() {
        return new CodeDateCompositeKey(code, _date);
    }
}
