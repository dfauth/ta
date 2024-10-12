package com.github.dfauth.ta.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@IdClass(CodeDateCompositeKey.class)
public class Position {

    @Id
    private Timestamp date;
    @Id
    private String code;
    private Integer size;
    private BigDecimal cost;

    public Position(Trade t) {
        this(List.of(t));
    }

    public Position(List<Trade> t) {
        this.code = t.stream().map(Trade::getCode).findFirst().orElseThrow();
        if(t.stream().map(Trade::getCode).count() != t.size()) {
            throw new IllegalArgumentException("list of trades has inconsistent codes: "+t);
        }
        this.date = t.stream().map(Trade::getDate).mapToLong(Timestamp::getTime).max().stream().mapToObj(Timestamp::new).findFirst().orElseThrow();
        this.size = t.stream().mapToInt(_t -> _t.getSide().valueOf(_t.getSize())).sum();
        this.cost = t.stream().map(_t -> _t.getSide().valueOf(_t.getCost())).reduce(BigDecimal::add).orElseThrow();
    }

    public Position onTrade(Trade t) {
        if(!t.getCode().equals(code)) {
            throw new IllegalArgumentException("Mismatching codes: cannot aggregate positions: "+code+" with "+t.getCode());
        }
        return new Position(t.getDate().toInstant().isAfter(date.toInstant()) ? t.getDate() : date, code, t.getSide().valueOf(t.getSize())+size, t.getSide().valueOf(t.getCost()).add(cost));
    }
}
