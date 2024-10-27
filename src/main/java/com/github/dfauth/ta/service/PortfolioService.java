package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.Market;
import com.github.dfauth.ta.model.MarketEnum;
import com.github.dfauth.ta.model.PortfolioSummary;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.repo.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.util.StreamOps.stream;

@Slf4j
@Service
public class PortfolioService {

    @Autowired
    private PositionService positionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PriceRepository priceRepository;

    public PortfolioSummary summary(LocalDate date) {
        return summary(date, MarketEnum.ASX);
    }

    public PortfolioSummary summary(LocalDate date, Market market) {

        Function<String, BigDecimal> f = code -> priceRepository.findLatestByCodeAndDate(code, date, market)
                .map(Price::getClose)
                .orElse(BigDecimal.ZERO);

        PortfolioSummary portfolioSummary = stream(positionService.findOpenPositions(date))
                .filter(p -> p.getSize() != 0)
                .reduce(new PortfolioSummary(), (ps, p) -> {
                    return ps.withPosition(p, f.apply(p.getCode()));
                }, oops());
        return transactionRepository.getBalanceAsAt(date)
                .map(b -> portfolioSummary.withCash(b))
                .orElse(portfolioSummary);
    }
}
