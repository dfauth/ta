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
    @Id private Timestamp _date;
    @Id private String code;

    public Indx() {
    }

    public Indx(String idx, Timestamp _date, String code) {
        this.idx = idx;
        this._date = _date;
        this.code = code;
    }

    public IndxKey getKey() {
        return new IndxKey(idx, _date, code);
    }
}
