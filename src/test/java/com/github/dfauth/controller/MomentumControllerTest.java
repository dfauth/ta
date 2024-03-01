package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.MomentumController;
import com.github.dfauth.ta.functions.Momentum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class MomentumControllerTest extends MockPriceRepoControllerTest<MomentumController> {

    private ObjectMapper mapper = new ObjectMapper();
    private MomentumController controller = new MomentumController();

    @Test
    public void testIt() throws JsonProcessingException {

        Optional<Momentum> result = getController().momentum("ASX:WGX");
        assertEquals(-0.071, result.map(Momentum::getMomentum).get().doubleValue(), 0.01d);
        assertEquals(92.9, result.map(Momentum::getPercentage).get().doubleValue(), 0.01d);
        assertEquals(-7.1, result.map(Momentum::getPercentageIncrease).get().doubleValue(), 0.01d);
        assertEquals(-0.3087, result.map(Momentum::getPercentagePerPeriod).get().doubleValue(), 0.01d);
    }

    @Override
    protected MomentumController getController() {
        return controller;
    }
}
