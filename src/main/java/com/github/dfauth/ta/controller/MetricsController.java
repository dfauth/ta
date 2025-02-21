package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.TradingMetrics;
import com.github.dfauth.ta.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.function.Predicate;

import static com.github.dfauth.ta.controller.MetricsController.Mode.ALL;
import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;
import static com.github.dfauth.ta.util.StreamOps.stream;

@RestController
@Slf4j
public class MetricsController {

    @Autowired
    private PositionService positionService;

    @GetMapping("/metrics")
    @ResponseStatus(HttpStatus.OK)
    public TradingMetrics metrics() {
        return metrics(ALL);
    }

    @GetMapping("/metrics/mode/{mode}")
    @ResponseStatus(HttpStatus.OK)
    public TradingMetrics metrics(@PathVariable Mode mode) {
        return stream(positionService.findAll()).filter(mode::test).reduce(new TradingMetrics(), TradingMetrics::add, TradingMetrics::add);
    }

    @GetMapping("/metrics/since/{yyyyMMdd}")
    @ResponseStatus(HttpStatus.OK)
    public TradingMetrics metricsSince(@PathVariable String yyyyMMdd) {
        return stream(positionService.findAllSince((LocalDate) YYYYMMDD.parse(yyyyMMdd))).reduce(new TradingMetrics(), TradingMetrics::add, TradingMetrics::add);
    }

    @GetMapping("/metrics/between/{start}/{end}")
    @ResponseStatus(HttpStatus.OK)
    public TradingMetrics metricsBetween(@PathVariable String start, @PathVariable String end) {
        var from = (LocalDate) YYYYMMDD.parse(start);
        var to = (LocalDate) YYYYMMDD.parse(end);
        return stream(positionService.findAllBetween(from, to)).reduce(new TradingMetrics(), TradingMetrics::add, TradingMetrics::add);
    }

    enum Mode implements Predicate<Position> {
        OPEN(Position::isOpen), CLOSE(Position::isClosed), ALL(ignore -> true);

        private Predicate<Position> nested;

        Mode(Predicate<Position> nested) {
            this.nested = nested;
        }

        public boolean test(Position p) {
            return nested.test(p);
        }
    }
}
