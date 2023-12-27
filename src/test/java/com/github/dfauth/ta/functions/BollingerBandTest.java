package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.functional.BollingerBand;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.dfauth.ta.functional.Lists.last;

@Slf4j
public class BollingerBandTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testIt() throws JsonProcessingException {
        List<PriceAction> pa = Lists.mapList(TestData.EMR, PriceAction.class::cast);
        List<BollingerBand.BBPoint> bb = BollingerBand.calculateBollingerBands(23, 2.0).apply(pa);
        log.info("bb: {}",mapper.writeValueAsString(last(bb).get()));
    }
}
