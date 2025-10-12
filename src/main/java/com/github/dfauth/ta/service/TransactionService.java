package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnType;
import com.github.dfauth.ta.repo.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> transactionsByDateAndType(LocalDate s, LocalDate e, TxnType type) {
        return paymentRepository.findByDateAndType(s,e, type);
    }

    public List<Payment> transactionsByType(TxnType type) {
        return paymentRepository.findByType(type);
    }

    public Optional<BigDecimal> cashAsAt(LocalDate date) {
        return paymentRepository.getBalanceAsAt(date);
    }

    public Iterable<Payment> findAll() {
        return paymentRepository.findAllOrderByDate();
    }

    public Iterable<Payment> findByDate(LocalDate date) {
        return paymentRepository.findByDate(date);
    }

    public List<Payment> findByCode(String code) {
        return paymentRepository.findByCode(code);
    }

    public List<Payment> findByCodeAndDate(String code, LocalDate start) {
        return paymentRepository.findByCodeAndDate(code, start);
    }

    public List<Payment> findByCodeAndDate(String code, LocalDate start, LocalDate end) {
        return paymentRepository.findByCodeAndDate(code, start, end);
    }
}
