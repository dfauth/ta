package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.DrawDownController;
import com.github.dfauth.ta.functions.Drawdown;
import com.github.dfauth.ta.functions.TestData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class DrawDownControllerTest extends MockPriceRepoControllerTest<DrawDownController> {

    private ObjectMapper mapper = new ObjectMapper();
    private DrawDownController controller = new DrawDownController();

    @Test
    public void testIt() throws JsonProcessingException {

        Drawdown result = getController().drawDown("ASX:LBL");
        assertEquals(-0.253, result.getMaxDrawDown().doubleValue());
        assertEquals(-0.220, result.getDrawDown().doubleValue());
    }

    @Test
    public void testPost() throws JsonProcessingException {

        Map<String, Drawdown> result = getController().drawDown(TestData.CODES_AS_LIST_LIST);
        assertEquals(-0.253, result.get("ASX:LBL").getMaxDrawDown().doubleValue());
        assertEquals(-0.220, result.get("ASX:LBL").getDrawDown().doubleValue());
    }

    @Override
    protected DrawDownController getController() {
        return controller;
    }
}
