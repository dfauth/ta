package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.BBController;
import com.github.dfauth.ta.functional.BollingerBand;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class BBControllerTest extends MockPriceRepoControllerTest<BBController> {

    private ObjectMapper mapper = new ObjectMapper();
    private BBController controller = new BBController();

    @Test
    public void testIt() throws JsonProcessingException {

        Optional<BollingerBand.BBPoint> result = getController().bb("ASX:EMR", 14);
        assertEquals(0.031127259994584933, result.get().getMinMarginPct().doubleValue(), 0.001d);
    }

    @Test
    public void testPost() throws JsonProcessingException {

        Map<String, BollingerBand.BBPoint> result = getController().bb(List.of(List.of("ASX:EMR", "ASX:WGX")), 14);
        assertEquals(0.024723691571050969, result.get("ASX:WGX").getMinMarginPct().doubleValue(), 0.001d);
    }

    @Override
    protected BBController getController() {
        return controller;
    }
}
