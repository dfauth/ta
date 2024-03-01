package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.RSIController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class RSIControllerTest extends MockPriceRepoControllerTest<RSIController> {

    private ObjectMapper mapper = new ObjectMapper();
    private RSIController controller = new RSIController();

    @Test
    public void testIt() throws JsonProcessingException {

        Optional<BigDecimal> result = getController().rsi("ASX:EMR",14);
        assertEquals(72.5, result.get().doubleValue(), 0.01d);
    }

    @Test
    public void testPost() throws JsonProcessingException {

        Map<String, BigDecimal> result = getController().rsi(List.of(List.of("ASX:EMR", "ASX:WGX")), 14);
        assertEquals(27.3, result.get("ASX:WGX").doubleValue(), 0.01d);
    }

    @Override
    protected RSIController getController() {
        return controller;
    }
}
