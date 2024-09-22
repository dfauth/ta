package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.repo.PositionRepository;
import com.github.dfauth.ta.repo.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
                    return positionRepository.findPositionByCode(p.getCode()).map(_p -> {
                                _p.setSize(p.getSize());
                                _p.setCost(p.getCost());
                                log.info("found position {}",_p);
                                return _p;
                            })
                            .orElseGet(() -> {
                                log.info("using default {}",p);
                                return p;
                            });
                })
                .map(p -> {
                    log.info("saving position {}",p);
                    return positionRepository.save(p);
                })
                .collect(Collectors.toList()).size();
    }

    public Iterable<Position> findAll() {
        return positionRepository.findAll();
    }
}
