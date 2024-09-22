package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Side;
import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.service.PositionService;
import com.github.dfauth.ta.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.controller.ControllerUtils.toBigDecimal;

@RestController
@Slf4j
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private PositionService positionService;

    private DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;

    @PostMapping("/sync/trades")
    @ResponseStatus(HttpStatus.CREATED)
    Integer trades(@RequestBody Object[][] args) {
        return trades("ASX",args);
    }

    @PostMapping("/sync/trades/{market}")
    @ResponseStatus(HttpStatus.CREATED)
    Integer trades(@PathVariable String market, @RequestBody Object[][] args) {
        try {
            log.info("args: ",args);

            List<Trade> trades = Stream.of(args)
                    .filter(arr -> arr[0] != null)
                    .filter(arr -> arr[0] != "")
                    .map(arr -> {
                                String confirmationNo = arr[18].toString();
                                Timestamp date = new Timestamp(LocalDate.parse((String) arr[0], dtf).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli());
                                String code = market+":"+(String)arr[1];
                                int size =(Integer)arr[2];
                                BigDecimal price = toBigDecimal(arr[3]);
                                BigDecimal cost = toBigDecimal(arr[4]);
                                Side side = Side.fromString(arr[5]);
                                String notes = arr[14].toString();
                                return  new Trade(0,
                                                confirmationNo,
                                                date,
                                                code,
                                                size,
                                                price,
                                                cost,
                                                side,
                                                side.getMultiplier(),
                                                notes);
                            }
                    )
                    .collect(Collectors.toList());
            return tradeService.sync(trades);
        } finally {
            asynchronously(() -> positionService.sync());
        }
    }

    private void asynchronously(Runnable runnable) {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

}
