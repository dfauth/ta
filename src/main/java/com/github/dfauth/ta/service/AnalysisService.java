package com.github.dfauth.ta.service;

import com.github.dfauth.ta.functional.Maps;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.Theme;
import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.model.TradingMetrics;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.repo.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BinaryOperator;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.model.TradingMetrics.closedTradingMetrics;
import static com.github.dfauth.ta.model.TradingMetrics.openTradingMetrics;
import static com.github.dfauth.ta.util.StreamOps.stream;

@Slf4j
@Service
public class AnalysisService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PriceRepository priceRepository;

    public Optional<TradingMetrics> analyse() {
        return analyse(Mode.ALL);
    }

    public Optional<TradingMetrics> analyseByDate(LocalDate start, LocalDate end) {
        return analyse(Mode.ALL, start, end);
    }

    public Optional<TradingMetrics> analyse(Mode mode) {
        Iterable<Trade> trades = tradeRepository.findAll();
        return analyse(trades, mode);
    }

    public Optional<TradingMetrics> analyse(Mode mode, LocalDate start, LocalDate end) {
        Iterable<Trade> trades = tradeRepository.findAllByDate(start, end);
        return analyse(trades, mode);
    }

    public Optional<TradingMetrics> analyseByTheme(Theme theme) {
        return analyseByTheme(theme, Mode.ALL);
    }

    public Optional<TradingMetrics> analyseByTheme(Theme theme, Mode mode) {
        List<Trade> trades = tradeRepository.findByTheme(theme);
        return analyse(trades, mode);
    }

    public Optional<TradingMetrics> analyse(Iterable<Trade> trades) {
        return analyse(trades, Mode.ALL);
    }

    public Optional<TradingMetrics> analyse(Iterable<Trade> trades, Mode mode) {
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
        // aggregate individual trades into positions
        Map<String, TradingMetrics.TradeAccumulator> positionMap = Maps.mapValues(tradeMap, TradingMetrics::aggregate);

        Optional<TradingMetrics> openTradingMetrics = openTradingMetrics(positionMap, code -> priceRepository.findLatestByCode(code).map(Price::getClose).orElseThrow());
        Optional<TradingMetrics> closedTradingMetrics = closedTradingMetrics(positionMap);
        return mode.apply(openTradingMetrics, closedTradingMetrics);
    }

    public enum Mode implements BinaryOperator<Optional<TradingMetrics>> {
        OPEN((o,c) -> o),
        CLOSE((o,c) -> c),
        ALL((o,c) -> o.flatMap(_o -> c.map(_o::merge)));

        private BinaryOperator<Optional<TradingMetrics>> fn;

        Mode(BinaryOperator<Optional<TradingMetrics>> fn) {
            this.fn = fn;
        }

        @Override
        public Optional<TradingMetrics> apply(Optional<TradingMetrics> open, Optional<TradingMetrics> close) {
            return fn.apply(open, close);
        }
    }
}
