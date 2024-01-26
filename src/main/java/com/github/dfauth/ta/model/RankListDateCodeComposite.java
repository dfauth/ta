package com.github.dfauth.ta.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ListDateCodeCompositeKey.class)
@Table(name = "RANKING")
public class RankListDateCodeComposite {

    public static Function<RankListDateCodeComposite, Stream<Map.Entry<String, Integer>>> mapToRankEntry = rldc -> rldc.getOptionalRank().map(rank -> Map.entry(rldc.getDate().toString(), rank)).stream();

    @Id private int id;
    @Id @Column(name = "ATDATE") private Timestamp date;
    @Id private String code;
    @Column(name = "_RANK") private Integer rank;

    public ListDateCodeCompositeKey getKey() {
        return new ListDateCodeCompositeKey(id, date, code);
    }

    public Optional<Integer> getOptionalRank() {
        return Optional.ofNullable(rank);
    }
}