package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.GapUpController;
import com.github.dfauth.ta.functions.TestData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class GapUpControllerTest extends MockPriceRepoControllerTest<GapUpController> {

    private ObjectMapper mapper = new ObjectMapper();
    private GapUpController controller = new GapUpController();

    @Test
    public void testIt() throws JsonProcessingException {

        Integer result = getController().gapUp("ASX:LBL");
        assertEquals(3, result.intValue());
    }

    @Test
    public void testPost() throws JsonProcessingException {

        Map<String, Integer> result = getController().gapUp(TestData.CODES_AS_LIST_LIST);
        assertEquals(3, result.get("ASX:LBL").intValue());
    }

    @Override
    protected GapUpController getController() {
        return controller;
    }
}
