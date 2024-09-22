package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.txn.TxnEntry;
import com.github.dfauth.ta.repo.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
                    transactionRepository.save(pymnt);
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
}
