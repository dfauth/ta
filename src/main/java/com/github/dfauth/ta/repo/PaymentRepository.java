package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends CrudRepository<Payment, Integer> {

    @Query(value = "SELECT p from Payment p where p.code = :#{#position.code} AND p.date >= :#{#position.open}")
    List<Payment> findByOpenPosition(@Param("position") Position position);

    @Query(value = "SELECT p from Payment p where p.code = :#{#position.code} AND (p.txnType = DEP OR (p.date >= :#{#position.open} and p.date < :#{#position.close}) OR (p.exDividendDate is not null AND p.exDividendDate >= :#{#position.open} and p.exDividendDate < :#{#position.close}))")
    List<Payment> findByClosedPosition(@Param("position") Position position);

    @Query(value = "SELECT p from Payment p where p.code = ?1 AND p.date >= ?2 and p.date < ?3")
    List<Payment> findByCodeAndOpenAndClose(String code, LocalDate open, LocalDate close);

    @Query(value = "SELECT * FROM Payment p WHERE date = ?1 AND value = ?2", nativeQuery = true)
    List<Payment> _findByDateAndValue(LocalDate date, BigDecimal value);

    default Optional<Payment> findByDateAndValue(LocalDate date, BigDecimal value) {
        return Optional.of(_findByDateAndValue(date, value).iterator()).filter(Iterator::hasNext).map(Iterator::next);
    }

    @Query(value = "SELECT * FROM Payment p WHERE date >= ?1 AND date < ?2 AND type = ?3", nativeQuery = true)
    List<Payment> findByDateAndType(LocalDate start, LocalDate end, TxnType t);

    @Query(value = "SELECT balance FROM Payment p WHERE date = ?1")
    List<BigDecimal> getBalance(LocalDate d);

    @Query(value = "SELECT balance FROM Payment p WHERE date <= ?1 order by date desc")
    List<BigDecimal> getBalancesFrom(LocalDate date);

    default Optional<BigDecimal> getBalanceAsAt(LocalDate date) {
        return getBalancesFrom(date).stream().findFirst();
    }

    @Query(value = "SELECT MAX(date) FROM Payment p WHERE date <= ?1")
    List<LocalDate> getMostRecent(LocalDate date);

    @Query(value = "SELECT * FROM Payment p WHERE type = ?1", nativeQuery = true)
    List<Payment> findByType(TxnType type);

    @Query(value = "SELECT * FROM Payment p WHERE date <= ?1 order by date asc", nativeQuery = true)
    Iterable<Payment> findByDate(LocalDate date);

    @Query(value = "SELECT * FROM Payment p order by Date asc", nativeQuery = true)
    Iterable<Payment> findAllOrderByDate();

    @Query(value = "SELECT p from Payment p where p.code = ?1")
    List<Payment> findByCode(String code);

    @Query(value = "SELECT p from Payment p where p.code = ?1 AND p.txnType = DIV AND p.date >= ?2 AND (p.date <= ?3 OR (p.exDividendDate is not null AND p.exDividendDate >= ?2 and p.exDividendDate < ?3))")
    List<Payment> findByCodeAndDate(String code, LocalDate start, LocalDate end);

    @Query(value = "SELECT p from Payment p where p.code = ?1 AND p.txnType = DIV AND p.date >= ?2")
    List<Payment> findByCodeAndDate(String code, LocalDate start);
}
