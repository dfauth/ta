package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.RankListDateCodeComposite;
import com.github.dfauth.ta.model.Ranking;
import com.github.dfauth.ta.repo.RankingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@RestController
@Slf4j
public class RankingController {

    public static Function<RankListDateCodeComposite,String> keyMapper = rlcd -> rlcd.getDate().toString();

    public static Function<RankListDateCodeComposite,Optional<Integer>> valueMapper = RankListDateCodeComposite::getRank;

    @Autowired
    private RankingRepository repository;

    @GetMapping("/rank/{list}/current/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Integer> latestRank(@PathVariable String list, @PathVariable String code) {
        log.info("ranking list {} code {}",list,code);
        return Ranking.findByCode(list).flatMap(r -> repository.findCurrentByCode(r.ordinal(), code).flatMap(RankListDateCodeComposite::getRank));
    }

    @GetMapping("/rank/{list}/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Integer> ranks(@PathVariable String list, @PathVariable String code) {
        log.info("ranking list {} code {}",list,code);
//        return Ranking.findByCode(list).stream().map(r ->
//                f.apply(repository.findByCode(r.ordinal(), code).stream()))).collect(Collectors.toMap(e -> e.getKey().toString()), Map.Entry::getValue));
//                repository.findByCode(r.ordinal(), code).stream().map(rldc -> Map.entry(rldc.getDate(), rldc.getRank())).map(OptionalOps::flattenEntry).collect(Collectors.toMap(e -> e.getKey().toString()), Map.Entry::getValue));
        return Collections.emptyMap();
    }

//    public static Function<Stream<RankListDateCodeComposite>, Stream<Map.Entry<String,Integer>>> f = s -> flattenEntry(s, rldc -> Map.entry(rldc.getDate().toString(), rldc.getRank()));
}
