package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.MarketEnum;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.PositionSummary;
import com.github.dfauth.ta.model.Theme;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.dfauth.ta.model.MarketEnum.ASX;
import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;
import static com.github.dfauth.ta.util.StreamOps.stream;
import static java.util.function.Predicate.not;

@RestController
@Slf4j
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping("/positions")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> positionValuation() {
        return positionService.findAll();
    }

    @GetMapping("/positions/summary")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, PositionSummary> positionSummary(@RequestParam("theme") Optional<Theme> theme) {
        return theme.map(t -> positionService.findPositionSummaries(t)).orElseGet(() -> positionService.findPositionSummaries());
    }

    @GetMapping("/position/summary/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<PositionSummary> positionSummary(@PathVariable String code, @RequestParam("theme") Optional<Theme> theme) {
        return theme.map(t -> positionService.findPositionSummaries(code, t)).orElse(positionService.findPositionSummaries(code));
    }

    @GetMapping("/position/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> position(@PathVariable String code) {
        return positionService.getPositions(code);
    }

    @GetMapping("/position/{code}/{date}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Position> position(@PathVariable String code,@PathVariable String date) {
        return position(code,date, ASX);
    }

    @GetMapping("/position/{code}/{date}/{market}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Position> position(@PathVariable String code,@PathVariable String date,@PathVariable MarketEnum market) {
        ZonedDateTime ts = market.atCloseOn(date);
        return positionService.getPosition(code,new Timestamp(ts.toInstant().toEpochMilli()));
    }

    @GetMapping("/position/sync")
    @ResponseStatus(HttpStatus.OK)
    public int sync() {
        return positionService.sync();
    }

    @GetMapping("/position/all")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getAllPositions() {
        return getAllPositions(ASX);
    }

    @GetMapping("/position/all/{market}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getAllPositions(@PathVariable MarketEnum market) {
        return positionService.getAllPositions(market);
    }

    @GetMapping("/positions/asAt/{yyyyMMdd}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getPositionAsAt(@PathVariable String yyyyMMdd) {
        LocalDate date = (LocalDate) YYYYMMDD.parse(yyyyMMdd);
        return positionService.getPositionAsAt(date);
    }

    @GetMapping("/position/{code}/asAt/{yyyyMMdd}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Position> getPositionCodeAsAt(@PathVariable String code, @PathVariable String yyyyMMdd) {
        LocalDate date = (LocalDate) YYYYMMDD.parse(yyyyMMdd);
        return positionService.getPositionAsAt(code, date);
    }

    @GetMapping("/position/unassigned/payments")
    @ResponseStatus(HttpStatus.OK)
    public Set<Payment> unAssignedPayments() {
        return positionService.findUnAssignedPayments();
    }

    @GetMapping("/position/find/payments/{code}")
    @ResponseStatus(HttpStatus.OK)
    public List<Payment> findPayments(@PathVariable String code) {
        return stream(positionService.getPositions(code)).flatMap(p -> positionService.findPaymentsByPosition(p).stream()).toList();
    }

    @GetMapping("/positions/unreconciled")
    @ResponseStatus(HttpStatus.OK)
    public List<Position> reconcilePositions() {
        return stream(positionService.findAll()).filter(not(Position::isReconciled)).toList();
    }

    @GetMapping("/position/reconcile/{code}")
    @ResponseStatus(HttpStatus.OK)
    public List<Position> reconcilePosition(@PathVariable String code) {
        return stream(positionService.getPositions(code)).filter(not(Position::isReconciled)).toList();
    }
}
