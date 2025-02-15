package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.MarketDepth;
import com.github.dfauth.ta.model.MktDepth;
import com.github.dfauth.ta.service.MktDepthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functional.Lists.sortBy;
import static com.github.dfauth.ta.util.StreamOps.stream;

@RestController
@Slf4j
@CrossOrigin(origins = "https://sharetrading.westpac.com.au")
public class MktDepthController implements ControllerMixIn {

    @Autowired
    private MktDepthService mktDepthService;

    @PostMapping("/mktDepth/sync/{code}")
    @ResponseStatus(HttpStatus.OK)
    public void sync(@PathVariable String code,@RequestBody Map<String,Double> body) {
        log.info("mkt depth {} {}",code, body);
        var builder = MktDepth.builder(code);
        Optional.ofNullable(body.get("buyers")).map(d -> builder.buyers((int)d.doubleValue()));
        Optional.ofNullable(body.get("buyerShares")).map(d -> builder.buyerShares((int)d.doubleValue()));
        Optional.ofNullable(body.get("sellers")).map(d -> builder.sellers((int)d.doubleValue()));
        Optional.ofNullable(body.get("sellerShares")).map(d -> builder.sellerShares((int)d.doubleValue()));
        Optional.ofNullable(body.get("price")).map(builder::price);
        Optional.ofNullable(body.get("change")).map(builder::change);
        Optional.ofNullable(body.get("volume")).map(d -> builder.volume((int)d.doubleValue()));
        mktDepthService.sync(builder.build());
    }

    @PostMapping("/mktDepth")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, MktDepth> mktDepth(@RequestBody List<List<String>> codes) {
        try {
            log.info("mktDepth/{}",codes);
            Map<String, MktDepth> result = mapCode(codes, code -> mktDepth(code)).entrySet().stream().flatMap(e -> e.getValue().map(v -> Map.entry(e.getKey(), v)).stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/mktDepth/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<MktDepth> mktDepth(@PathVariable String code) {
        return mktDepth(code, m -> true);
    }

    @GetMapping("/mktDepth/today/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<MktDepth> mktDepthToday(@PathVariable String code) {
        return mktDepth(code, m -> m.getLocalDate().equals(LocalDate.now()));
    }

    private Optional<MktDepth> mktDepth(@PathVariable String code, @PathVariable Predicate<MktDepth> p) {
        try {
            log.info("mkt depth {} {}",code);
            return mktDepthService.findById(code).stream().filter(p).reduce(new MarketDepth(), MarketDepth::add, oops()).byCode(code).stream().reduce(MktDepth::trend);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @GetMapping("/mktDepth/ratio/{threshold}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<MktDepth> mktDepthLatest(@PathVariable Double threshold) {
        return sortBy(MktDepth.comparator).apply(mktDepthLatest().stream().filter(m -> m.getSharesRatio().map(r -> r >= threshold).orElse(false)).toList());
    }

    @GetMapping("/mktDepth/latest")
    @ResponseStatus(HttpStatus.OK)
    public Collection<MktDepth> mktDepthLatest() {
        return mktDepthLatest(LocalDate.now());
    }

    @GetMapping("/mktDepth/latest/{date}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<MktDepth> mktDepthLatest(String date) {
        return mktDepthLatest(LocalDate.parse(date));
    }

    private Collection<MktDepth> mktDepthLatest(LocalDate date) {
        return stream(mktDepthService.findLatest(date).iterator()).reduce(new MarketDepth(), MarketDepth::add, oops()).byDate(date);
    }

    @GetMapping("/mktDepth/count")
    @ResponseStatus(HttpStatus.OK)
    public long mktDepthCount() {
        return stream(mktDepthService.findAll().iterator()).count();
    }
}
