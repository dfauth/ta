package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.model.MktDepth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.TreeMap;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketDepth {

    @JsonIgnore
    private TreeMap<LocalDate,TreeMap<String,MktDepth>> sortedDateList = new TreeMap<>();
    @JsonIgnore
    private TreeMap<String,TreeMap<LocalDate,MktDepth>> sortedCodeList = new TreeMap<>();

    public MarketDepth add(MktDepth mktDepth) {
        sortedDateList.computeIfAbsent(mktDepth.getLocalDate(), k -> treeMap(mktDepth.getCode(), mktDepth));
        sortedDateList.computeIfPresent(mktDepth.getLocalDate(), (k,v) -> {
            v.computeIfAbsent(mktDepth.getCode(), _k -> mktDepth);
            v.computeIfPresent(mktDepth.getCode(), (_k,_v) -> _v.avg(mktDepth));
            return v;
        });
        sortedCodeList.computeIfAbsent(mktDepth.getCode(), k -> treeMap(mktDepth.getLocalDate(), mktDepth));
        sortedCodeList.computeIfPresent(mktDepth.getCode(), (k,v) -> {
            v.computeIfAbsent(mktDepth.getLocalDate(), _k -> mktDepth);
            v.computeIfPresent(mktDepth.getLocalDate(), (_k,_v) -> _v.avg(mktDepth));
            return v;
        });
        return this;
    }

    private static <K> TreeMap<K,MktDepth> treeMap(K k, MktDepth mktDepth) {
        TreeMap<K, MktDepth> m = new TreeMap<>();
        m.put(k, mktDepth);
        return m;
    }

    public Collection<MktDepth> byCode(String code) {
        return Optional.ofNullable(sortedCodeList.get(code)).map(TreeMap::values).orElse(Collections.emptyList());
    }

    public Collection<MktDepth> byDate(LocalDate date) {
        return Optional.ofNullable(sortedDateList.get(date)).map(TreeMap::values).orElse(Collections.emptyList());
    }
}
