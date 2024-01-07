package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functions.RSI.calculateRSI;

@RestController
@Slf4j
public class RSIController extends PriceController {

    // rsi
    @GetMapping("/rsi/{_code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> rsi(@PathVariable String _code) {
        return rsi(_code,14);
    }

    @GetMapping("/rsi/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> rsi(@PathVariable String _code, @PathVariable int period) {
        return calculateRSI(mapList(prices(_code, period+2), Price::getClose), period);
    }

}
