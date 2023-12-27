package com.github.dfauth.ta.state.pattern;

import com.github.dfauth.ta.model.Candlestick;
import com.github.dfauth.ta.state.StateMachine;
import org.junit.jupiter.api.Test;

import static com.github.dfauth.ta.state.pattern.CandlestickPatternState.INITIAL;

public class CandlestickPatternStateMachineTest {

    private final StateMachine<CandlestickPatternState,Void, Candlestick> stateMachine = StateMachine.<CandlestickPatternState,Void, Candlestick>builder()
            .initial(b -> b.withPayload(INITIAL)
            )
            .build();


    @Test
    public void testIt() {

    }
}
