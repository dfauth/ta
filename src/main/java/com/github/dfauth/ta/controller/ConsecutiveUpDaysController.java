package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.ConsecutiveUpDays;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;

@RestController
@Slf4j
public class ConsecutiveUpDaysController extends PriceController implements ControllerMixIn {

    @GetMapping("/consecutiveUpDays/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Integer> consecutiveUpDays(@PathVariable String _code, @PathVariable int period) {
        return ConsecutiveUpDays.consecutiveUpDays(mapList(prices(_code, period), Price::getClose));
    }

    @PostMapping("/consecutiveUpDays/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Integer> consecutiveUpDays(@RequestBody List<List<String>> codes, @PathVariable int period) {
        return flatMapCode(codes, code -> consecutiveUpDays(code, period).stream());
    }

}
