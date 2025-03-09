package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnType;
import com.github.dfauth.ta.repo.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> transactionsByDateAndType(LocalDate s, LocalDate e, TxnType type) {
        return paymentRepository.findByDateAndType(s,e, type);
    }

}
