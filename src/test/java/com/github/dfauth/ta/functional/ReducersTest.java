package com.github.dfauth.ta.functional;

import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functions.Reducers.toCollector;
import static com.github.dfauth.ta.functions.Reducers.toList;
import static junit.framework.TestCase.assertEquals;

public class ReducersTest {

    @Test
    public void testIt() {
        List<Integer> result = IntStream.range(0, 10).mapToObj(Integer::new).collect(toCollector(toList()));
        assertEquals(List.of(0,1,2,3,4,5,6,7,8,9), result);
    }

}
