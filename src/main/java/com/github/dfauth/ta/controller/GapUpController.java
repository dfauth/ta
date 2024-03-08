package com.github.dfauth.ta.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.functional.GapUp;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.math.BigDecimal.ONE;

@RestController
@Slf4j
public class GapUpController extends BaseController implements ControllerMixIn {

    @PostMapping("/gapup")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Integer> gapUp(@RequestBody List<List<String>> codes) {
        try {
            log.info("sma/{}/{}",codes);
            Map<String, Integer> result = mapCode(codes, code -> gapUp(code));
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/gapup/{_code}")
    @ResponseStatus(HttpStatus.OK)
    public Integer gapUp(@PathVariable String _code) {
        log.info("sma/{}",_code);
        int period = 23;
        return tryCatch(() -> {
            List<Price> prices = prices(_code, period);
            return prices.stream().collect(GapUp.collector());
        }, ControllerMixIn.logAndReturn(0));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceVolSMA {
        @JsonIgnore
        private Price latest;
        @JsonIgnore
        private PriceAction priceAction;

        public BigDecimal getClose() {
            return latest.getClose();
        }

        public BigDecimal getSMAClose() {
            return priceAction.getClose();
        }

        public BigDecimal getPctFromSMA() {
            return ONE.subtract(getSMAClose().divide(getClose(), RoundingMode.HALF_UP));
        }

        public int getVolume() {
            return latest.getVolume();
        }

        public int getSMAVolume() {
            return priceAction.getVolume();
        }

        public BigDecimal getPctSMAVolume() {
            return BigDecimal.valueOf(getVolume()/getSMAVolume());
        }
    }
}
