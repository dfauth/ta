package com.github.dfauth.ta.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
    private Timestamp date;
    private String code;
    private Integer size;
    private BigDecimal price;
    private BigDecimal cost;
    @Transient
    private Side side;
    @Basic
    @Column(name = "SIDE")
    private int persistedSide;
    private String notes;

    @PostLoad
    void loadSide() {
        if (persistedSide != 0) {
            this.side = Side.fromMultiplier(persistedSide);
        }
    }

    @PrePersist
    void persistSide() {
        if (side != null) {
            this.persistedSide = side.getMultiplier();
        }
    }
}
