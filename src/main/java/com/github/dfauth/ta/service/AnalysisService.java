package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.Theme;
import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.model.TradingMetrics;
import com.github.dfauth.ta.repo.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.util.StreamOps.stream;

@Slf4j
@Service
public class AnalysisService {

    @Autowired
    private TradeRepository tradeRepository;

    public Optional<TradingMetrics> analyse() {
        Iterable<Trade> trades = tradeRepository.findAll();
        return analyse(trades);
    }

    public Optional<TradingMetrics> analyseByTheme(Theme theme) {
        List<Trade> trades = tradeRepository.findByTheme(theme);
        return analyse(trades);
    }

    public Optional<TradingMetrics> analyse(Iterable<Trade> trades) {
        HashMap<String, List<Trade>> tradeMap = stream(trades.iterator()).reduce(new HashMap<>(), (a, t) -> {
            a.compute(t.getCode(), (k,v) -> Optional.ofNullable(v)
                    .map(l -> {
                        List<Trade> tmp = new ArrayList<>(v);
                        tmp.add(t);
                        return tmp;
                    })
                    .orElse(List.of(t)));
            return a;
        }, oops());
        return TradingMetrics.calculateTradingMetrics(tradeMap);
    }
}
