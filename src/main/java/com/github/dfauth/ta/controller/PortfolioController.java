package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.PortfolioMetrics;
import com.github.dfauth.ta.service.PositionService;
import com.github.dfauth.ta.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.github.dfauth.ta.util.StreamOps.stream;

@Slf4j
@RestController
public class PortfolioController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/metrics/portfolio")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioMetrics portfolioMetrics() {
        return stream(positionService.findAll()).reduce(new PortfolioMetrics(), PortfolioMetrics::add, PortfolioMetrics::add);
    }

    @GetMapping("/metrics/portfolio/year/{year}")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioMetrics portfolioMetricsByYear(@PathVariable int year) {
        LocalDate yearEnding = LocalDate.of(year, 12,31);
        LocalDate start = LocalDate.of(2000, 12, 31);
        LocalDate end = LocalDate.of(year, 12, 31);
        PortfolioMetrics metrics = stream(positionService.getPositionAsAt(yearEnding)).reduce(new PortfolioMetrics(), PortfolioMetrics::add, PortfolioMetrics::add);
        return metrics;
    }

}
