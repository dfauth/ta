package com.github.dfauth.ta.state;

import com.github.dfauth.ta.functions.PNV;
import com.github.dfauth.ta.model.Price;
import org.junit.Test;

import java.util.List;

import static com.github.dfauth.ta.functional.FunctionUtils.unsupported;
import static junit.framework.TestCase.assertEquals;

public class StateTest {

    public static List<Price> prices = PNV.toPrices("PNV", PNV.PRICE_STRING);

    @Test
    public void testIt() {
        CandlestickPatternState state = prices.stream()
                .<CandlestickPatternState>reduce(
                        InitialState.INITIAL_STATE,
                        CandlestickPatternState::onEvent,
                        unsupported()
                );

        assertEquals(InitialState.INITIAL_STATE, state);
    }

    static class InitialState implements CandlestickPatternState {

        public static final InitialState INITIAL_STATE = new InitialState();

        private InitialState() {
        }

        @Override
        public CandlestickPatternState onEvent(Price price) {
            return this;
        }
    }

    interface CandlestickPatternState {
        CandlestickPatternState onEvent(Price price);
    }
}
