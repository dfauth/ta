package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Theme;
import com.github.dfauth.ta.model.TradingMetrics;
import com.github.dfauth.ta.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/analyse")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TradingMetrics> analyse() {
        return analysisService.analyse();
    }

    @GetMapping("/analyse/theme/{theme}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TradingMetrics> analyseByTheme(@PathVariable Theme theme) {
        return analysisService.analyseByTheme(theme);
    }
}
