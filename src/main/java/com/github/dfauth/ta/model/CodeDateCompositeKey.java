package com.github.dfauth.ta.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class CodeDateCompositeKey implements Serializable {

    private String code;
    private Timestamp date;

    public CodeDateCompositeKey() {
    }

    public CodeDateCompositeKey(String code, Timestamp date) {
        this.code = code;
        this.date = date;
    }



}
