package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.VWAP;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

@Slf4j
public class VWAPTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testIt() throws JsonProcessingException {
        List<PriceAction> pa = Lists.mapList(TestData.EMR, PriceAction.class::cast);
        Optional<VWAP.VolumeWeightedPrice> vwap = VWAP.calculate(23).apply(pa);
        log.info("bb: {}",mapper.writeValueAsString(vwap.get()));
    }
}
