package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class PriceCompositeKey implements Serializable {

    private String _code;
    private Timestamp _date;

    public PriceCompositeKey() {
    }

    public PriceCompositeKey(String _code, Timestamp _date) {
        this._code = _code;
        this._date = _date;
    }



}
