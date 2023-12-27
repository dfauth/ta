package com.github.dfauth.ta.functional;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static com.github.dfauth.ta.functions.Reducers.groupBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapsTest {

    @Test
    public void testIt() {
        Function<Integer, Map.Entry<Integer,String>> f = i -> Map.entry(i, String.valueOf(i));
        assertEquals(Map.of(1,"1", 2, "2", 3, "3"), Lists.of(1,2,3).map(f).reduce(groupBy()));
    }
}
