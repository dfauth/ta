package com.github.dfauth.controller;

import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import io.github.dfauth.trycatch.ExceptionalRunnable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.util.function.Predicate.not;
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
        return Stream.of(TestData.class.getDeclaredFields())
                .filter(not(f -> f.getName().equals("CODES")))
                .filter(not(f -> f.getName().equals("CODES_AS_LIST_LIST")))
                .map(f -> {
                    f.setAccessible(true);
                    return f;
                })
                .map(f -> Map.entry(
                        String.format("ASX:%s",f.getName()),
                        ExceptionalRunnable.<List<Price>>tryCatch(() -> List.class.cast(f.get(TestData.class))))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected abstract T getController();
}
