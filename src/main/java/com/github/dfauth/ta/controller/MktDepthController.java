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

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.util.StreamOps.stream;

@RestController
@Slf4j
@CrossOrigin(origins = "https://sharetrading.westpac.com.au")
public class MktDepthController implements ControllerMixIn {

    @Autowired
    private MktDepthService mktDepthService;

    @PostMapping("/mktDepth/sync/{code}")
    @ResponseStatus(HttpStatus.OK)
    public void sync(@PathVariable String code,@RequestBody Map<String,Integer[]> body) {
        log.info("mkt depth {} {}",code, body);
        var builder = MktDepth.builder(code);
        Optional.ofNullable(body.get("buyers")).map(ints -> builder.buyers(ints[0]).buyerShares(ints[1]));
        Optional.ofNullable(body.get("sellers")).map(ints -> builder.sellers(ints[0]).sellerShares(ints[1]));
        mktDepthService.sync(builder.build());
    }

    @PostMapping("/mktDepth")
    @ResponseStatus(HttpStatus.OK)
    public long mktDepth(@RequestBody List<List<String>> codes) {
        try {
            log.info("mktDepth/{}",codes);
            Map<String, Collection<MktDepth>> result = mapCode(codes, code -> mktDepth(code));
            return result.values().stream().count();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/mktDepth/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<MktDepth> mktDepth(@PathVariable String code) {
        log.info("mkt depth {} {}",code);
        return mktDepthService.findById(code).stream().reduce(new MarketDepth(), MarketDepth::add, oops()).byCode(code);
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
