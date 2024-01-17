package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.Momentum;
import com.github.dfauth.ta.model.PriceAction;
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
public class MomentumController extends PriceController {

    // momentum
    @GetMapping("/momentum/{_code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Momentum> momentum(@PathVariable String _code) {
        return momentum(_code,23);
    }

    @GetMapping("/momentum/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Momentum> momentum(@PathVariable String _code, @PathVariable int period) {
        return Momentum.momentum(mapList(prices(_code, period+1), PriceAction.class::cast), period);
    }

}
