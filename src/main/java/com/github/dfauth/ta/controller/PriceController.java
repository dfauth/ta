package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

public abstract class PriceController {

    @Autowired
    protected PriceRepository repository;

    List<Price> prices(String _code, int period) {
        return repository.findByCode(_code, period);
    }

    List<Price> prices(String _code, Timestamp marketDate, int period) {
        return repository.findByCodeAndDate(_code, marketDate, period);
    }

    Timestamp latestPriceDate() {
        return repository.latestPriceDate();
    }

    List<Price> prices(String _code) {
        return repository.findByCode(_code, 252);
    }

}
