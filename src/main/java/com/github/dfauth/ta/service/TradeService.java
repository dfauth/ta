package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.repo.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Transactional
    public Integer sync(List<Trade> trades) {
        trades.stream()
                .map(t -> Optional.ofNullable(tradeRepository.findByConfirmationNo(t.getConfirmation_no()))
                        .map(_t -> {
                            _t.setCode(t.getCode());
                            _t.setDate(t.getDate());
                            _t.setSize(t.getSize());
                            _t.setPrice(t.getPrice());
                            _t.setCost(t.getCost());
                            _t.setSide(t.getSide());
                            _t.setNotes(t.getNotes());
                            return _t;
                        })
                        .orElse(t)
                )
                .forEach(tradeRepository::save);
        return trades.size();
    }
}
