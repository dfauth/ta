package com.github.dfauth.ta.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "CODES")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Code {

    @Id
    private String code;
}
