package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Trend;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;

@RestController
@Slf4j
public class ThingyController extends BaseController implements ControllerMixIn {

    @PostMapping("/thingy/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Trend> thingy(@PathVariable int period, @RequestBody List<List<String>> codes) {
        try {
            log.info("thingy/{}",codes);
            Map<String, Trend> result = flatMapCode(codes, code -> thingy(period, code).stream());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/thingy/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Trend> thingy(@PathVariable int period, @PathVariable String _code) {
        log.info("thingy/{}",_code);
        return tryCatch(() -> {
            List<PriceAction> prices = mapList(prices(_code, period),PriceAction.class::cast);
            return Trend.calculateTrend(prices);
        }, ControllerMixIn.logAndReturn(Optional.empty()));
    }
}
