package com.github.dfauth.controller;

import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public abstract class MockPriceRepoControllerTest<T> {

    private PriceRepository mockRepo = mock(PriceRepository.class);

    @BeforeEach
    public void setUp() throws IllegalAccessException {
        T controller = getController();
        Field f = Stream.of(controller.getClass().getSuperclass()).map(cls -> tryCatch(() ->
                cls.getDeclaredField("repository")))
                .findFirst().orElseThrow();
        f.setAccessible(true);
        f.set(controller, mockRepo);
        getTestData().entrySet().stream().forEach(e ->
                when(mockRepo.findByCode(eq(e.getKey()), anyInt())).thenReturn(e.getValue())
        );
    }

    protected Map<String,List<Price>> getTestData() {
        return Map.of("ASX:EMR", TestData.EMR,
                "ASX:MP1", TestData.MP1,
                "ASX:CGC", TestData.CGC,
                "ASX:AX1", TestData.AX1,
                "ASX:PPL", TestData.PPL,
                "ASX:WGX", TestData.WGX
        );
    }

    protected abstract T getController();
}
