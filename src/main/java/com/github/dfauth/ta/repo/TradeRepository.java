package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.Trade;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface TradeRepository extends CrudRepository<Trade, String> {

    @Query(value = "SELECT * FROM Trade t WHERE t.CONFIRMATION_NO = ?1", nativeQuery = true)
    Trade findByConfirmationNo(String confirmation_no);

    default Trade findBy_date_code_size(Trade trade) {
        String confirmation_no = String.format("%s_%s_%d", DateTimeFormatter.ofPattern("yyyyMMdd").format(trade.getDate().toInstant()),trade.getCode(),trade.getSize());
        return findByConfirmationNo(confirmation_no);
    }


    @Query(value = "SELECT t.code, sum(t.side*t.cost) as COST,sum(t.size*t.side) as SIZE FROM Trade t GROUP by t.CODE", nativeQuery = true)
    List<Map<String,Object>> aggregateTrades();

    default List<Position> derivePositions() {
        return aggregateTrades().stream().map(m -> new Position(0, (String)m.get("CODE"), ((BigDecimal)m.get("SIZE")).intValue(), (BigDecimal)m.get("COST"))).collect(Collectors.toList());
    }
}
