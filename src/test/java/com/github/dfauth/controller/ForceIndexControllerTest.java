package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.controller.ForceIndexController;
import com.github.dfauth.ta.functional.ForceIndex;
import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class ForceIndexControllerTest {

    private ForceIndexController fic = new ForceIndexController();
    private PriceRepository mockRepo = mock(PriceRepository.class);
    private ObjectMapper mapper = new ObjectMapper();
    private String code = "ASX:EMR";
    private int period = 23;
    private int sma = 13;
    private int ema = 2;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field f = fic.getClass().getDeclaredField("repository");
        f.setAccessible(true);
        f.set(fic, mockRepo);
        when(mockRepo.findByCode(anyString(), anyInt())).thenReturn(TestData.EMR);
    }

    @Test
    public void testIt() throws JsonProcessingException {

        ForceIndex result = fic.forceIndex(code, sma, ema);
        log.info("result: {}",mapper.writeValueAsString(result));
    }
}
