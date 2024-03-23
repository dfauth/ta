package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.ConsecutiveUpDaysController;
import com.github.dfauth.ta.functions.TestData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ConsecutiveUpDaysControllerTest extends MockPriceRepoControllerTest<ConsecutiveUpDaysController> {

    private ObjectMapper mapper = new ObjectMapper();
    private ConsecutiveUpDaysController controller = new ConsecutiveUpDaysController();

    @Test
    public void testIt() throws JsonProcessingException {
        Integer result = getController().consecutiveUpDays("ASX:LBL", 252);
        assertEquals(0, result.intValue());
    }

    @Test
    public void testPost() throws JsonProcessingException {
        Map<String, Integer> result = getController().consecutiveUpDays(TestData.CODES_AS_LIST_LIST, 252);
        assertEquals(0, result.get("ASX:LBL").intValue());
    }

    @Override
    protected ConsecutiveUpDaysController getController() {
        return controller;
    }
}
