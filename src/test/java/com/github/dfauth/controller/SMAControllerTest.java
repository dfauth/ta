package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.SMAController;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SMAControllerTest extends MockPriceRepoControllerTest<SMAController> {

    private ObjectMapper mapper = new ObjectMapper();
    private SMAController controller = new SMAController();

    @Test
    public void testIt() throws JsonProcessingException {

        Optional<PriceAction> result = getController().sma("ASX:EMR", 14);
        assertEquals(43218337, result.get().getVolume());
    }

    @Test
    public void testPost() throws JsonProcessingException {

        Map<String, PriceAction> result = getController().sma(List.of(List.of("ASX:EMR", "ASX:WGX")), 14);
        assertEquals(18799220, result.get("ASX:WGX").getVolume());
    }

    @Override
    protected SMAController getController() {
        return controller;
    }
}
