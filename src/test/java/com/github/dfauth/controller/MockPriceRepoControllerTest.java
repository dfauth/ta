package com.github.dfauth.controller;

import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dfauth.ta.util.ExceptionalRunnable.tryCatch;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(mockRepo.findByCode(anyString(), anyInt())).thenReturn(getTestData());
    }

    protected List<Price> getTestData() {
        return TestData.EMR;
    }

    protected abstract T getController();
}
