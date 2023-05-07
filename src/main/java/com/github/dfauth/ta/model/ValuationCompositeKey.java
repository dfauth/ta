package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class ValuationCompositeKey implements Serializable {

    private String code;
    private Timestamp _date;

    public ValuationCompositeKey() {
    }

    public ValuationCompositeKey(String code, Timestamp _date) {
        this.code = code;
        this._date = _date;
    }



}
