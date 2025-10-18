package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.*;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.repo.Position;
import com.github.dfauth.ta.repo.*;
import com.github.dfauth.ta.util.PositionCollector;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.model.txn.TxnType.DIV;
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

    @Autowired
    private PaymentRepository paymentRepository;


    public int sync() {

        // group trades by code, ordered by date
        List<Position> positions = stream(tradeRepository.findAllByDate())
                .collect(Collectors.groupingBy(Trade::getCode, new PositionCollector()))
                .values().stream()
                .flatMap(List::stream)
                .toList();

        log.info("processing {} positions", positions.size());
        return save(positions);
    }

    @Transactional
    public int save(List<Position> positions) {
        return positions
                .stream()
                .map(p -> {
                    log.info("processing {}",p);
                    // lookup payments
                    List<Payment> payments = p.isOpen() ? paymentRepository.findByOpenPosition(p) : paymentRepository.findByClosedPosition(p);
                    p.setPayments(payments);
                    return positionRepository.findById(new CodeDateCompositeKey(p.getCode(), p.getDate()))
                            .map(_p -> {
                                _p.setLast(p.getLastTimestamp());
                                _p.setUnitsPurchased(p.getUnitsPurchased());
                                _p.setUnitsSold(p.getUnitsSold());
                                _p.setUnitsHoldingDays(p.getUnitsHoldingDays());
                                _p.setPurchaseValue(p.getPurchaseValue());
                                _p.setSaleValue(p.getSaleValue());
                                _p.setCommission(p.getCommission());
                                _p.setTrades(p.getTrades());
                                _p.setPayments(p.getPayments());
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

    public Map<String,PositionSummary> findPositionSummaries(Theme theme) {
        return findPositionSummaries(openPositionStream(positionRepository.findAll()).filter(theme));
    }

    public Optional<PositionSummary> findPositionSummaries(String code) {
        return findPositionSummaries(openPositionStream(positionRepository.findByCode(code))).values().stream().findFirst();
    }

    public Optional<PositionSummary> findPositionSummaries(String code, Theme theme) {
        return findPositionSummaries(openPositionStream(positionRepository.findByCode(code).stream().filter(theme))).values().stream().findFirst();
    }

    private Map<String,PositionSummary> findPositionSummaries(Stream<Position> stream) {
        return stream
                .collect(Collectors.groupingBy(Position::getCode))
                .entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().reduce(new PositionSummary(), PositionSummary::add, oops())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

    private UnaryOperator<Stream<Position>> openPositionStream(LocalDate date) {
        return stream -> stream.map(p -> p.isOpenAt(date) ?
                priceRepository.findByCodeAndDate(p.getCode(), date).
                        <Position>map(_p -> new OpenPosition(p,_p))
                        .orElse(p) :
                p);
    }

    public Iterable<Position> findAllSince(LocalDate date) {
        return openPositionStream(stream(positionRepository.findStartingOnOrAfter(date))).toList();
    }

    public Iterable<Position> findAllBetween(LocalDate from, LocalDate to) {
        return openPositionStream(stream(positionRepository.findBetween(from,to))).toList();
    }

    public Iterable<Position> getPositionAsAt(LocalDate date) {
        return openPositionStream(date).apply(stream(positionRepository.findAllPriorTo(date)).map(p -> p.positionAsAt(date))).toList();
    }

    public Optional<Position> getPositionAsAt(String code, LocalDate date) {
        return openPositionStream(date).apply(positionRepository.findPositionByCodeAndDate(code, date).stream()).findFirst();
    }

    public Set<Payment> findUnAssignedPayments() {
        var tmp = stream(positionRepository.findAll()).flatMap(p -> p.getPayments().stream()).toList();
        var tmp1 = stream(paymentRepository.findAll()).filter(DIV).toList();
        var s1 = new HashSet<>(tmp1);
        s1.removeAll(tmp);
        return s1;
    }

    public List<Payment> findPaymentsByPosition(Position p) {
        return p.isOpen() ? paymentRepository.findByOpenPosition(p) : paymentRepository.findByClosedPosition(p);
    }
}
