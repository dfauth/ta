package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnEntry;
import com.github.dfauth.ta.repo.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Payment> transactionsByDateAndType(LocalDate s, LocalDate e, TxnEntry.TxnType type) {
        return transactionRepository.findByDateAndType(s,e, type);
    }

}
