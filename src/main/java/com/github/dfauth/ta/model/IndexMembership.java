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
@IdClass(CodeDateCompositeKey.class)
public class IndexMembership {

    @Id private String code;
    @Id private Timestamp _date;

    public IndexMembership() {
    }

    public IndexMembership(Timestamp _date, String code) {
        this._date = _date;
        this.code = code;
    }

    public CodeDateCompositeKey getKey() {
        return new CodeDateCompositeKey(code, _date);
    }
}
