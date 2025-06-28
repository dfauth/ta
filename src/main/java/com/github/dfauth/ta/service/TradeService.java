package com.github.dfauth.ta.service;

import com.github.dfauth.ta.functional.Maps;
import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.repo.PaymentRepository;
import com.github.dfauth.ta.repo.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functional.Collectors.toMap;
import static com.github.dfauth.ta.functional.Maps.maps;
import static com.github.dfauth.ta.model.txn.TxnType.DEP;
import static com.github.dfauth.ta.model.txn.TxnType.PAYMENT;
import static com.github.dfauth.ta.util.StreamOps.stream;
import static java.math.BigDecimal.ZERO;
import static java.util.function.Predicate.not;

@Slf4j
@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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
                            _t.setTheme(t.getTheme());
                            return _t;
                        })
                        .orElse(t)
                )
                .forEach(tradeRepository::save);
        return trades.size();
    }

    public Iterable<Trade> findAll() {
        return tradeRepository.findAll();
    }

    public Iterable<Trade> findByCode(String code) {
        return tradeRepository.findByCode(code);
    }

    public Maps<LocalDate, BigDecimal> findTradesPendingPayment() {
        Function<Trade, BiFunction<LocalDate, BigDecimal, BigDecimal>> reMapper1 = t -> (d, v) -> Optional.ofNullable(v).map(t.getValue()::add).orElse(t.getValue());
        Map<LocalDate, BigDecimal> tradeMap = stream(findAll()).collect(toMap(new TreeMap<>(), Trade::getLocalDate, reMapper1));
        TreeMap<LocalDate, BigDecimal> tmp = new TreeMap<>();
        TreeMap<LocalDate, BigDecimal> balanceMap = tradeMap.entrySet().stream().reduce(tmp, updateBalance(), oops());
        Function<Payment, BiFunction<LocalDate, BigDecimal, BigDecimal>> reMapper2 = p -> (d, v) -> Optional.ofNullable(v).map(p.getValue()::add).orElse(p.getValue());
        Map<LocalDate, BigDecimal> paymentMap = stream(paymentRepository.findByType(PAYMENT)).collect(toMap(new TreeMap<>(), Payment::getDate, reMapper2));
        paymentMap.entrySet().stream().reduce(balanceMap, updateBalance(BigDecimal::subtract), oops());
        Map<LocalDate, BigDecimal> depositMap = stream(paymentRepository.findByType(DEP)).collect(toMap(new TreeMap<>(), Payment::getDate, reMapper2));
        depositMap.entrySet().stream().reduce(balanceMap, updateBalance(), oops());
        //filter zero entries
        return maps(balanceMap).filterValue(not(ZERO::equals));
    }

    private BiFunction<TreeMap<LocalDate, BigDecimal>, ? super Map.Entry<LocalDate, BigDecimal>, TreeMap<LocalDate, BigDecimal>> updateBalance() {
        return updateBalance(BigDecimal::add);
    }

    private BiFunction<TreeMap<LocalDate, BigDecimal>, ? super Map.Entry<LocalDate, BigDecimal>, TreeMap<LocalDate, BigDecimal>> updateBalance(BinaryOperator<BigDecimal> f2) {
        return (m, e) -> {
            Map.Entry<LocalDate, BigDecimal> current;
            if ((current = m.floorEntry(e.getKey())) == null) {
                m.put(e.getKey(), e.getValue());
            } else {
                m.put(e.getKey(), f2.apply(current.getValue(), e.getValue()));
            }
            return m;
        };
    }

    public static <T> Collector<T, ?, BigDecimal> reducingCollector(Function<T,BigDecimal> f) {
        return Collectors.reducing(ZERO, f, BigDecimal::add);
    }
}
