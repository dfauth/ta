package com.github.dfauth.ta.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Optional;

import static com.github.dfauth.ta.functional.RingBufferCollector.ringBufferCollector;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.math.BigDecimal.ONE;
import static java.util.Optional.empty;

@RestController
@Slf4j
public class SMAController extends BaseController implements ControllerMixIn {

    @PostMapping("/sma/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, PriceAction> sma(@RequestBody List<List<String>> codes, @PathVariable int period) {
        try {
            log.info("sma/{}/{}",codes,period);
            Map<String, PriceAction> result = flatMapCode(codes, code -> sma(code, period).stream());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/sma/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<PriceAction> sma(@PathVariable String _code, @PathVariable int period) {
        log.info("sma/{}/{}",_code,period);
        return tryCatch(() -> {
            List<Price> prices = prices(_code, period);
            return Optional.of(prices.stream().collect(ringBufferCollector(new PriceAction[period], PriceAction.SMA)));
        }, ControllerMixIn.logAndReturn(empty()));
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
