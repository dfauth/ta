package com.github.dfauth.util;

import com.github.dfauth.ta.util.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class TypeTokenTest {

    @Test
    public void testIt() {
        {
            TypeToken<Integer> typeToken = new TypeToken<>() {
            };
            assertEquals(Integer.class, typeToken.getType());
        }
        {
            Thingy<Object> typeToken = new Thingy<>(){};
            assertEquals(Object.class, typeToken.getType());
        }
        {
            Thingy2 typeToken = new Thingy2();
            assertEquals(String.class, typeToken.getType());
        }
        {
            Thingy3<String> thingy3 = new Thingy3(){};
            Type typeToken = TypeToken.getTypeToken(thingy3.getClass());
            assertEquals(String.class, typeToken);
        }
    }

    static class Thingy<T> extends TypeToken<T> {

    }

    static class Thingy2 extends TypeToken<String> {

    }
    static class Thingy3<T> {

    }
}
