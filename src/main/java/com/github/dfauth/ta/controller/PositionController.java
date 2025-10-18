package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.*;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.service.PositionService;
import com.github.dfauth.ta.util.ComparableWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

import static com.github.dfauth.ta.controller.MetricsController.Mode.ALL;
import static com.github.dfauth.ta.functional.Predicates.alwaysTrue;
import static com.github.dfauth.ta.model.MarketEnum.ASX;
import static com.github.dfauth.ta.model.PositionCollectors.sortedDoubleKeyedMapCollector;
import static com.github.dfauth.ta.model.PositionDatePredicate.*;
import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;
import static com.github.dfauth.ta.util.StreamOps.stream;
import static java.util.function.Predicate.not;

@RestController
@Slf4j
@RequestMapping("position")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Object positions(@RequestParam("startBefore") Optional<String> startBefore,
                                        @RequestParam("startAfter") Optional<String> startAfter,
                                        @RequestParam("endBefore") Optional<String> endBefore,
                                        @RequestParam("endAfter") Optional<String> endAfter,
                                        @RequestParam("mode") Optional<MetricsController.Mode> mode,
                                        @RequestParam("theme") Optional<String> theme,
                                        @RequestParam("excludeCodes") Optional<String> excludeCodes,
                                        @RequestParam("includeCodes") Optional<String> includeCodes,
                                        @RequestParam("collector") Optional<PositionCollectors> collector) {
        Predicate<Position> startDatePredicate = startBefore.map(START_BEFORE).orElse(ignore()).and(startAfter.map(START_AFTER).orElse(ignore()));
        Predicate<Position> endDatePredicate = endBefore.map(END_BEFORE).orElse(ignore()).and(endAfter.map(END_AFTER).orElse(ignore()));
        Predicate<com.github.dfauth.ta.model.Position> modePredicate = mode.orElse(ALL);
        Predicate<com.github.dfauth.ta.model.Position> themePredicate = theme.map(Theme::fromString).orElse(alwaysTrue());
        Predicate<Position> excludeCodesPredicate = excludeCodes.map(str -> (Predicate<Position>)(p -> !Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());
        Predicate<Position> includeCodesPredicate = includeCodes.map(str -> (Predicate<Position>)(p -> Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());

        return stream(positionService.findAll())
                .filter(startDatePredicate
                        .and(endDatePredicate)
                        .and(modePredicate)
                        .and(themePredicate)
                        .and(excludeCodesPredicate)
                        .and(includeCodesPredicate))
                .collect(collector.map(PositionCollectors::toCollector).orElse(PositionCollectors.defaultCollector()));
    }

    @GetMapping("/sort")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Map<ComparableWrapper<LocalDate>,Position>> sortedPositions() {
        return sortedPositions(Position::getCode, p -> new ComparableWrapper<>(p.getOpen(), LocalDate::compareTo));
    }

    @GetMapping("/sort/byDate")
    @ResponseStatus(HttpStatus.OK)
    public Object sortedPositionsByDate(@RequestParam("startBefore") Optional<String> startBefore,
                                        @RequestParam("startAfter") Optional<String> startAfter,
                                        @RequestParam("endBefore") Optional<String> endBefore,
                                        @RequestParam("endAfter") Optional<String> endAfter,
                                        @RequestParam("mode") Optional<MetricsController.Mode> mode,
                                        @RequestParam("theme") Optional<String> theme,
                                        @RequestParam("collector") Optional<PositionCollectors> collector) {
        Predicate<Position> startDatePredicate = startBefore.map(START_BEFORE).orElse(ignore()).and(startAfter.map(START_AFTER).orElse(ignore()));
        Predicate<Position> endDatePredicate = endBefore.map(END_BEFORE).orElse(ignore()).and(endAfter.map(END_AFTER).orElse(ignore()));
        Predicate<com.github.dfauth.ta.model.Position> modePredicate = mode.orElse(ALL);
        Predicate<com.github.dfauth.ta.model.Position> themePredicate = theme.map(Theme::fromString).orElse(alwaysTrue());

        Collector<Position, Object, Object> d = PositionCollectors.defaultCollector();
        return stream(positionService.findAll())
                .filter(startDatePredicate
                        .and(endDatePredicate)
                        .and(modePredicate)
                        .and(themePredicate))
                .collect(collector.map(PositionCollectors::toCollector).orElse(d));
    }

    private <K extends Comparable<K>, L extends Comparable<L>> Map<K,Map<L,Position>> sortedPositions(Function<Position, K> primaryKeyMapper,
                                                                                                      Function<Position, L> secondaryKeyMapper) {
        return stream(positionService.findAll())
                .collect(sortedDoubleKeyedMapCollector(primaryKeyMapper, secondaryKeyMapper));
    }

    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, PositionSummary> positionSummary(@RequestParam("theme") Optional<Theme> theme) {
        return theme.map(t -> positionService.findPositionSummaries(t)).orElseGet(() -> positionService.findPositionSummaries());
    }

    @GetMapping("/summary/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<PositionSummary> positionSummary(@PathVariable String code, @RequestParam("theme") Optional<Theme> theme) {
        return theme.map(t -> positionService.findPositionSummaries(code, t)).orElse(positionService.findPositionSummaries(code));
    }

    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> position(@PathVariable String code) {
        return stream(positionService.getPositions(code)).map(Position.class::cast).toList();
    }

    @GetMapping("/{code}/{date}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Position> position(@PathVariable String code,@PathVariable String date) {
        return position(code,date, ASX);
    }

    @GetMapping("/{code}/{date}/{market}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Position> position(@PathVariable String code,@PathVariable String date,@PathVariable MarketEnum market) {
        ZonedDateTime ts = market.atCloseOn(date);
        return positionService.getPosition(code,new Timestamp(ts.toInstant().toEpochMilli())).map(Position.class::cast);
    }

    @GetMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public int sync() {
        return positionService.sync();
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getAllPositions() {
        return getAllPositions(ASX);
    }

    @GetMapping("/all/{market}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getAllPositions(@PathVariable MarketEnum market) {
        return stream(positionService.getAllPositions(market)).map(Position.class::cast).toList();
    }

    @GetMapping("/asAt/{yyyyMMdd}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getPositionAsAt(@PathVariable String yyyyMMdd) {
        LocalDate date = (LocalDate) YYYYMMDD.parse(yyyyMMdd);
        return stream(positionService.getPositionAsAt(date)).map(Position.class::cast).toList();
    }

    @GetMapping("/{code}/asAt/{yyyyMMdd}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Position> getPositionCodeAsAt(@PathVariable String code, @PathVariable String yyyyMMdd) {
        LocalDate date = (LocalDate) YYYYMMDD.parse(yyyyMMdd);
        return positionService.getPositionAsAt(code, date).map(Position.class::cast);
    }

    @GetMapping("/unassigned/payments")
    @ResponseStatus(HttpStatus.OK)
    public Set<Payment> unAssignedPayments() {
        return positionService.findUnAssignedPayments();
    }

    @GetMapping("/find/payments/{code}")
    @ResponseStatus(HttpStatus.OK)
    public List<Payment> findPayments(@PathVariable String code) {
        return stream(positionService.getPositions(code)).flatMap(p -> positionService.findPaymentsByPosition(p).stream()).toList();
    }

    @GetMapping("/unreconciled")
    @ResponseStatus(HttpStatus.OK)
    public List<Position> reconcilePositions() {
        return stream(positionService.findAll()).filter(not(com.github.dfauth.ta.repo.Position::isReconciled)).map(Position.class::cast).toList();
    }

    @GetMapping("/reconcile/{code}")
    @ResponseStatus(HttpStatus.OK)
    public List<Position> reconcilePosition(@PathVariable String code) {
        return stream(positionService.getPositions(code)).filter(not(com.github.dfauth.ta.repo.Position::isReconciled)).map(Position.class::cast).toList();
    }
}
