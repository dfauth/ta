package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.ATR;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class ATRController {

    @Autowired
    private PriceRepository repository;

    List<Price> prices(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

    // ATR
    @GetMapping("/atr/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> atr(@PathVariable String _code, @PathVariable int period) {
        log.info("atr/{}/{}",_code,period);
        return avgTrueRange(_code, period).map(ATR.AverageTrueRange::getAtr);
    }

    @GetMapping("/avgTrueRange/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<ATR.AverageTrueRange> avgTrueRange(@PathVariable String _code, @PathVariable int period) {
        log.info("avgTrueRange/{}/{}",_code,period);
        return ATR.atr(prices(_code, period*2), period);
    }
}
