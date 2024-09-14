package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Side;
import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.repo.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.controller.ControllerUtils.toBigDecimal;

@RestController
@Slf4j
public class TradeController {

    @Autowired
    private TradeRepository tradeRepo;

    private DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;

    // Save
    @PostMapping("/sync/trades")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Integer trades(@RequestBody Object[][] args) {
        log.info("args: ",args);

        List<Trade> trades = Stream.of(args)
                .filter(arr -> arr[0] != null)
                .filter(arr -> arr[0] != "")
                .map(arr -> {
                    String confirmationNo = arr[18].toString();
                    Timestamp date = new Timestamp(LocalDate.parse((String) arr[0], dtf).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli());
                    String code = (String)arr[1];
                    int size =(Integer)arr[2];
                    BigDecimal price = toBigDecimal(arr[3]);
                    BigDecimal cost = toBigDecimal(arr[4]);
                    Side side = Side.fromString(arr[5]);
                    String notes = arr[14].toString();
                    return  Optional.ofNullable(tradeRepo.findByConfirmationNo(confirmationNo))
                            .map(t -> {
                                t.setCode(code);
                                t.setDate(date);
                                t.setSize(size);
                                t.setPrice(price);
                                t.setCost(cost);
                                t.setSide(side);
                                t.setNotes(notes);
                                return t;
                            })
                            .orElse(new Trade(0,
                                            confirmationNo,
                                            date,
                                            code,
                                            size,
                                            price,
                                            cost,
                                            side,
                                            notes)
                            );
                        }
                )
        .collect(Collectors.toList());
        tradeRepo.saveAll(trades);
        return trades.size();
    }

    @PostMapping("/sync/trades/update")
    @ResponseStatus(HttpStatus.CREATED)
    Integer tradeUpdate(@RequestBody Object[][] args) {
        log.info("args: ",args);

        List<Trade> trades = Stream.of(args).map(arr -> new Trade(0,
                arr[18].toString(),
                new Timestamp(LocalDate.parse((String) arr[0], dtf).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()),
                (String) arr[1],
                (Integer) arr[2],
                toBigDecimal(arr[3]),
                toBigDecimal(arr[4]),
                Side.fromString(arr[5]),
                arr[14].toString())
        ).map(t -> Optional.ofNullable(tradeRepo.findBy_date_code_size(t))
                .map(t1 -> {
                    t1.setConfirmation_no(t.getConfirmation_no());
                    return t1;
                })).flatMap(Optional::stream)
                .map(t -> tradeRepo.save(t)).collect(Collectors.toList());
        return trades.size();
    }

}
