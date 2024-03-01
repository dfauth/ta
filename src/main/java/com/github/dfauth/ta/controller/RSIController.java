package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Dated;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functions.RSI.calculateRSI;

@RestController
@Slf4j
public class RSIController extends PriceController implements ControllerMixIn {

    // rsi
    @GetMapping("/rsi/{_code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> rsi(@PathVariable String _code) {
        return rsi(_code,14);
    }

    @GetMapping("/rsi/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> rsi(@PathVariable String _code, @PathVariable int period) {
        return calculateRSI(mapList(prices(_code, period+2), d -> d.map(PriceAction::getClose)), period).map(Dated::getPayload);
    }

    @PostMapping("/rsi/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,BigDecimal> rsi(@RequestBody List<List<String>> codes, @PathVariable int period) {
        return flatMapCode(codes, code -> rsi(code, period).stream());
    }

}
