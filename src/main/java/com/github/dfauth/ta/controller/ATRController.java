package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.ATR;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
public class ATRController implements ControllerMixIn {

    @Autowired
    private PriceRepository repository;

    List<Price> prices(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

    // ATR
    @GetMapping("/atr/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> atr(@PathVariable String _code, @PathVariable int period) {
        log.info("atr/{}/{}",_code,period);
        return avgTrueRange(_code, period).map(ATR.AverageTrueRange::getAtr);
    }

    @GetMapping("/avgTrueRange/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<ATR.AverageTrueRange> avgTrueRange(@PathVariable String _code, @PathVariable int period) {
        try {
            log.info("avgTrueRange/{}/{}",_code,period);
            return ATR.atr(prices(_code, period*2), period);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @PostMapping("/avgTrueRange/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,ATR.AverageTrueRange> avgTrueRange(@RequestBody List<List<String>> codes, @PathVariable int period) {
        log.info("avgTrueRange/{}/{}",codes,period);
        return flatMapCode(codes, code -> avgTrueRange(code, period).stream());
    }
}
