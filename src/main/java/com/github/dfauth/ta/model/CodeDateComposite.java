package com.github.dfauth.ta.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Slf4j
@Entity
@Data
@IdClass(CodeDateCompositeKey.class)
@Table(name = "FUNDAMENTALLY_SOUND")
public class CodeDateComposite {

    @Id private String code;
    @Id private Timestamp _date;

    public CodeDateComposite() {
    }

    public CodeDateComposite(Timestamp _date, String code) {
        this._date = _date;
        this.code = code;
    }

    public CodeDateCompositeKey getKey() {
        return new CodeDateCompositeKey(code, _date);
    }
}
