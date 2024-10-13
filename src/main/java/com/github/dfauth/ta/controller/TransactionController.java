package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.txn.TxnEntry;
import com.github.dfauth.ta.repo.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;

@RestController
@Slf4j
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/txns/sync")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void txnSync(@RequestBody List<TxnEntry.Payment> txns) {
        try {
            List<TxnEntry.Payment> reconsituted = txns.stream().map(pymnt -> pymnt.getTxnType().<TxnEntry.Payment>blah(pymnt)).collect(Collectors.toList());
            log.info("txns/sync: {}",reconsituted);
//            transactionRepository.saveAll(txns);
            txns.stream().forEach(pymnt -> {
                try {
                    TxnEntry.Payment p = transactionRepository.findByDateAndValue(pymnt.getDate(), pymnt.getValue()).map(_p -> {
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
        return transactionsByDateAndType(start, end, TxnEntry.TxnType.DIV).stream().map(TxnEntry.Payment::getValue).reduce(BigDecimal::add);
    }

    @GetMapping("/txns/dividends/{start}/{end}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<TxnEntry.Payment> dividends(@PathVariable String start, @PathVariable String end) {
        return transactionsByDateAndType(start, end, TxnEntry.TxnType.DIV);
    }

    @GetMapping("/txns/{start}/{end}/{type}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<TxnEntry.Payment> transactionsByDateAndType(@PathVariable String start, @PathVariable String end, @PathVariable TxnEntry.TxnType type) {
        LocalDate s = (LocalDate) YYYYMMDD.parse(start);
        LocalDate e = (LocalDate) YYYYMMDD.parse(end);
        return transactionRepository.findByDateAndType(s,e, type);
    }
}
