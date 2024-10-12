package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.CodeDateCompositeKey;
import com.github.dfauth.ta.model.MarketEnum;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.repo.PositionRepository;
import com.github.dfauth.ta.repo.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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
        return positionRepository.findNonZeroPositions();
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
