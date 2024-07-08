package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Collectors;
import com.github.dfauth.ta.model.Market;
import com.github.dfauth.ta.model.MarketEnum;
import com.github.dfauth.ta.model.RankListDateCodeComposite;
import com.github.dfauth.ta.model.Ranking;
import com.github.dfauth.ta.repo.RankingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.github.dfauth.ta.model.MarketEnum.ASX;
import static com.github.dfauth.ta.model.RankListDateCodeComposite.mapToRankEntry;

@RestController
@Slf4j
public class RankingController implements ControllerMixIn {

    public static Function<RankListDateCodeComposite,String> keyMapper = rlcd -> rlcd.getDate().toString();

    public static Function<RankListDateCodeComposite,Optional<Integer>> valueMapper = RankListDateCodeComposite::getOptionalRank;

    @Autowired
    private RankingRepository repository;
    private Market market = ASX;

    @GetMapping("/rank/{list}/current/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Integer> latestRank(@PathVariable String list, @PathVariable String code) {
        log.info("ranking list {} code {}",list,code);
        return Ranking.findByCode(list).flatMap(r -> repository.findCurrentByCode(r.ordinal(), code).flatMap(RankListDateCodeComposite::getOptionalRank));
    }

    @GetMapping("/ranks/localdate/{list}/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Map<LocalDate,Integer> ranksByLocalDate(@PathVariable String list, @PathVariable String code) {
        log.info("ranking list {} code {}",list,code);
        return Ranking.findByCode(list).map(r ->
                repository.findByCode(r.ordinal(), code).stream().flatMap(mapToRankEntry).collect(Collectors.toMapEntry())
        ).orElseThrow();
    }

    @GetMapping("/ranks/{list}/{code}/{date}")
    @ResponseStatus(HttpStatus.OK)
    public Integer ranks(@PathVariable String list, @PathVariable String code, @PathVariable String date) {
        log.info("ranking list {} code {} date {}",list,code,date);
        return ranksByLocalDate(list,code).get(LocalDate.parse(date));
    }

    @PostMapping("/ranks/int/{list}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Map<Integer,Integer>> ranksByInt(@PathVariable String list, @RequestBody List<List<String>> codes) {
        log.info("ranking list {} code {}",list,codes);
        return mapCode(codes, code -> ranksByInt(list,code));
    }

    @GetMapping("/ranks/int/{list}/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Map<Integer,Integer> ranksByInt(@PathVariable String list, @PathVariable String code) {
        log.info("ranking list {} code {}",list,code);
        Map<LocalDate,Integer> sorted = new TreeMap(ranksByLocalDate(list, code));
        AtomicInteger i= new AtomicInteger();
        ToIntFunction<LocalDate> _keyMapper = k -> sorted.size() - i.getAndIncrement();
        Map<Integer, Integer> result = sorted.entrySet().stream().map(e -> Map.entry(_keyMapper.applyAsInt(e.getKey()), e.getValue())).collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return result;
    }

    @GetMapping("/rank/{list}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Map<LocalDate,Integer>> ranks(@PathVariable String list) {
        log.info("ranking list {}",list);
        return Ranking.findByCode(list).map(r ->
                repository.findByRanking(r.ordinal()).stream().flatMap(rldc -> rldc.getOptionalRank().stream().map(ignore -> rldc)).collect(Collectors.toMap(RankListDateCodeComposite::getCode, Ranking.reMapper))
        ).orElseThrow();
    }

    @PostMapping("/sync/rank/{list}")
    @ResponseStatus(HttpStatus.OK)
    public Long syncRank(@PathVariable String list, @RequestBody List<List<String>> o) {
        log.info("syncRank {} {}", list, o);
        int r = Ranking.findByCode(list).map(Ranking::ordinal).orElseThrow();
        Timestamp sydneyClose = new Timestamp(market.atMarketCloseOn(market.getMarketDate()).toEpochMilli());
        final Iterator<List<String>> finalIt = o.iterator();
        List<RankListDateCodeComposite> codes = IntStream.rangeClosed(1, o.size())
                .boxed()
                .flatMap(i -> finalIt.next().stream().map(v -> Map.entry(i,market.withCode(v))))
                .map(e -> new RankListDateCodeComposite(r, sydneyClose, e.getValue(), e.getKey()))
                .collect(java.util.stream.Collectors.toList());
        Iterable<RankListDateCodeComposite> it = repository.saveAll(codes);
        return StreamSupport.stream(it.spliterator(), false).count();
    }

    @PostMapping("/sync/rank/sp500/{list}")
    @ResponseStatus(HttpStatus.OK)
    public Long syncRankSP500(@PathVariable String list, @RequestBody List<List<String>> o) {
        log.info("syncRankSP500 {} {}", list, o);
        MarketEnum mkt = MarketEnum.NYSE;
        int r = Ranking.findByCode(list).map(Ranking::ordinal).orElseThrow();
        Timestamp mktClose = new Timestamp(mkt.atMarketCloseOn(mkt.getMarketDate()).toEpochMilli());
        final Iterator<List<String>> finalIt = o.iterator();
        List<RankListDateCodeComposite> codes = IntStream.rangeClosed(1, o.size())
                .boxed()
                .flatMap(i -> finalIt.next().stream().map(v -> Map.entry(i,mkt.withCode(v))))
                .map(e -> new RankListDateCodeComposite(r, mktClose, e.getValue(), e.getKey()))
                .collect(java.util.stream.Collectors.toList());
        Iterable<RankListDateCodeComposite> it = repository.saveAll(codes);
        return StreamSupport.stream(it.spliterator(), false).count();
    }
}
