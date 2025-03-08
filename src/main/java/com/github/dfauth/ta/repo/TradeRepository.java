package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Theme;
import com.github.dfauth.ta.model.Trade;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;

public interface TradeRepository extends CrudRepository<Trade, String> {

    @Query(value = "SELECT * FROM Trade t WHERE t.CONFIRMATION_NO = ?1", nativeQuery = true)
    Trade findByConfirmationNo(String confirmation_no);

    @Query(value = "SELECT * FROM Trade t WHERE t.CODE = ?1 ORDER BY DATE ASC", nativeQuery = true)
    List<Trade> findByCode(String code);

    default Trade findBy_date_code_size(Trade trade) {
        String confirmation_no = String.format("%s_%s_%d", YYYYMMDD.format(trade.getDate().toInstant()),trade.getCode(),trade.getSize());
        return findByConfirmationNo(confirmation_no);
    }


    @Query(value = "SELECT t.code, sum(t.side*t.cost) as COST,sum(t.size*t.side) as SIZE FROM Trade t GROUP by t.CODE", nativeQuery = true)
    List<Map<String,Object>> aggregateTrades();

    @Query(value = "SELECT t FROM Trade t order by t.date asc")
    Iterable<Trade> findAllByDate();

    @Query(value = "SELECT t FROM Trade t where t.date >= ?1 and t.date < ?2 order by t.date asc")
    Iterable<Trade> findAllByDate(Timestamp start, Timestamp end);

    default Iterable<Trade> findAllByDate(LocalDate start, LocalDate end) {
        return findAllByDate(
                new Timestamp(start.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()),
                new Timestamp(end.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        );
    }

    @Query(value = "SELECT t FROM Trade t, Position p where t.code = p.code and p.size > 0 order by t.date", nativeQuery = true)
    Iterable<Trade> findOpenPositionEvents();

    @Query(value = "SELECT t FROM Trade t where t.theme = ?1")
    List<Trade> findByTheme(Theme theme);
}
