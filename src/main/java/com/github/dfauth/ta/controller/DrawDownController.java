package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.Drawdown;
import com.github.dfauth.ta.model.PriceAction;
import io.github.dfauth.trycatch.ExceptionalRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.github.dfauth.ta.functional.Lists.mapList;

@RestController
@Slf4j
public class DrawDownController extends BaseController implements ControllerMixIn {

    @PostMapping("/drawDown")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Drawdown> drawDown(@RequestBody List<List<String>> codes) {
        try {
            log.info("drawDown/{}",codes);
            Map<String, Drawdown> result = mapCode(codes, code -> drawDown(code));
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/drawDown/{_code}")
    @ResponseStatus(HttpStatus.OK)
    public Drawdown drawDown(@PathVariable String _code) {
        log.info("drawDown/{}",_code);
        int period = 252;
        return ExceptionalRunnable.<Drawdown>tryCatch(() -> {
            List<PriceAction> prices = mapList(prices(_code, period),PriceAction.class::cast);
            return Drawdown.drawDownPrice(prices);
        }, ControllerMixIn.logAndReturn(new Drawdown()));
    }
}
