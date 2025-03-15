package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnType;
import com.github.dfauth.ta.repo.PaymentRepository;
import com.github.dfauth.ta.service.TransactionService;
import com.github.dfauth.ta.util.DateTimeUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;
import static java.util.function.Predicate.not;

@RestController
@Slf4j
public class TransactionController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/txns/sync/raw")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void txnSyncRaw(@RequestBody List<List<String>> txns) {

        txnSync(txns.stream().map(t -> {
            try {
                // 0 : account id - ignore
                // 1 = date 2024-10-10T16:00:00.000Z
                LocalDateTime date = LocalDateTime.parse(t.get(1), DateTimeUtils.spreadsheetDateTime);
                // 2 - narrative
                var detail = (String) t.get(2);
                // 3 - debit
                var debit = Optional.ofNullable(t.get(3)).filter(not(""::equals)).map(BigDecimal::new).orElse(BigDecimal.ZERO);
                // 4 - credit
                var credit = Optional.ofNullable(t.get(4)).filter(not(""::equals)).map(BigDecimal::new).orElse(BigDecimal.ZERO);
                // 5 - balance
                var balance = Optional.ofNullable(t.get(5)).filter(not(""::equals)).map(BigDecimal::new).orElse(BigDecimal.ZERO);
                // 6 - category
                var txnType = TxnType.valueOf(t.get(6));
                // 7 - serial
                var contractNo = t.get(7);
                // 6 - code
                var code = Optional.ofNullable(t.get(8)).filter(not(""::equals)).orElse(null);
                return Payment.builder()
                        .txnType(txnType)
                        .date(date.toLocalDate())
                        .detail(detail)
                        .debit(debit)
                        .credit(credit)
                        .balance(balance)
                        .contractNo(contractNo)
                        .code(code)
                        .build().toPayment();
            } catch (RuntimeException e) {
                log.error("exception when processing transaction "+t+" exception message: "+e.getMessage(), e);
                throw e;
            }
        }).toList());
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

    @GetMapping("/txns/dividends/sum")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Optional<BigDecimal> sumOfDividends() {
        return transactionService.transactionsByDateAndType(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()), LocalDate.now(), TxnType.DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/dividends/sum/{start}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Optional<BigDecimal> sumOfDividends(@PathVariable String start) {
        return sumOfDividends(start, YYYYMMDD.format(LocalDate.now()));
    }

    @GetMapping("/txns/dividends/sum/{start}/{end}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Optional<BigDecimal> sumOfDividends(@PathVariable String start, @PathVariable String end) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s, e, TxnType.DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/dividends/{start}/{end}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<Payment> dividends(@PathVariable String start, @PathVariable String end) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s, e, TxnType.DIV);
    }

    @GetMapping("/dividends/year/{year}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Optional<BigDecimal> dividendsByYear(@PathVariable int year) {
        LocalDate start = LocalDate.of(2000, 12, 31);
        LocalDate end = LocalDate.of(year, 12, 31);
        return transactionService.transactionsByDateAndType(start, end, TxnType.DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/{start}/{end}/{type}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<Payment> transactionsByDateAndType(@PathVariable String start, @PathVariable String end, @PathVariable TxnType type) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s,e, type);
    }

    @GetMapping("/txns/sum/{start}/{end}/{type}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Optional<BigDecimal> sumOfTransactionsByDateAndType(@PathVariable String start, @PathVariable String end, @PathVariable TxnType type) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s,e,type).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }
}
