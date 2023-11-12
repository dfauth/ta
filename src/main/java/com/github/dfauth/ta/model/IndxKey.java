package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class IndxKey implements Serializable {

    private String idx;
    private Timestamp _date;
    private String code;

    public IndxKey() {
    }

    public IndxKey(String idx, Timestamp _date, String code) {
        this.idx = idx;
        this._date = _date;
        this.code = code;
    }



}
