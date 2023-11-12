package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.DaysSince;
import com.github.dfauth.ta.functional.LastHigh;
import com.github.dfauth.ta.functions.ConsecutiveUpDays;
import com.github.dfauth.ta.functions.HighLow;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.FunctionUtils.windowfy;
import static com.github.dfauth.ta.functional.HistoricalOffset.zipWithHistoricalOffset;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functions.Reducers.latest;

@RestController
@Slf4j
public class MaxMinController {

    @Autowired
    private PriceRepository repository;

    // 52wkMax
    @GetMapping("/max/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> max(@PathVariable String _code, @PathVariable int period) {
        log.info("max/{}/{}",_code,period);
        return closeOperation(_code, period, HighLow::max);
    }

    // 52wkMin
    @GetMapping("/min/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> min(@PathVariable String _code, @PathVariable int period) {
        log.info("min/{}/{}",_code,period);
        return closeOperation(_code, period, HighLow::min);
    }

    // days since
    @GetMapping("/daysSinceLastHigh/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<Integer> daysSinceLastHigh(@PathVariable String _code, @PathVariable int period) {
        log.info("daysSinceLastHigh/{}/{}",_code,period);
        return DaysSince.lastHigh(prices(_code, period).stream()
                .map(Price::get_close)
                .collect(Collectors.toList()));
    }

    // days since
    @GetMapping("/recentHigh/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<DaysSince.RecentHigh> recentLastHigh(@PathVariable String _code, @PathVariable int period) {
        log.info("recentHigh/{}/{}",_code,period);
        return DaysSince.recentHigh(prices(_code, period).stream()
                .collect(Collectors.toList()));
    }

    // pct below previous high
    @GetMapping("/pctBelowPreviousHigh/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> pctBelowPreviousHigh(@PathVariable String _code, @PathVariable int period) {
        log.info("pctBelowPreviousHigh/{}/{}",_code,period);
        return LastHigh.pctBelow(prices(_code, period).stream()
                .map(Price::get_close)
                .collect(Collectors.toList()));
    }

    // consecutive up days
    @GetMapping("/consecutiveUpDays/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "http://localhost:3000", methods = RequestMethod.GET)
    Optional<ConsecutiveUpDays.PriceInterval> consecutiveUpDays(@PathVariable String _code, @PathVariable int period) {
        log.info("consecutiveUpDays/{}/{}",_code,period);
        return last(zipWithHistoricalOffset(prices(_code, period))
                .collect(new ConsecutiveUpDays()))
                .filter(ConsecutiveUpDays.PriceInterval::isCurrent);
    }

    private Optional<BigDecimal> closeOperation(String _code, int period, Function<List<BigDecimal>, Optional<BigDecimal>> f1) {
        Function<BigDecimal, Optional<BigDecimal>> f = windowfy(period, f1);
        return prices(_code, period).stream()
                .map(Price::get_close)
                .map(f)
                .flatMap(Optional::stream)
                .reduce(latest());
    }

    List<Price> prices(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

}
