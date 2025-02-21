package com.github.dfauth.ta.service;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.model.*;
import com.github.dfauth.ta.repo.PositionRepository;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.repo.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.util.StreamOps.stream;

@Slf4j
@Service
public class PositionService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PriceRepository priceRepository;


    public int sync() {
//        List<Position> positions = tradeRepository.derivePositions();
        // greoup trades by code, ordered by date
        Map<String, List<Trade>> tradeByCode = stream(tradeRepository.findAllByDate()).collect(Collectors.groupingBy(Trade::getCode, Collectors.toList()));

        // for each code, reduce to a series of positions
        List<Position> positions = tradeByCode.entrySet().stream().map(e -> {
            List<Position> tmp = new ArrayList<>();
            Position last = e.getValue().stream().reduce(new Position(), (p, t) -> {
                var p1 = p.onTrade(t);
                if (p1.isClosed()) {
                    tmp.add(p1);
                    return new Position();
                } else {
                    return p1;
                }
            }, oops());
            // add open positions
            if(last.isOpen()) {
                tmp.add(last);
            }
            return tmp;
        }).flatMap(List::stream).collect(Collectors.toList());
        log.info("processing {} positions", positions.size());
        return save(positions);
    }

    @Transactional
    public int save(List<Position> positions) {
        return positions
                .stream()
                .map(p -> {
                    log.info("processing {}",p);
                    return positionRepository.findById(new CodeDateCompositeKey(p.getCode(), p.getDate()))
                            .map(_p -> {
                                _p.setLast(p.getLast());
                                _p.setUnitsPurchased(p.getUnitsPurchased());
                                _p.setUnitsSold(p.getUnitsSold());
                                _p.setWeightedHoldingTime(p.getWeightedHoldingTime());
                                _p.setPurchaseValue(p.getPurchaseValue());
                                _p.setSaleValue(p.getSaleValue());
                                _p.setCommission(p.getCommission());
                                _p.setTrades(p.getTrades());
                                return _p;
                            }).orElse(p);
                })
                .map(p -> {
                    log.info("saving position {}",p);
                    return positionRepository.save(p);
                })
                .toList().size();
    }

    public Iterable<Position> findAll() {
        return openPositionStream(positionRepository.findAll()).toList();
    }

    public Map<String,PositionSummary> findPositionSummaries() {
        return findPositionSummaries(openPositionStream(positionRepository.findAll()));
    }

    public Map<String,PositionSummary> findPositionSummaries(String code) {
        return findPositionSummaries(openPositionStream(positionRepository.findByCode(code)));
    }

    private Map<String,PositionSummary> findPositionSummaries(Stream<Position> stream) {
        return stream
                .collect(Collectors.groupingBy(Position::getCode))
                .entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().reduce(new PositionSummary(e.getKey(), priceRepository.findLatestByCode(e.getKey()).orElse(null)), PositionSummary::add, oops())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Iterable<Position> findOpenPositions() {
        return findOpenPositions(LocalDate.now());
    }

    public Iterable<Position> findOpenPositions(LocalDate date) {
        return findOpenPositions(date, MarketEnum.ASX);
    }

    public Iterable<Position> findOpenPositions(LocalDate date, Market market) {
        Instant i = market.atMarketCloseOnOrPriorTo(date);
        Map<String, Position> map = Lists.toMap1(positionRepository.findAllPriorTo(new Timestamp(i.toEpochMilli())), Position::getCode, p -> (k, v) -> Optional.ofNullable(v)
                .map(prev -> prev.later(p))
                .orElse(p));
        // filter non zero
        return map.values().stream().filter(p -> p.getSize() > 0).collect(Collectors.toList());
    }

    public Optional<Position> getPosition(String code, Timestamp date) {
        return positionRepository.findPositionByCodeAndDate(code, date);
    }

    public Iterable<Position> getAllPositions(MarketEnum market) {
        return positionRepository.findAllNonZeroPositions();
    }

    public Iterable<Position> getPositions(String code) {
        return openPositionStream(positionRepository.findByCode(code)).toList();
    }

    private Stream<Position> openPositionStream(Iterable<Position> iterable) {
        return openPositionStream(stream(iterable.spliterator()));
    }

    private Stream<Position> openPositionStream(Stream<Position> stream) {
        return stream.map(p -> p.isOpen() ? priceRepository.findLatestByCode(p.getCode()).<Position>map(_p -> new OpenPosition(p,_p)).orElse(p) : p);
    }

    public Iterable<Position> findAllSince(LocalDate date) {
        return openPositionStream(stream(positionRepository.findStartingOnOrAfter(date))).toList();
    }

    public Iterable<Position> findAllBetween(LocalDate from, LocalDate to) {
        return openPositionStream(stream(positionRepository.findBetween(from,to))).toList();
    }
}
