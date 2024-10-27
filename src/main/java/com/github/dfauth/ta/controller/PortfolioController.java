package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.PortfolioSummary;
import com.github.dfauth.ta.service.PortfolioService;
import com.github.dfauth.ta.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/portfolio")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioSummary portfolio() {
        return portfolio(0);
    }

    @GetMapping("/portfolio/elapsed/{period}")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioSummary portfolio(@PathVariable int period) {
        return portfolioService.summary(LocalDate.now().minusDays(period));
    }

    @GetMapping("/portfolio/compare/{period}")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioSummary portfolioComparison(@PathVariable int period) {
        PortfolioSummary ref = portfolioService.summary(LocalDate.now());
        return ref.compare(portfolioService.summary(LocalDate.now().minusDays(period)));
    }

    @GetMapping("/portfolio/date/{date}")
    @ResponseStatus(HttpStatus.OK)
    public PortfolioSummary portfolioByDate(@PathVariable String date) {
        LocalDate localDate = (LocalDate) DateTimeUtils.Format.YYYYMMDD.parse(date);
        return portfolioService.summary(localDate);
    }
}
