package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.VWAP;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;

@RestController
@Slf4j
public class VWAPController extends BaseController {

    @GetMapping("/vwap/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<VWAP.VolumeWeightedPrice> vwap(@PathVariable String _code, @PathVariable int period) {
        log.info("vwap/{}/{}",_code,period);
        List<PriceAction> prices = mapList(prices(_code, period),PriceAction.class::cast);
        return VWAP.calculate(prices,period);
    }

}
