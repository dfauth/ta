package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.RSIController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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

    @Override
    protected RSIController getController() {
        return controller;
    }
}
