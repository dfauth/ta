package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Stochastic;
import com.github.dfauth.ta.functional.StochasticReducer;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class StochasticController extends BaseController {

    @GetMapping("/stochastic/{_code}/{fast}/{slow}")
    @ResponseStatus(HttpStatus.OK)
    Stochastic stochastic(@PathVariable String _code, @PathVariable int fast, @PathVariable int slow) {
        log.info("stochastic/{}/{}",_code,fast,slow);
        List<Price> prices = prices(_code, fast+slow);
        return prices.stream().collect(new StochasticReducer(fast, slow));
    }
}
