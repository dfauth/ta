package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class IndxKey implements Serializable {

    private String idx;
    private Timestamp date;
    private String code;

    public IndxKey() {
    }

    public IndxKey(String idx, Timestamp date, String code) {
        this.idx = idx;
        this.date = date;
        this.code = code;
    }
}
