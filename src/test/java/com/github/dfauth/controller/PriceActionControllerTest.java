package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.PriceActionController;
import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class PriceActionControllerTest {

    private PriceRepository mockRepo = mock(PriceRepository.class);
    private ObjectMapper mapper = new ObjectMapper();
    private int period = 21;
    private String sma = "sma";
    private String roc = "roc";

    @Test
    public void testIt() throws NoSuchFieldException, IllegalAccessException, JsonProcessingException {
        PriceActionController pac = new PriceActionController();
        Field f = pac.getClass().getDeclaredField("repository");
        f.setAccessible(true);
        f.set(pac, mockRepo);
        when(mockRepo.findByCode("EMR", period)).thenReturn(TestData.EMR);

        String smaKey = String.format("%s(%d)",sma,period);
        String rocKey = String.format("%s(%d)",roc,period);
        String allKeys = Stream.of(smaKey,rocKey).collect(Collectors.joining(","));

        Map<String, Object> result = pac.priceAction("EMR", allKeys);
        log.info("result: {}",mapper.writeValueAsString(result));
        log.info("result: {}",mapper.writeValueAsString(((Optional)result.get(smaKey)).get()));
        log.info("result: {}",mapper.writeValueAsString(((Optional)result.get(rocKey)).get()));
    }
}
