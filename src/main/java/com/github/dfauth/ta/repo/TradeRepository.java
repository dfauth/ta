package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Trade;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.format.DateTimeFormatter;

public interface TradeRepository extends CrudRepository<Trade, String> {

    @Query(value = "SELECT * FROM Trade t WHERE t.CONFIRMATION_NO = ?1", nativeQuery = true)
    Trade findByConfirmationNo(String confirmation_no);

    default Trade findBy_date_code_size(Trade trade) {
        String confirmation_no = String.format("%s_%s_%d", DateTimeFormatter.ofPattern("yyyyMMdd").format(trade.getDate().toInstant()),trade.getCode(),trade.getSize());
        return findByConfirmationNo(confirmation_no);
    }


}
