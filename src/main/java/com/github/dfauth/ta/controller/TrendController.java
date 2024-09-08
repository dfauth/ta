package com.github.dfauth.ta.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.functional.Maps;
import com.github.dfauth.ta.functional.Trend;
import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.util.BigDecimalOps.divide;
import static com.github.dfauth.ta.util.BigDecimalOps.pctChangeOrZero;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;

@RestController
@Slf4j
public class TrendController extends BaseController implements ControllerMixIn {

    @PostMapping("/trend2/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Trend2> trend2(@PathVariable int period, @RequestBody List<List<String>> codes) {
        try {
            log.info("trend2/{}",codes);
            return Maps.mapValues(trend(period, codes), t -> t.map(Trend2::new));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/trend2/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<PFSL> trend2(@PathVariable int period, @PathVariable String _code) {
        log.info("trend2/{}",_code);
        return trend(period, _code).map(t -> t.map((c,p) -> PFSL.getFrom(c)));
    }

    @PostMapping("/trend/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Trend> trend(@PathVariable int period, @RequestBody List<List<String>> codes) {
        try {
            log.info("trend/{}",codes);
            Map<String, Trend> result = flatMapCode(codes, code -> trend(period, code).stream());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/trend/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Trend> trend(@PathVariable int period, @PathVariable String _code) {
        log.info("trend/{}",_code);
        return tryCatch(() -> {
            List<PriceAction> prices = mapList(prices(_code, period),PriceAction.class::cast);
            return Trend.calculateTrend(prices);
        }, ControllerMixIn.logAndReturn(Optional.empty()));
    }

    @Getter
    @AllArgsConstructor
    static class Trend2 {

        @JsonIgnore
        private Trend.Nested current;
        @JsonIgnore
        private Trend.Nested previous;

        @JsonProperty("l")
        public BigDecimal getNested() {
            return getFastToSlow(current);
        }

        @JsonProperty("f2s")
        public BigDecimal getFastToSlow() {
            return getFastToSlow(current);
        }

        @JsonProperty("r")
        public BigDecimal getRatio() {
            return divide(getFastToSlow(), getFastToSlow(previous));
        }

        private BigDecimal getFastToSlow(Trend.Nested nested) {
            return pctChangeOrZero(
                    _fast(nested).subtract(_long(nested)),
                    _slow(nested).subtract(_long(nested))
            ).setScale(3, RoundingMode.HALF_UP);
        }

        private BigDecimal _fast(Trend.Nested nested) {
            return nested.getFastPriceAction().getClose();
        }

        private BigDecimal _slow(Trend.Nested nested) {
            return nested.getSlowPriceAction().getClose();
        }

        private BigDecimal _long(Trend.Nested nested) {
            return nested.getLongPriceAction().getClose();
        }

    }

    public interface PFSL {
        @JsonProperty("p")
        BigDecimal getPrice();
        @JsonProperty("l")
        BigDecimal getLong();
        @JsonProperty("s")
        BigDecimal getSlow();
        @JsonProperty("f")
        BigDecimal getFast();

        static PFSL getFrom(Trend.Nested nested) {
            return new PFSL() {
                @Override
                public BigDecimal getPrice() {
                    return close(nested.getPriceAction());
                }

                @Override
                public BigDecimal getLong() {
                    return close(nested.getLongPriceAction());
                }

                @Override
                public BigDecimal getSlow() {
                    return close(nested.getSlowPriceAction());
                }

                @Override
                public BigDecimal getFast() {
                    return close(nested.getFastPriceAction());
                }

                private BigDecimal close(PriceAction priceAction) {
                    return priceAction.getClose().setScale(3, RoundingMode.HALF_UP);
                }
            };
        }
    }
}
