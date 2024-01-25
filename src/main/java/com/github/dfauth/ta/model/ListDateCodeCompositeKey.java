package com.github.dfauth.ta.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListDateCodeCompositeKey implements Serializable {

    private int id;
    private Timestamp date;
    private String code;
}
