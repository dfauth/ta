package com.github.dfauth.ta.model;

import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CodeDateCompositeKey implements Serializable, Comparable<CodeDateCompositeKey> {

    private String code;
    private Timestamp date;

    public CodeDateCompositeKey(String code, LocalDate date) {
        this(code, new Timestamp(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    @Override
    public int compareTo(CodeDateCompositeKey other) {
        return code.equals(other.code) ? date.getDate() - other.date.getDate() : code.compareTo(other.code);
    }
}
