package com.github.dfauth.ta.service;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.model.CodeDateCompositeKey;
import com.github.dfauth.ta.model.Market;
import com.github.dfauth.ta.model.MarketEnum;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.repo.PositionRepository;
import com.github.dfauth.ta.repo.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PositionService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PositionRepository positionRepository;


    @Transactional
    public int sync() {
        List<Position> positions = tradeRepository.derivePositions();
        log.info("processing {} positions", positions.size());
        return positions
                .stream()
                .map(p -> {
                    log.info("processing {}",p);
                    return positionRepository.findById(new CodeDateCompositeKey(p.getCode(), p.getDate()))
                            .map(_p -> {
                                _p.setSize(p.getSize());
                                _p.setCost(p.getCost());
                                _p.setCommission(p.getCommission());
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
        return positionRepository.findAll();
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

    public Optional<Position> getPosition(String code) {
        return positionRepository.findByCode(code).stream().findFirst();
    }

    public Optional<Position> getPosition(String code, Timestamp date) {
        return positionRepository.findPositionByCodeAndDate(code, date);
    }

    public Iterable<Position> getAllPositions(MarketEnum market) {
        return positionRepository.findAllNonZeroPositions();
    }

    public Iterable<Position> getPositions(String code) {
        return positionRepository.findByCode(code);
    }
}
