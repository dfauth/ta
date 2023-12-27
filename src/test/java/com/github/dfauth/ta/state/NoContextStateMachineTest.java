package com.github.dfauth.ta.state;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class NoContextStateMachineTest {

    private final StateMachine<TestState, NoContextStateMachineTest,TestEvent> stateMachine = StateMachine.<TestState, NoContextStateMachineTest,TestEvent>builder()
            .withExecutor(this::fakeExecute)
            .initial(b -> b.withPayload(TestState.INITIAL)
                    .onExit(this::recordTransition)
                    .withTransitions(b1 -> b1
                            .onEvent(TestEventImpl.A)
                            .unless(this::recordGuardCall)
                            .transitionTo(TestState.FINAL)
                    )
            ).withState(b -> b.withPayload(TestState.FINAL).onEntry(this::recordTransition))
            .build();

    private void fakeExecute(Runnable runnable) {
        runnable.run();
    }

    private List<TransitionEvent<TestState, NoContextStateMachineTest, TestEvent>> transitionEvents = new ArrayList<TransitionEvent<TestState, NoContextStateMachineTest, TestEvent>>();
    private int guardCalled = 0;

    private enum TestState {
        INITIAL,FINAL;
    }

    private interface TestEvent {

    }

    private enum TestEventImpl implements TestEvent {
        A,B;
    }

    @Test
    public void testIt() {
        assertTrue(() -> stateMachine.getCurrentState().callback((payload, ctx) -> payload == TestState.INITIAL && ctx == NoContextStateMachineTest.this, this));
        stateMachine.onEvent(this, TestEventImpl.B);
        assertEquals(0,guardCalled);
        assertEquals(0, transitionEvents.size());
        assertTrue(() -> stateMachine.getCurrentState().callback((payload, ctx) -> payload == TestState.INITIAL && ctx == NoContextStateMachineTest.this, this));
        stateMachine.onEvent(this, TestEventImpl.A);
        assertEquals(1,guardCalled);
        assertEquals(2, transitionEvents.size());
        assertTrue(() -> stateMachine.getCurrentState().callback((payload, ctx) -> payload == TestState.FINAL && ctx == NoContextStateMachineTest.this, this));
        stateMachine.onEvent(this, TestEventImpl.B);
        assertEquals(1,guardCalled);
        assertEquals(2, transitionEvents.size());
        assertTrue(() -> stateMachine.getCurrentState().callback((payload, ctx) -> payload == TestState.FINAL && ctx == NoContextStateMachineTest.this, this));
        stateMachine.onEvent(this, TestEventImpl.A);
        assertEquals(1,guardCalled);
        assertEquals(2, transitionEvents.size());
        assertTrue(() -> stateMachine.getCurrentState().callback((payload, ctx) -> payload == TestState.FINAL && ctx == NoContextStateMachineTest.this, this));
    }


    private boolean recordGuardCall(TestEvent event) {
        guardCalled++;
        return false;
    }

    private void recordTransition(TransitionEvent<TestState, NoContextStateMachineTest,TestEvent> transitionEvent) {
        this.transitionEvents.add(transitionEvent);
    }
}
