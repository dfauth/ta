package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Payment, Integer> {

    @Query(value = "SELECT * FROM Payment p WHERE date = ?1 AND value = ?2", nativeQuery = true)
    List<Payment> _findByDateAndValue(LocalDate date, BigDecimal value);

    default Optional<Payment> findByDateAndValue(LocalDate date, BigDecimal value) {
        return Optional.of(_findByDateAndValue(date, value).iterator()).filter(Iterator::hasNext).map(Iterator::next);
    }

    @Query(value = "SELECT * FROM Payment p WHERE date >= ?1 AND date < ?2 AND type = ?3", nativeQuery = true)
    List<Payment> findByDateAndType(LocalDate start, LocalDate end, TxnEntry.TxnType t);

    @Query(value = "SELECT balance FROM Payment p WHERE date = ?1")
    List<BigDecimal> getBalance(LocalDate d);

    default Optional<BigDecimal> getBalanceAsAt(LocalDate date) {
        return getMostRecent(date).stream().flatMap(d -> getBalance(d).stream()).reduce((prev, next) -> next);
    }

    @Query(value = "SELECT MAX(date) FROM Payment p WHERE date <= ?1")
    List<LocalDate> getMostRecent(LocalDate date);
}
