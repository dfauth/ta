package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class PriceCompositeKey implements Serializable {

    private String code;
    private Timestamp _date;

    public PriceCompositeKey() {
    }

    public PriceCompositeKey(String code, Timestamp date) {
        this.code = code;
        this._date = date;
    }



}
