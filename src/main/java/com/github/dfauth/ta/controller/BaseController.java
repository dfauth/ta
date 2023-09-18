package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.repo.ValuationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public abstract class BaseController {

    @Autowired
    protected PriceRepository repository;

    @Autowired
    protected ValuationRepository valuationRepo;

    List<Price> prices(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }


}
