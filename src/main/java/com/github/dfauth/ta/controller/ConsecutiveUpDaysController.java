package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.ConsecutiveUpDays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class ConsecutiveUpDaysController extends PriceController implements ControllerMixIn {

    @GetMapping("/consecutiveUpDays/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Integer consecutiveUpDays(@PathVariable String _code, @PathVariable int period) {
        return prices(_code, period).stream().collect(ConsecutiveUpDays.collector());
    }

    @PostMapping("/consecutiveUpDays/{period}")
    @ResponseStatus(HttpStatus.OK)
    public List<Integer> consecutiveUpDays(@RequestBody List<List<String>> codes, @PathVariable int period) {
        return mapCode(codes, code -> consecutiveUpDays(code, period));
    }

}
