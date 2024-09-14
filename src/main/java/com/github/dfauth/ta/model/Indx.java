package com.github.dfauth.ta.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.sql.Timestamp;

@Slf4j
@Entity
@Data
@IdClass(IndxKey.class)
public class Indx {

    @Id private String idx;
    @Id private Timestamp date;
    @Id private String code;

    public Indx() {
    }

    public Indx(String idx, Timestamp date, String code) {
        this.idx = idx;
        this.date = date;
        this.code = code;
    }

    public IndxKey getKey() {
        return new IndxKey(idx, date, code);
    }
}
