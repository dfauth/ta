package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.PriceActionController;
import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
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

    private PriceActionController pac = new PriceActionController();
    private PriceRepository mockRepo = mock(PriceRepository.class);
    private ObjectMapper mapper = new ObjectMapper();
    private int period = 21;
    private String sma = "sma";
    private String roc = "roc";
    private String lobf = "lobf";

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field f = pac.getClass().getDeclaredField("repository");
        f.setAccessible(true);
        f.set(pac, mockRepo);
        when(mockRepo.findByCode("EMR", period)).thenReturn(TestData.EMR);
    }

    @Test
    public void testIt() throws JsonProcessingException {

        String smaKey = String.format("%s(%d)",sma,period);
        String rocKey = String.format("%s(%d)",roc,period);
        String allKeys = Stream.of(smaKey,rocKey).collect(Collectors.joining("|"));

        Map<String, Object> result = pac.priceAction("EMR", allKeys);
        log.info("result: {}",mapper.writeValueAsString(((Optional)result.get(smaKey)).get()));
        log.info("result: {}",mapper.writeValueAsString(((Optional)result.get(rocKey)).get()));
    }

    @Ignore
    @Test
    public void testAlt() throws JsonProcessingException {

        String lobfKey = String.format("%s(%d)",lobf,period);
        String allKeys = Stream.of(lobfKey).collect(Collectors.joining("|"));

        Map<String, Object> result = pac.priceAction("EMR", allKeys);
        log.info("result: {}",mapper.writeValueAsString(((Optional)result.get(lobfKey)).get()));
    }
}
