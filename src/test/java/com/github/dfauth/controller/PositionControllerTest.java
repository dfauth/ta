package com.github.dfauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dfauth.ta.controller.PositionController;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.repo.PositionRepository;
import com.github.dfauth.ta.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dfauth.controller.PositionControllerTest.First.assign;
import static com.github.dfauth.ta.model.MarketEnum.ASX;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class PositionControllerTest {
    interface First {
        static <T> First assign(T value) {
            return new First() {
                @Override
                public <R> Next<R> toField(String fieldName) {
                    return new Next<R>() {
                        @Override
                        public Last<R> onObject(R object) {
                            return new Last<R>() {
                                @Override
                                public R ofClass(Class<? extends R> classOfR) {
                                    Field f = Stream.of(classOfR)
                                            .map(cls -> tryCatch(() -> cls.getDeclaredField(fieldName)))
                                            .findFirst().orElseThrow();
                                    f.setAccessible(true);
                                    return tryCatch(() -> {
                                        f.set(object, value);
                                        return object;
                                    });
                                }
                            };
                        }
                    };
                }
            };
        }
        <R> Next<R> toField(String fieldName);
        interface Next<R> {
            Last<R> onObject(R r);

            interface Last<R> {
                R ofClass(Class<? extends R> classOfR);
            }
        }
    }

    private PositionRepository mockRepo = mock(PositionRepository.class);
    private PositionController controller = new PositionController();

    @BeforeEach
    public void setUp() {
        PositionService positionService = new PositionService();
        assign(mockRepo).toField("positionRepository").onObject(positionService).ofClass(PositionService.class);
        assign(positionService).toField("positionService").onObject(controller).ofClass(PositionController.class);

        getTestData().entrySet().stream().forEach(e ->
                when(mockRepo.findPositionByCodeAndDate(anyString(), any(Timestamp.class))).thenReturn(e.getValue().stream().findFirst())
        );
    }

    protected Map<String,List<Position>> getTestData() {
        return Map.of();
//        return TestData.Position.getPositions();
    }

    @Test
    public void testIt() throws JsonProcessingException {
        Optional<Position> result = controller.position("ASX:WGX", "20240101", ASX);
        assertEquals(27.3, result);
    }

    @Test
    public void testAll() throws JsonProcessingException {
        List<Position> result = StreamSupport.stream(Spliterators.spliteratorUnknownSize(controller.getAllPositions(ASX).iterator(), 0), false).collect(Collectors.toList());
        assertFalse(result.isEmpty());
    }

}
