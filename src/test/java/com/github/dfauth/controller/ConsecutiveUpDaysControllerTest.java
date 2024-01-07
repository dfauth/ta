package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.ConsecutiveUpDaysController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ConsecutiveUpDaysControllerTest extends MockPriceRepoControllerTest<ConsecutiveUpDaysController> {

    private ObjectMapper mapper = new ObjectMapper();
    private ConsecutiveUpDaysController controller = new ConsecutiveUpDaysController();

    @Test
    public void testIt() throws JsonProcessingException {

        Optional<Integer> result = getController().consecutiveUpDays("ASX:EMR", 252);
        assertEquals(245, result.get().intValue());
    }

    @Override
    protected ConsecutiveUpDaysController getController() {
        return controller;
    }
}
