package com.github.dfauth.ta.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.functional.BollingerBand;
import com.github.dfauth.ta.functional.Lists;
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

import static com.github.dfauth.ta.functional.BollingerBand.calculateBollingerBands;
import static com.github.dfauth.ta.functional.Lists.last;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.math.BigDecimal.ONE;
import static java.util.Optional.empty;

@RestController
@Slf4j
public class BBController extends BaseController implements ControllerMixIn {

    @PostMapping("/bb/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, BollingerBand.BBPoint> bb(@RequestBody List<List<String>> codes, @PathVariable int period) {
        log.info("bb/{}/{}",codes,period);
        return flatMapCode(codes, code -> bb(code, period).stream());
    }

    @GetMapping("/bb/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BollingerBand.BBPoint> bb(@PathVariable String _code, @PathVariable int period) {
        log.info("bb/{}/{}",_code,period);
        return tryCatch(() -> {
            List<PriceAction> prices = Lists.mapList(prices(_code, period+1),PriceAction.class::cast);
            List<BollingerBand.BBPoint> bb = calculateBollingerBands(period,2.0).apply(prices);
            return last(bb);
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
