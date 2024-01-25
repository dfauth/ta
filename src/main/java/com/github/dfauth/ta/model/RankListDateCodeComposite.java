package com.github.dfauth.ta.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.sql.Timestamp;
import java.util.Optional;

@Slf4j
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ListDateCodeCompositeKey.class)
public class RankListDateCodeComposite {

    @Id private int id;
    @Id private Timestamp date;
    @Id private String code;
    private Optional<Integer> rank;

    public ListDateCodeCompositeKey getKey() {
        return new ListDateCodeCompositeKey(id, date, code);
    }
}
