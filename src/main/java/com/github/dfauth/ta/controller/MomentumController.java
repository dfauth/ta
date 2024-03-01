package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.Momentum;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;

@RestController
@Slf4j
public class MomentumController extends PriceController implements ControllerMixIn {

    // momentum
    @GetMapping("/momentum/{_code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Momentum> momentum(@PathVariable String _code) {
        return momentum(_code,23);
    }

    @GetMapping("/momentum/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Momentum> momentum(@PathVariable String _code, @PathVariable int period) {
        try {
            return Momentum.momentum(mapList(prices(_code, period+1), PriceAction.class::cast), period);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @PostMapping("/momentum/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Momentum> momentum(@RequestBody List<List<String>> codes, @PathVariable int period) {
        return flatMapCode(codes, code -> momentum(code, period).stream());
    }

}
