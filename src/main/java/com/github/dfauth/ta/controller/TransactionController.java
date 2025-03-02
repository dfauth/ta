package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnEntry;
import com.github.dfauth.ta.repo.TransactionRepository;
import com.github.dfauth.ta.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;

@RestController
@Slf4j
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/txns/sync")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void txnSync(@RequestBody List<Payment> txns) {
        try {
            List<Payment> reconsituted = txns.stream().map(pymnt -> pymnt.getTxnType().<Payment>blah(pymnt)).collect(Collectors.toList());
            log.info("txns/sync: {}",reconsituted);
//            transactionRepository.saveAll(txns);
            txns.stream().forEach(pymnt -> {
                try {
                    Payment p = transactionRepository.findByDateAndValue(pymnt.getDate(), pymnt.getValue()).map(_p -> {
                        _p.setBalance(pymnt.getBalance());
                        _p.setCode(pymnt.getCode());
                        _p.setContractNo(pymnt.getContractNo());
                        _p.setDetail(pymnt.getDetail());
                        _p.setTxnType(pymnt.getTxnType());
                        _p.setSide(pymnt.getSide());
                        return _p;
                    }).orElse(pymnt);
                    transactionRepository.save(p);
                } catch (Exception e) {
                    // log to get the affected record
                    log.error(e.getMessage(), e);
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
        return transactionService.transactionsByDateAndType(LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()), LocalDate.now(), TxnEntry.TxnType.DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
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
        return transactionService.transactionsByDateAndType(s, e, TxnEntry.TxnType.DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/dividends/{start}/{end}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<Payment> dividends(@PathVariable String start, @PathVariable String end) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s, e, TxnEntry.TxnType.DIV);
    }

    @GetMapping("/dividends/year/{year}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Optional<BigDecimal> dividendsByYear(@PathVariable int year) {
        LocalDate start = LocalDate.of(2000, 12, 31);
        LocalDate end = LocalDate.of(year, 12, 31);
        return transactionService.transactionsByDateAndType(start, end, TxnEntry.TxnType.DIV).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/{start}/{end}/{type}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<Payment> transactionsByDateAndType(@PathVariable String start, @PathVariable String end, @PathVariable TxnEntry.TxnType type) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s,e, type);
    }

    @GetMapping("/txns/sum/{start}/{end}/{type}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Optional<BigDecimal> sumOfTransactionsByDateAndType(@PathVariable String start, @PathVariable String end, @PathVariable TxnEntry.TxnType type) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionService.transactionsByDateAndType(s,e,type).stream().map(Payment::getValue).reduce(BigDecimal::add);
    }
}
