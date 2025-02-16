package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.MarketEnum;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.github.dfauth.ta.model.MarketEnum.ASX;

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

    @GetMapping("/position/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Position> position(@PathVariable String code) {
        return positionService.getPosition(code);
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

    @GetMapping("/positions/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> positionEvents(@PathVariable String code) {
        return positionService.getPositions(code);
    }

    @GetMapping("/position/sync")
    @ResponseStatus(HttpStatus.OK)
    public int sync() {
        return positionService.sync();
    }

    @GetMapping("/positions/all")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getAllPositions() {
        return getAllPositions(ASX);
    }

    @GetMapping("/positions/all/{market}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Position> getAllPositions(@PathVariable MarketEnum market) {
        return positionService.getAllPositions(market);
    }
}
