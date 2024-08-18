package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.Controller;
import com.github.dfauth.ta.controller.SMAController;
import com.github.dfauth.ta.functions.TestData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SMAControllerTest extends MockPriceRepoControllerTest<SMAController> {

    private ObjectMapper mapper = new ObjectMapper();
    private SMAController controller = new SMAController();

    @Test
    public void testIt() throws JsonProcessingException {

        Optional<Controller.OHLC> result = getController().sma("ASX:EMR", 14);
        assertEquals(43218337, result.get().getV());
    }

    @Test
    public void testPost() throws JsonProcessingException {

        Map<String, Controller.OHLC> result = getController().sma(TestData.CODES_AS_LIST_LIST, 14);
        assertEquals(18799220, result.get("ASX:WGX").getV());
    }

    @Test
    public void testEmaPostMomentum() {

        Map<String, Controller.OHLC> result = getController().emaMomentum(TestData.CODES_AS_LIST_LIST, 23);
        assertEquals(0.040728d, result.get("ASX:WGX").getC().doubleValue(), 0.001);
        assertEquals(-68867.0, (double) result.get("ASX:WGX").getV(), 1d);
    }

    @Test
    public void testPostMomentum() {

//        log.info("WGX: \n{}",TestData.WGX.stream().map(p -> String.format("%.2f,%d",p.getClose().doubleValue(), p.getVolume())).collect(Collectors.joining("\n")));
        Map<String, Controller.OHLC> result = getController().smaMomentum(TestData.CODES_AS_LIST_LIST, 23);
        assertEquals(0.002268d, result.get("ASX:WGX").getC().doubleValue(), 0.001);
        assertEquals(-33173d, (double) result.get("ASX:WGX").getV(), 1d);
    }

    @Test
    public void testGetMomentum() throws JsonProcessingException {

        Optional<Controller.OHLC> result = getController().smaMomentum("ASX:WGX", 23);
        assertEquals(0.002268d, result.get().getC().doubleValue(), 0.001);
        assertEquals(-33173d, (double) result.get().getV(), 1d);
    }

    @Override
    protected SMAController getController() {
        return controller;
    }
}
