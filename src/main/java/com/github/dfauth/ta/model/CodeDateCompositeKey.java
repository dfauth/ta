package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class CodeDateCompositeKey implements Serializable {

    private String code;
    private Timestamp _date;

    public CodeDateCompositeKey() {
    }

    public CodeDateCompositeKey(String code, Timestamp _date) {
        this.code = code;
        this._date = _date;
    }



}
