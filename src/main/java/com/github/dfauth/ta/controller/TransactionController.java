package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Consecutive;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.Reduction;
import com.github.dfauth.ta.model.OpenPosition;
import com.github.dfauth.ta.model.PaymentCollectors;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.Side;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnType;
import com.github.dfauth.ta.repo.PaymentRepository;
import com.github.dfauth.ta.service.PositionService;
import com.github.dfauth.ta.service.TransactionService;
import com.github.dfauth.ta.util.ComparableWrapper;
import com.github.dfauth.ta.util.DateTimeUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.*;
import static com.github.dfauth.ta.functional.Reduction.SUM_PAYMENTS;
import static com.github.dfauth.ta.model.txn.TxnType.DIV;
import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;
import static com.github.dfauth.ta.util.DateTimeUtils.localDateComparator;
import static com.github.dfauth.ta.util.StreamOps.stream;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.util.function.Predicate.not;

@RestController
@Slf4j
public class TransactionController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionService transactionService;

    // used prioir to april 2025 when westpac changed their transaction detail format
    @PostMapping("/txns/sync/raw")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<Payment> txnSyncRaw(@RequestBody List<List<String>> txns) {

        txnSync(txns.stream().map(t -> {
            try {
                // 0 : account id - ignore
                // 1 = date 2024-10-10T16:00:00.000Z
                LocalDateTime date = LocalDateTime.parse(t.get(1), DateTimeUtils.spreadsheetDateTime);
                // 2 - narrative
                var detail = (String) t.get(2);
                // 3 - debit
                var debit = Optional.ofNullable(t.get(3)).filter(not(""::equals)).map(BigDecimal::new).orElse(ZERO);
                // 4 - credit
                var credit = Optional.ofNullable(t.get(4)).filter(not(""::equals)).map(BigDecimal::new).orElse(ZERO);
                // 5 - balance
                var balance = Optional.ofNullable(t.get(5)).filter(not(""::equals)).map(BigDecimal::new).orElse(ZERO);
                // 6 - category
                var txnType = TxnType.valueOf(t.get(6));
                // 7 - serial
                var contractNo = t.get(7);
                // 6 - code
                var code = Optional.ofNullable(t.get(8)).filter(not(""::equals)).orElse(null);
                // 7 - ex-dividend date
                var exDividendDate = Optional.ofNullable(t.get(9)).filter(not(""::equals)).map(str -> LocalDateTime.parse(str, DateTimeUtils.spreadsheetDateTime).toLocalDate()).orElse(null);
                return Payment.builder()
                        .txnType(txnType)
                        .date(date.toLocalDate())
                        .detail(detail)
                        .debit(debit)
                        .credit(credit)
                        .balance(balance)
                        .contractNo(contractNo)
                        .code(code)
                        .exDividendDate(exDividendDate)
                        .build().toPayment();
            } catch (RuntimeException e) {
                log.error("exception when processing transaction "+t+" exception message: "+e.getMessage(), e);
                throw e;
            }
        }).toList());
        return reconcile();
    }

    // used after april 2025 when westpac changed their transaction detail format
    @PostMapping("/txns/sync/raw2")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<Payment> txnSyncRaw2(@RequestBody List<List<String>> txns) {

        txnSync(txns.stream().map(t -> {
            try {
                // 0 : account id - ignore
                // 1 = date 2024-10-10T16:00:00.000Z
                LocalDate date = LocalDate.parse(t.get(1), DateTimeUtils.spreadsheetDateTime);
                // 2 - narrative
                var detail = (String) t.get(2);
                // 3 - debit
                var debit = Optional.ofNullable(t.get(3)).filter(not(""::equals)).map(BigDecimal::new).orElse(null);
                // 4 - credit
                var credit = Optional.ofNullable(t.get(4)).filter(not(""::equals)).map(BigDecimal::new).orElse(null);
                // 5 - balance
                var balance = Optional.ofNullable(t.get(5)).filter(not(""::equals)).map(BigDecimal::new).orElseThrow();
                // 6 - category
                var txnType = TxnType.valueOf(t.get(6));
                // 7 - SIDE
                var side = Side.fromString(t.get(7));
                // 6 - code
                var code = Optional.ofNullable(t.get(8)).filter(not(""::equals)).orElse(null);
                // 7 - ex-dividend date
                var exDividendDate = Optional.ofNullable(t.get(9)).filter(not(""::equals)).map(str -> LocalDateTime.parse(str, DateTimeUtils.spreadsheetDateTime).toLocalDate()).orElse(null);
                return new Payment(0l,
                        side.isDiv() ? TxnType.DIV : txnType,
                        date,
                        detail,
                        Optional.ofNullable(debit).orElse(credit),
                        balance,
                        side.map(s -> !s.isBuy() ? !s.isSell() ? null : s : s ),
                        code,
                        deriveContractNumber(side,detail),
                        exDividendDate);
            } catch (RuntimeException e) {
                log.error("exception when processing transaction "+t+" exception message: "+e.getMessage(), e);
                throw e;
            }
        }).toList());
        return reconcile();
    }

    private String deriveContractNumber(Side side, String detail) {
        return side.isInt() ? null : Optional.of(detail.split(" ")).map(arr -> arr[arr.length-1]).orElseThrow();
    }

    @PostMapping("/txns/sync")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void txnSync(@RequestBody List<Payment> txns) {
        try {
            txns.stream().forEach(pymnt -> {
                try {
                    Payment p = paymentRepository.findByDateAndValue(pymnt.getDate(), pymnt.getValue()).map(_p -> {
                        _p.setBalance(pymnt.getBalance());
                        _p.setCode(pymnt.getCode());
                        _p.setContractNo(pymnt.getContractNo());
                        _p.setDetail(pymnt.getDetail());
                        _p.setTxnType(pymnt.getTxnType());
                        _p.setSide(pymnt.getSide());
                        _p.setExDividendDate(pymnt.getExDividendDate());
                        return _p;
                    }).orElse(pymnt);
                    paymentRepository.save(p);
                } catch (Exception e) {
                    // log to get the affected record
                    log.error("cannot persist payment: "+pymnt+" error: "+e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/txns")
    @ResponseStatus(HttpStatus.OK)
    public Object txns(@RequestParam("collector") Optional<PaymentCollectors> collectorOpt) {
        return stream(transactionService.findAll()).collect(collectorOpt.map(PaymentCollectors::collector).orElse(PaymentCollectors.defaultCollector()));
    }

//    @GetMapping("/txns/asAt/{yyyyMMdd}")
//    @ResponseStatus(HttpStatus.OK)
    public Iterable<Payment> txnsAsAt(@PathVariable String yyyyMMdd) {
        LocalDate date = (LocalDate) YYYYMMDD.parse(yyyyMMdd);
        return transactionService.findByDate(date);
    }

    @GetMapping("/txns/asAt/{yyyyMMdd}")
    @ResponseStatus(HttpStatus.OK)
    public Object txnsAsAt(@PathVariable String yyyyMMdd, @RequestParam Optional<TxnType> txnType, @RequestParam Optional<Reduction> reduction) {
        LocalDate date = (LocalDate) YYYYMMDD.parse(yyyyMMdd);
        Predicate<Payment> p = txnType.map(Predicate.class::cast).orElse(ignore -> true);
        Collector<Payment,?,?> c = reduction.map(Reduction::get).map(Collector.class::cast).orElse(Collectors.toList());
        return txnsAsAt(date, p, c);
    }

    private <T> T txnsAsAt(LocalDate date, Predicate<Payment> p, Collector<Payment,?,T> collector) {
        return stream(transactionService.findByDate(date)).filter(p).collect(collector);
    }

    @GetMapping("/dividends/sum")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal sumOfDividends() {
        return (BigDecimal) txnsAsAt(LocalDate.now(),DIV,(Collector<Payment, ?, ? extends Object>) SUM_PAYMENTS.get());
    }

    @GetMapping("/txns/dividends/sum/{start}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> sumOfDividends(@PathVariable String start) {
        return sumOfDividends(start, YYYYMMDD.format(now()));
    }

    @GetMapping("/txns/dividends/sum/{start}/{end}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> sumOfDividends(@PathVariable String start, @PathVariable String end) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s, e, DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/dividends/{start}/{end}")
    @ResponseStatus(HttpStatus.OK)
    public List<Payment> dividends(@PathVariable String start, @PathVariable String end) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s, e, DIV);
    }

    @GetMapping("/dividends/year/{year}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> dividendsByYear(@PathVariable int year) {
        LocalDate start = LocalDate.of(2000, 12, 31);
        LocalDate end = LocalDate.of(year, 12, 31);
        return transactionService.transactionsByDateAndType(start, end, DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/{start}/{end}/{type}")
    @ResponseStatus(HttpStatus.OK)
    public List<Payment> transactionsByDateAndType(@PathVariable String start, @PathVariable String end, @PathVariable TxnType type) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s,e, type);
    }

    @GetMapping("/txns/sum/{start}/{end}/{type}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> sumOfTransactionsByDateAndType(@PathVariable String start, @PathVariable String end, @PathVariable TxnType type) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s,e,type).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/byType/{txnType}")
    @ResponseStatus(HttpStatus.OK)
    public List<Payment> txnsByType(@PathVariable TxnType txnType) {
        return transactionService.transactionsByType(txnType);
    }

    @GetMapping("/txns/byType/{txnType}/sum")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> sumOfTxnsByType(@PathVariable TxnType txnType) {
        return transactionService.transactionsByType(txnType).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/{code}")
    @ResponseStatus(HttpStatus.OK)
    public List<Payment> txnsByCode(@PathVariable String code, @RequestParam Optional<TxnType> txnType) {
        Predicate<Payment> p = txnType.map(Predicate.class::cast).orElse(ignore -> true);
        return stream(transactionService.findByCode(code)).filter(p).toList();
    }

    @GetMapping("/cash/asAt/{yyyyMMdd}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> cashAsAt(@PathVariable String yyyyMMdd) {
        LocalDate date = (LocalDate) YYYYMMDD.parse(yyyyMMdd);
        return transactionService.cashAsAt(date);
    }

    @GetMapping("/cash")
    @ResponseStatus(HttpStatus.OK)
    public Optional<BigDecimal> cash() {
        return transactionService.cashAsAt(now());
    }

    @GetMapping("/txns/reconcile")
    @ResponseStatus(HttpStatus.OK)
    public List<Payment> reconcile() {
        List<Payment> discrepencies = new ArrayList<>();
        return stream(transactionService.findAll())                                          // findAll
                .collect(toTreeMap(localDateComparator, aggregate(Payment::getDate)))        // aggregate to a map of lists sorted by date
                .values()
                .stream()
                .flatMap(l -> reorderSameDayPayments(l).stream())                           // reorder same day payments
                .collect(new Consecutive<>(discrepencies,                                   // collect by processing consecutive entities
                        (previousPayment, currentPayment) -> {
                            BigDecimal txnValue = currentPayment.getTxnType().apply(currentPayment.getValue());
                            var previousBalance = previousPayment.getBalance();
                            var currentBalance = currentPayment.getBalance();
                            var discrepency = previousBalance.add(txnValue).subtract(currentBalance);
                            if(discrepency.doubleValue() != 0) {
                                discrepencies.add(currentPayment);
                            }
                        })
                );
    }

    private List<Payment> reorderSameDayPayments(List<Payment> currentPayments) {
        if(currentPayments.size() ==1) {
            return currentPayments;
        }
        Payment last = currentPayments.stream()
                .filter(p -> currentPayments.stream()
                                .filter(p1 -> p.getBalance().equals(p1.getBalance().subtract(p1.getTxnType().apply(p1.getValue()))))
                                .findFirst()
                        .isEmpty())
                .findFirst().orElseThrow();// not found, must be the last
        var tmp = new ArrayList<>(currentPayments);
        tmp.remove(last);
        return Lists.add(reorderSameDayPayments(tmp), last);
    }

    @GetMapping("/txns/balance")
    @ResponseStatus(HttpStatus.OK)
    public Map<ComparableWrapper<LocalDate>, BigDecimal> balance() {
        return stream(transactionService.findAll())
                .map(p -> Map.entry(new ComparableWrapper<>(p.getDate(), LocalDate::compareTo), p.getBalance()))
                .map(e -> {
                    var d = e.getKey().getNested().minusDays(2);
                    var x = positionService.getPositionAsAt(d);
                    var y = stream(x)
                            .filter(Position::isOpen)
                            .map(OpenPosition.class::cast)
                            .map(OpenPosition::getMarketValue)
                            .reduce(BigDecimal::add)
                            .map(v -> v.add(e.getValue()))
                            .orElse(e.getValue());
                    return Map.entry(e.getKey(), y);
                })
                .collect(mapEntryMap(() -> new TreeMap<ComparableWrapper<LocalDate>, BigDecimal>(ComparableWrapper::compareTo)));
    }

    @Autowired
    private PositionService positionService;
}
