package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class FundamentallySoundCompositeKey implements Serializable {

    private String code;
    private Timestamp _date;

    public FundamentallySoundCompositeKey() {
    }

    public FundamentallySoundCompositeKey(String code, Timestamp _date) {
        this.code = code;
        this._date = _date;
    }



}
