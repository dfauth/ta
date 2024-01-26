package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Collectors;
import com.github.dfauth.ta.model.RankListDateCodeComposite;
import com.github.dfauth.ta.model.Ranking;
import com.github.dfauth.ta.repo.RankingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.github.dfauth.ta.model.RankListDateCodeComposite.mapToRankEntry;
import static com.github.dfauth.ta.util.DateOps.sydneyCloseMostRecentWeekday;
import static com.github.dfauth.ta.util.MarketOps.market;

@RestController
@Slf4j
public class RankingController {

    public static Function<RankListDateCodeComposite,String> keyMapper = rlcd -> rlcd.getDate().toString();

    public static Function<RankListDateCodeComposite,Optional<Integer>> valueMapper = RankListDateCodeComposite::getOptionalRank;

    @Autowired
    private RankingRepository repository;

    @GetMapping("/rank/{list}/current/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Integer> latestRank(@PathVariable String list, @PathVariable String code) {
        log.info("ranking list {} code {}",list,code);
        return Ranking.findByCode(list).flatMap(r -> repository.findCurrentByCode(r.ordinal(), code).flatMap(RankListDateCodeComposite::getOptionalRank));
    }

    @GetMapping("/rank/{list}/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Integer> ranks(@PathVariable String list, @PathVariable String code) {
        log.info("ranking list {} code {}",list,code);
        return Ranking.findByCode(list).map(r ->
                repository.findByCode(r.ordinal(), code).stream().flatMap(mapToRankEntry).collect(Collectors.toMapEntry())
        ).orElseThrow();
    }

    @PostMapping("/sync/rank/{list}")
    @ResponseStatus(HttpStatus.OK)
    public Long syncRank(@PathVariable String list, @RequestBody List<List<String>> o) {
        log.info("syncRank {} {}", list, o);
        int r = Ranking.findByCode(list).map(Ranking::ordinal).orElseThrow();
        Timestamp sydneyClose = sydneyCloseMostRecentWeekday();
        final Iterator<List<String>> finalIt = o.iterator();
        List<RankListDateCodeComposite> codes = IntStream.rangeClosed(1, o.size())
                .boxed()
                .flatMap(i -> finalIt.next().stream().map(v -> Map.entry(i,market(v))))
                .map(e -> new RankListDateCodeComposite(r, sydneyClose, e.getValue(), e.getKey()))
                .collect(java.util.stream.Collectors.toList());
        Iterable<RankListDateCodeComposite> it = repository.saveAll(codes);
        return StreamSupport.stream(it.spliterator(), false).count();
    }
}
