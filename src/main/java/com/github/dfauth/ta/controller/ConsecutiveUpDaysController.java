package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.ConsecutiveUpDays;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;

@RestController
@Slf4j
public class ConsecutiveUpDaysController extends PriceController {

    // rsi
    @GetMapping("/consecutiveUpDays/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Integer> consecutiveUpDays(@PathVariable String _code, @PathVariable int period) {
        return ConsecutiveUpDays.consecutiveUpDays(mapList(prices(_code, period), Price::getClose));
    }

}
